package com.vincenthuto.hemomancy.common.block.shared;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BloodwoodGrowthHandler {
	private static final long PROGRESS_EXPIRY_TICKS = 20L * 60L * 10L;
	private static final Map<GrowthKey, GrowthProgress> PROGRESS = new ConcurrentHashMap<>();

	private BloodwoodGrowthHandler() {
	}

	public static double tryGrowFromProjection(ServerLevel level, BlockPos deadBushPos, ServerPlayer player,
			double maxAmount) {
		if (maxAmount <= 0.0D || !level.getBlockState(deadBushPos).is(Blocks.DEAD_BUSH)) {
			return 0.0D;
		}
		if (!hasClearance(level, deadBushPos)) {
			player.displayClientMessage(Component.literal("The dead bush cannot open enough space for bloodwood.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}

		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive() || volume.getBloodVolume() <= 0.0D) {
			player.displayClientMessage(Component.literal("No active blood answers the dead bush.")
					.withStyle(ChatFormatting.DARK_RED), true);
			return 0.0D;
		}

		GrowthKey key = new GrowthKey(level.dimension(), deadBushPos.immutable());
		long gameTime = level.getGameTime();
		GrowthProgress progress = PROGRESS.get(key);
		double current = progress == null || gameTime - progress.lastUpdatedTick() > PROGRESS_EXPIRY_TICKS
				? 0.0D
				: progress.amount();
		double remaining = Math.max(0.0D, BloodwoodGrowthRules.PROJECTION_BLOOD_COST - current);
		double transfer = Math.min(maxAmount, Math.min(remaining, volume.getBloodVolume()));
		if (transfer <= 0.0D || !volume.drain(transfer)) {
			return 0.0D;
		}
		volume.addBloodSpend(transfer);
		BloodVolumeEvents.syncVolume(player, volume);

		double updated = current + transfer;
		if (updated + 0.001D < BloodwoodGrowthRules.PROJECTION_BLOOD_COST) {
			PROGRESS.put(key, new GrowthProgress(updated, gameTime));
			return transfer;
		}

		PROGRESS.remove(key);
		growTree(level, deadBushPos);
		level.playSound(null, deadBushPos, SoundEvents.AZALEA_PLACE, SoundSource.BLOCKS, 1.0F, 0.55F);
		player.displayClientMessage(Component.literal("The dead bush drinks deeply and becomes bloodwood.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), true);
		return transfer;
	}

	private static boolean hasClearance(ServerLevel level, BlockPos origin) {
		for (BloodwoodGrowthRules.Offset offset : BloodwoodGrowthRules.requiredClearance()) {
			BlockPos pos = origin.offset(offset.x(), offset.y(), offset.z());
			BlockState state = level.getBlockState(pos);
			if (pos.equals(origin)) {
				continue;
			}
			if (!state.canBeReplaced()) {
				return false;
			}
		}
		return true;
	}

	private static void growTree(ServerLevel level, BlockPos origin) {
		BlockState log = BlockInit.blood_wood_log.get().defaultBlockState();
		BlockState leaves = BlockInit.blood_wood_leaves.get().defaultBlockState()
				.setValue(LeavesBlock.PERSISTENT, true);
		for (BloodwoodGrowthRules.Offset offset : BloodwoodGrowthRules.leafOffsets()) {
			level.setBlock(origin.offset(offset.x(), offset.y(), offset.z()), leaves, Block.UPDATE_ALL);
		}
		for (BloodwoodGrowthRules.Offset offset : BloodwoodGrowthRules.logOffsets()) {
			level.setBlock(origin.offset(offset.x(), offset.y(), offset.z()), log, Block.UPDATE_ALL);
		}
	}

	private record GrowthKey(ResourceKey<net.minecraft.world.level.Level> dimension, BlockPos pos) {
	}

	private record GrowthProgress(double amount, long lastUpdatedTick) {
	}
}
