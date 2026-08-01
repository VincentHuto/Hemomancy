package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureOfferingBurst;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodStructureCraftingHelper;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BloodStructureFeedManager {
	private static final Map<FeedKey, FeedProgress> ACTIVE_FEEDS = new HashMap<>();
	private static final Map<FeedKey, Long> COMPLETING_FEEDS = new HashMap<>();
	private static final Map<UUID, Long> LAST_HINT_TICK = new HashMap<>();
	private static final double FEED_RATE = BloodStructureFeedRules.STRUCTURE_FEED_RATE;
	private static final int CRAFT_ANIMATION_TICKS = 30;
	private static final int COMPLETION_LOCK_TICKS = CRAFT_ANIMATION_TICKS + 5;
	private static final int COMPLETION_VISIBLE_TICKS = CRAFT_ANIMATION_TICKS + 22;
	private static final int FEED_SYNC_VISIBLE_TICKS = BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS;
	private static final double FEED_SYNC_RANGE = 64.0;
	private static final int HINT_INTERVAL_TICKS = 20;

	private BloodStructureFeedManager() {
	}

	public static boolean feedStructure(ServerPlayer player, ServerLevel level, BlockPos hitPos, ItemStack offhandCatalyst) {
		return feedStructure(player, level, hitPos, offhandCatalyst, FEED_RATE);
	}

	public static boolean feedStructure(ServerPlayer player, ServerLevel level, BlockPos hitPos, ItemStack offhandCatalyst,
			double feedRate) {
		var candidate = BloodStructureCraftingHelper.findProjectionCraftMatch(player, level, hitPos, offhandCatalyst);
		if (candidate.isEmpty()) {
			return false;
		}

		var match = candidate.get();
		if (!match.valid()) {
			sendHint(player, match.error());
			return true;
		}

		BloodStructureRecipe recipe = match.recipe();
		BlockPattern blockPattern = recipe.getPattern().getBlockPattern();
		List<BlockPos> positions = BloodStructureCraftingHelper.getVisibleMatchPositions(match.match(), blockPattern,
				recipe.getPattern().getPatternArray());
		FeedKey key = new FeedKey(level.dimension(), recipe.getId(), positions.stream().map(BlockPos::asLong).toList());
		long gameTime = level.getGameTime();
		Long lockedUntilGameTime = COMPLETING_FEEDS.get(key);
		if (lockedUntilGameTime != null) {
			if (BloodStructureFeedRules.isCompletionLocked(gameTime, lockedUntilGameTime)) {
				return true;
			}
			COMPLETING_FEEDS.remove(key);
		}

		FeedProgress progress = ACTIVE_FEEDS.get(key);
		double currentProgress = progress == null ? 0.0 : progress.bloodFed;

		IBloodVolume bloodVolume = HemoCapabilityAccess.getBloodVolume(player)
				.orElseThrow(NullPointerException::new);
		double feedAmount = BloodStructureFeedRules.feedAmount(currentProgress, recipe.getBloodCost(),
				bloodVolume.getBloodVolume(), Math.max(0.0D, feedRate));
		if (feedAmount <= 0.0) {
			sendHint(player, Component.literal("Not enough blood can be projected into the formation.")
					.withStyle(ChatFormatting.DARK_RED));
			if (progress != null) {
				sendFeedSync(level, positions, (float) (progress.bloodFed / recipe.getBloodCost()), false);
			}
			return true;
		}

		if (progress == null) {
			progress = new FeedProgress();
			ACTIVE_FEEDS.put(key, progress);
		}
		bloodVolume.drain(feedAmount);
		progress.bloodFed += feedAmount;
		progress.lastFedGameTime = gameTime;
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(bloodVolume));

		float normalizedProgress = (float) Math.min(1.0, progress.bloodFed / recipe.getBloodCost());
		sendFeedSync(level, positions, normalizedProgress, false);

		if (BloodStructureFeedRules.isComplete(progress.bloodFed, recipe.getBloodCost())) {
			completeFeed(player, level, hitPos, offhandCatalyst, match.match(), blockPattern, recipe, positions,
					match.offerings(), key);
		}
		return true;
	}

	public static void tick(ServerLevel level) {
		long gameTime = level.getGameTime();
		Iterator<Map.Entry<FeedKey, FeedProgress>> iterator = ACTIVE_FEEDS.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<FeedKey, FeedProgress> entry = iterator.next();
			FeedKey key = entry.getKey();
			if (!key.dimension.equals(level.dimension())) {
				continue;
			}
			if (BloodStructureFeedRules.isExpired(gameTime, entry.getValue().lastFedGameTime)) {
				sendFeedSync(level, key.positions.stream().map(BlockPos::of).toList(), 0.0f, true);
				iterator.remove();
			}
		}
		COMPLETING_FEEDS.entrySet().removeIf(entry -> entry.getKey().dimension.equals(level.dimension())
				&& !BloodStructureFeedRules.isCompletionLocked(gameTime, entry.getValue()));
	}

	public static void clear() {
		ACTIVE_FEEDS.clear();
		COMPLETING_FEEDS.clear();
		LAST_HINT_TICK.clear();
	}

	private static void completeFeed(ServerPlayer player, ServerLevel level, BlockPos hitPos, ItemStack offhandCatalyst,
			BlockPattern.BlockPatternMatch match, BlockPattern blockPattern, BloodStructureRecipe recipe,
			List<BlockPos> positions, List<BlockPos> offerings, FeedKey key) {
		if (!player.getAbilities().instabuild) {
			offhandCatalyst.shrink(1);
		}

		List<BloodStructureCraftingHelper.ConsumedOffering> consumedOfferings =
				BloodStructureCraftingHelper.consumeMatchedOfferings(level, offerings);
		AABB bounds = BloodStructureCraftingHelper.getMatchBounds(match, blockPattern);
		BlockPos ringCenter = BlockPos.containing(bounds.getCenter().x, bounds.minY, bounds.getCenter().z);
		float centerY = (float) bounds.getCenter().y;
		float startRadius = (float) Math.max(bounds.getXsize(), bounds.getZsize()) / 2.0f + 2.0f;

		sendFeedSync(level, positions, 1.0f, false, COMPLETION_VISIBLE_TICKS);
		sendOfferingBurst(level, hitPos, consumedOfferings);
		PacketDistributor.sendToAllPlayers(new PacketBloodCraftRing(ringCenter, startRadius,
				centerY, CRAFT_ANIMATION_TICKS));

		PendingBloodCraftManager.schedule(new PendingBloodCraftManager.PendingCraft(
				level, match,
				blockPattern.getWidth(), blockPattern.getHeight(), blockPattern.getDepth(),
				hitPos, recipe.getResult().copy(),
				CRAFT_ANIMATION_TICKS, player));
		ACTIVE_FEEDS.remove(key);
		COMPLETING_FEEDS.put(key, level.getGameTime() + COMPLETION_LOCK_TICKS);
	}

	private static void sendOfferingBurst(ServerLevel level, BlockPos center,
			List<BloodStructureCraftingHelper.ConsumedOffering> consumedOfferings) {
		if (consumedOfferings.isEmpty()) {
			return;
		}
		List<PacketBloodStructureOfferingBurst.OfferingBurst> bursts = consumedOfferings.stream()
				.map(offering -> new PacketBloodStructureOfferingBurst.OfferingBurst(offering.pos(), offering.stack()))
				.toList();
		PacketDistributor.sendToPlayersNear(level, null, center.getX() + 0.5D, center.getY() + 0.5D,
				center.getZ() + 0.5D, FEED_SYNC_RANGE,
				new PacketBloodStructureOfferingBurst(bursts, center, CRAFT_ANIMATION_TICKS));
	}

	private static void sendFeedSync(ServerLevel level, List<BlockPos> positions, float progress, boolean clear) {
		sendFeedSync(level, positions, progress, clear, FEED_SYNC_VISIBLE_TICKS);
	}

	private static void sendFeedSync(ServerLevel level, List<BlockPos> positions, float progress, boolean clear,
			int visibleTicks) {
		if (positions.isEmpty()) {
			return;
		}
		AABB bounds = boundsFor(positions);
		PacketDistributor.sendToPlayersNear(level, null, bounds.getCenter().x, bounds.getCenter().y, bounds.getCenter().z,
				FEED_SYNC_RANGE, new PacketBloodStructureFeed(positions, progress, visibleTicks, clear));
	}

	private static AABB boundsFor(List<BlockPos> positions) {
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos pos : positions) {
			if (pos.getX() < minX) minX = pos.getX();
			if (pos.getX() > maxX) maxX = pos.getX();
			if (pos.getY() < minY) minY = pos.getY();
			if (pos.getY() > maxY) maxY = pos.getY();
			if (pos.getZ() < minZ) minZ = pos.getZ();
			if (pos.getZ() > maxZ) maxZ = pos.getZ();
		}
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	private static void sendHint(ServerPlayer player, Component message) {
		long gameTime = player.serverLevel().getGameTime();
		long lastHint = LAST_HINT_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE);
		if (gameTime - lastHint < HINT_INTERVAL_TICKS) {
			return;
		}
		player.displayClientMessage(message, true);
		player.serverLevel().playLocalSound(player.blockPosition().getX(), player.blockPosition().getY(),
				player.blockPosition().getZ(), SoundEvents.ENDERMAN_SCREAM, null, 0.35f, 1.25f, false);
		LAST_HINT_TICK.put(player.getUUID(), gameTime);
	}

	private record FeedKey(ResourceKey<Level> dimension, ResourceLocation recipeId, List<Long> positions) {
		private FeedKey {
			positions = List.copyOf(positions);
		}
	}

	private static final class FeedProgress {
		private double bloodFed;
		private long lastFedGameTime;
	}
}
