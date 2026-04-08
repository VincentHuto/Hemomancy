package com.vincenthuto.hemomancy.common.crafting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

/**
 * Server-side manager for delayed blood structure crafting.
 * When a blood craft ring animation starts, the actual block breaking
 * and result spawning are deferred until the ring finishes collapsing.
 */
public class PendingBloodCraftManager {

	private static final List<PendingCraft> pendingCrafts = new ArrayList<>();

	public static class PendingCraft {
		private final ServerLevel level;
		private final BlockPattern.BlockPatternMatch patternMatch;
		private final int patternWidth;
		private final int patternHeight;
		private final int patternDepth;
		private final BlockPos hitPos;
		private final ItemStack result;
		private int remainingTicks;

		public PendingCraft(ServerLevel level, BlockPattern.BlockPatternMatch patternMatch,
				int patternWidth, int patternHeight, int patternDepth,
				BlockPos hitPos, ItemStack result, int delayTicks) {
			this.level = level;
			this.patternMatch = patternMatch;
			this.patternWidth = patternWidth;
			this.patternHeight = patternHeight;
			this.patternDepth = patternDepth;
			this.hitPos = hitPos;
			this.result = result;
			this.remainingTicks = delayTicks;
		}

		/** Tick down; returns true when complete. */
		public boolean tick() {
			if (--remainingTicks <= 0) {
				execute();
				return true;
			}
			return false;
		}

		private void execute() {
			// Clear the structure blocks
			for (int i = 0; i < patternWidth; ++i) {
				for (int j = 0; j < patternHeight; ++j) {
					for (int k = 0; k < patternDepth; ++k) {
						BlockInWorld cachedBlockInfo = patternMatch.getBlock(i, j, k);
						level.setBlock(cachedBlockInfo.getPos(),
								Blocks.AIR.defaultBlockState(), 2);
						level.levelEvent(2001, cachedBlockInfo.getPos(),
								Block.getId(cachedBlockInfo.getState()));
					}
				}
			}

			// Sound & result drop
			level.playSound(null, hitPos, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 1, 1);
			level.addFreshEntity(new ItemEntity(level,
					hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5, result));
		}
	}

	/** Schedule a craft to execute after delayTicks. */
	public static void schedule(PendingCraft craft) {
		pendingCrafts.add(craft);
	}

	/** Called every server tick to advance pending crafts. */
	public static void tick() {
		Iterator<PendingCraft> it = pendingCrafts.iterator();
		while (it.hasNext()) {
			if (it.next().tick()) {
				it.remove();
			}
		}
	}

	public static void clear() {
		pendingCrafts.clear();
	}
}
