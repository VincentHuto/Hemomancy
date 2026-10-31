package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * The Order's surface sites author their Iron Braziers lit, but a rotated or mirrored template loads them cold
 * ({@link BrazierBlock} puts the fire out when it turns). Each chunk's afterPlace relights the braziers its own pass placed.
 */
public final class StructureBraziers {
	private StructureBraziers() {
	}

	public static void rekindle(WorldGenLevel level, BoundingBox fullBox, BoundingBox chunkBox) {
		int minX = Math.max(fullBox.minX(), chunkBox.minX());
		int maxX = Math.min(fullBox.maxX(), chunkBox.maxX());
		int minZ = Math.max(fullBox.minZ(), chunkBox.minZ());
		int maxZ = Math.min(fullBox.maxZ(), chunkBox.maxZ());
		var pos = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = fullBox.minY(); y <= fullBox.maxY(); y++) {
					var state = level.getBlockState(pos.set(x, y, z));
					if (state.is(BlockInit.iron_brazier.get()) && !BrazierBlock.isLit(state)
							&& !state.getValue(BrazierBlock.WATERLOGGED)) {
						level.setBlock(pos, state.setValue(BrazierBlock.RITUAL_PHASE, 1), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}
}
