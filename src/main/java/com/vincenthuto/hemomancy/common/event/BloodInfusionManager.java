package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.RecipeInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketBloodStructureFeed;
import com.vincenthuto.hemomancy.common.recipe.BloodInfusionRecipe;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
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
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class BloodInfusionManager {
	private static final BloodAccounting.Source INFUSION = BloodAccounting.Source.of("blood_infusion", Category.RITE, false);
	private static final double SYNC_RANGE = 64.0D;
	private static final int COMPLETION_VISIBLE_TICKS = BloodInfusionRules.COLLAPSE_TICKS + 22;
	private static final Map<TargetKey, Progress> ACTIVE = new HashMap<>();
	private static final Map<TargetKey, Pending> PENDING = new HashMap<>();

	private BloodInfusionManager() {
	}

	public static boolean feedBlock(ServerPlayer player, ServerLevel level, BlockPos pos, double feedRate) {
		return feedBlock(player, level, pos, pos.getCenter().add(0.0D, 0.5D, 0.0D), feedRate);
	}

	/** {@code hit} is where the projection strikes now; the veins spread out from wherever it is aimed. */
	public static boolean feedBlock(ServerPlayer player, ServerLevel level, BlockPos pos, Vec3 hit,
			double feedRate) {
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
			sync(level, pos, progress.origin, 0.0F, true, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);
			progress = null;
		}
		if (progress == null) {
			// The collapse lifts the block, so a player who may not break it never starts the infusion.
			if (!ManipulationCombatHelper.mayAlter(player, pos) || !PlayerBlockEdits.mayBreak(player, pos)) {
				player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
						"message.hemomancy.player_block_edits.refused").withStyle(net.minecraft.ChatFormatting.DARK_RED), true);
				return true;
			}
			progress = new Progress(holder.id(), hit);
			ACTIVE.put(key, progress);
		}

		IBloodVolume blood = HemoCapabilityAccess.getBloodVolume(player);
		double amount = BloodStructureFeedRules.feedAmount(progress.bloodFed, recipe.bloodCost(),
				blood.getBloodVolume(), Math.max(0.0D, feedRate));
		if (amount <= 0.0D) {
			return true;
		}

		BloodAccounting.pay(player, amount, INFUSION);
		progress.origin = hit;
		progress.bloodFed += amount;
		progress.lastFedGameTime = level.getGameTime();
		float normalized = (float) Math.min(1.0D, progress.bloodFed / recipe.bloodCost());
		sync(level, pos, progress.origin, normalized, false, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);

		if (BloodStructureFeedRules.isComplete(progress.bloodFed, recipe.bloodCost())) {
			ACTIVE.remove(key);
			PENDING.put(key, new Pending(recipe, BloodInfusionRules.COLLAPSE_TICKS, player));
			sync(level, pos, progress.origin, 1.0F, false, COMPLETION_VISIBLE_TICKS);
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
				sync(level, entry.getKey().pos, entry.getValue().origin, 0.0F, true,
						BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);
				active.remove();
			}
		}

		Iterator<Map.Entry<TargetKey, Pending>> pending = PENDING.entrySet().iterator();
		while (pending.hasNext()) {
			Map.Entry<TargetKey, Pending> entry = pending.next();
			if (!entry.getKey().dimension.equals(level.dimension()) || --entry.getValue().remainingTicks > 0) {
				continue;
			}
			complete(level, entry.getKey().pos, entry.getValue());
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
				// A recipe that names a Sculpting is that Sculpting's alone; plain infusion never runs it.
				.filter(holder -> holder.value().sculpting().isEmpty())
				.filter(holder -> holder.value().matches(level.getBlockState(pos), hasBlockEntity))
				.findFirst().orElse(null);
	}

	private static void complete(ServerLevel level, BlockPos pos, Pending pending) {
		BloodInfusionRecipe recipe = pending.recipe;
		boolean matches = level.getBlockState(pos).is(recipe.input());
		ServerPlayer player = pending.editor(level);
		// The infuser lifts the block; one who has left, or may no longer break it, leaves it standing.
		if (!BloodInfusionRules.canComplete(matches, level.getBlockEntity(pos) != null) || player == null
				|| PlayerBlockEdits.lift(player, pos) == null) {
			sync(level, pos, pos.getCenter(), 0.0F, true, BloodStructureFeedRules.PROGRESS_TIMEOUT_TICKS);
			return;
		}
		level.playSound(null, pos, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 0.7F, 1.25F);
		level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D, recipe.resultStack()));
		if (recipe.resultStack().is(com.vincenthuto.hemomancy.common.init.BlockInit.venous_stone.get().asItem())) {
			com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment.recordVenousStone(player);
		}
	}

	private static void sync(ServerLevel level, BlockPos pos, Vec3 origin, float progress, boolean clear,
			int visibleTicks) {
		PacketHandler.sendNear(level, pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D, SYNC_RANGE,
				PacketBloodStructureFeed.infusion(pos, origin, progress, visibleTicks, clear));
	}

	private record TargetKey(ResourceKey<Level> dimension, BlockPos pos) {
	}

	private static final class Progress {
		private final ResourceLocation recipeId;
		private Vec3 origin;
		private double bloodFed;
		private long lastFedGameTime;

		private Progress(ResourceLocation recipeId, Vec3 origin) {
			this.recipeId = recipeId;
			this.origin = origin;
		}
	}

	private static final class Pending {
		private final BloodInfusionRecipe recipe;
		private final UUID playerId;
		private final ServerPlayer infuser;
		private int remainingTicks;

		private Pending(BloodInfusionRecipe recipe, int remainingTicks, ServerPlayer infuser) {
			this.recipe = recipe;
			this.remainingTicks = remainingTicks;
			this.playerId = infuser.getUUID();
			this.infuser = infuser;
		}

		/** The infuser as the server knows them now (after a respawn, the new body), or null once they have left. */
		private ServerPlayer editor(ServerLevel level) {
			ServerPlayer online = level.getServer().getPlayerList().getPlayer(playerId);
			if (online != null) return online;
			return infuser.isRemoved() ? null : infuser;
		}
	}
}
