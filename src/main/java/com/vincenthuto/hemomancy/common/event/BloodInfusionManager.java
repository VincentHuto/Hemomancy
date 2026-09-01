package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodCraftRing;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.recipe.BloodInfusionRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public final class BloodInfusionManager {
	private static final double SYNC_RANGE = 64.0D;
	private static final int COMPLETION_VISIBLE_TICKS = BloodInfusionRules.COLLAPSE_TICKS + 22;
	private static final Map<TargetKey, Progress> ACTIVE = new HashMap<>();
	private static final Map<TargetKey, Pending> PENDING = new HashMap<>();

	private BloodInfusionManager() {
	}

	public static boolean feedBlock(ServerPlayer player, ServerLevel level, BlockPos pos, double feedRate) {
		RecipeHolder<BloodInfusionRecipe> holder = findRecipe(level, pos);
		if (holder == null) {
			return false;
		}

		TargetKey key = new TargetKey(level.dimension(), pos.immutable());
		if (PENDING.containsKey(key)) {
			return true;
		}

		BloodInfusionRecipe recipe = holder.value();
		Progress progress = ACTIVE.get(key);
		if (progress != null && !progress.recipeId.equals(holder.id())) {
			sync(level, pos, 0.0F, true, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);
			progress = null;
		}
		if (progress == null) {
			progress = new Progress(holder.id());
			ACTIVE.put(key, progress);
		}

		IBloodVolume blood = HemoCapabilityAccess.requireBloodVolume(player);
		double amount = BloodStructureFeedRules.feedAmount(progress.bloodFed, recipe.bloodCost(),
				blood.getBloodVolume(), Math.max(0.0D, feedRate));
		if (amount <= 0.0D) {
			return true;
		}

		blood.drain(amount);
		progress.bloodFed += amount;
		progress.lastFedGameTime = level.getGameTime();
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(blood));
		float normalized = (float) Math.min(1.0D, progress.bloodFed / recipe.bloodCost());
		sync(level, pos, normalized, false, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);

		if (BloodStructureFeedRules.isComplete(progress.bloodFed, recipe.bloodCost())) {
			ACTIVE.remove(key);
			PENDING.put(key, new Pending(recipe, BloodInfusionRules.COLLAPSE_TICKS));
			sync(level, pos, 1.0F, false, COMPLETION_VISIBLE_TICKS);
			PacketDistributor.sendToPlayersNear(level, null, pos.getX() + 0.5D, pos.getY() + 0.5D,
					pos.getZ() + 0.5D, SYNC_RANGE,
					new PacketBloodCraftRing(pos, 2.5F, pos.getY() + 0.5F, BloodInfusionRules.COLLAPSE_TICKS));
		}
		return true;
	}

	public static void tick(ServerLevel level) {
		long gameTime = level.getGameTime();
		Iterator<Map.Entry<TargetKey, Progress>> active = ACTIVE.entrySet().iterator();
		while (active.hasNext()) {
			Map.Entry<TargetKey, Progress> entry = active.next();
			if (entry.getKey().dimension.equals(level.dimension())
					&& BloodStructureFeedRules.isExpired(gameTime, entry.getValue().lastFedGameTime)) {
				sync(level, entry.getKey().pos, 0.0F, true, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);
				active.remove();
			}
		}

		Iterator<Map.Entry<TargetKey, Pending>> pending = PENDING.entrySet().iterator();
		while (pending.hasNext()) {
			Map.Entry<TargetKey, Pending> entry = pending.next();
			if (!entry.getKey().dimension.equals(level.dimension()) || --entry.getValue().remainingTicks > 0) {
				continue;
			}
			complete(level, entry.getKey().pos, entry.getValue().recipe);
			pending.remove();
		}
	}

	public static void clear() {
		ACTIVE.clear();
		PENDING.clear();
	}

	private static RecipeHolder<BloodInfusionRecipe> findRecipe(ServerLevel level, BlockPos pos) {
		boolean hasBlockEntity = level.getBlockEntity(pos) != null;
		return level.getRecipeManager().getAllRecipesFor(RecipeInit.blood_infusion_type.get()).stream()
				.sorted(Comparator.comparing(holder -> holder.id().toString()))
				.filter(holder -> holder.value().matches(level.getBlockState(pos), hasBlockEntity))
				.findFirst().orElse(null);
	}

	private static void complete(ServerLevel level, BlockPos pos, BloodInfusionRecipe recipe) {
		boolean matches = level.getBlockState(pos).is(recipe.input());
		if (!BloodInfusionRules.canComplete(matches, level.getBlockEntity(pos) != null)) {
			sync(level, pos, 0.0F, true, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);
			return;
		}
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		level.playSound(null, pos, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 0.7F, 1.25F);
		level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D, recipe.resultStack()));
	}

	private static void sync(ServerLevel level, BlockPos pos, float progress, boolean clear, int visibleTicks) {
		PacketDistributor.sendToPlayersNear(level, null, pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D, SYNC_RANGE,
				new PacketBloodStructureFeed(List.of(pos), progress, visibleTicks, clear));
	}

	private record TargetKey(ResourceKey<Level> dimension, BlockPos pos) {
	}

	private static final class Progress {
		private final ResourceLocation recipeId;
		private double bloodFed;
		private long lastFedGameTime;

		private Progress(ResourceLocation recipeId) {
			this.recipeId = recipeId;
		}
	}

	private static final class Pending {
		private final BloodInfusionRecipe recipe;
		private int remainingTicks;

		private Pending(BloodInfusionRecipe recipe, int remainingTicks) {
			this.recipe = recipe;
			this.remainingTicks = remainingTicks;
		}
	}
}
