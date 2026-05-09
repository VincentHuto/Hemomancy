package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;

final class StructurePlacementChecks {
	private static final int MAX_LAND_STRUCTURE_HEIGHT = 150;
	private static final int MAX_ALLOWED_WATER_DEPTH = 2;
	private static final int MAX_ALLOWED_SWAMP_WATER_DEPTH = 8;
	private static final int MAUSOLEUM_FOOTPRINT_RADIUS = 32;
	private static final int MAUSOLEUM_SAMPLE_STEP = 16;
	private static final int MAX_MAUSOLEUM_SURFACE_VARIATION = 10;

	private StructurePlacementChecks() {
	}

	static boolean isSuitableLandChunk(Structure.GenerationContext context) {
		return isSuitableLandChunk(context, MAX_ALLOWED_WATER_DEPTH);
	}

	static boolean isSuitableSwampChunk(Structure.GenerationContext context) {
		return isSuitableLandChunk(context, MAX_ALLOWED_SWAMP_WATER_DEPTH);
	}

	private static boolean isSuitableLandChunk(Structure.GenerationContext context, int maxAllowedWaterDepth) {
		ChunkPos chunkPos = context.chunkPos();
		int minX = chunkPos.getMinBlockX();
		int minZ = chunkPos.getMinBlockZ();
		int[][] samples = {
				{ 8, 8 },
				{ 0, 0 },
				{ 15, 0 },
				{ 0, 15 },
				{ 15, 15 }
		};

		for (int[] sample : samples) {
			if (!isSuitableLandColumn(context, minX + sample[0], minZ + sample[1], maxAllowedWaterDepth)) {
				return false;
			}
		}
		return true;
	}

	static boolean isSuitableBuriedMausoleumSite(Structure.GenerationContext context) {
		if (!isSuitableLandChunk(context)) {
			return false;
		}

		ChunkPos chunkPos = context.chunkPos();
		int centerX = chunkPos.getMinBlockX() + 8;
		int centerZ = chunkPos.getMinBlockZ() + 8;
		int centerSurface = getSurfaceHeight(context, centerX, centerZ);

		if (!isSuitableLandColumn(context, centerX, centerZ, MAX_ALLOWED_WATER_DEPTH)) {
			return false;
		}

		for (int xOffset = -MAUSOLEUM_FOOTPRINT_RADIUS; xOffset <= MAUSOLEUM_FOOTPRINT_RADIUS; xOffset += MAUSOLEUM_SAMPLE_STEP) {
			for (int zOffset = -MAUSOLEUM_FOOTPRINT_RADIUS; zOffset <= MAUSOLEUM_FOOTPRINT_RADIUS; zOffset += MAUSOLEUM_SAMPLE_STEP) {
				int x = centerX + xOffset;
				int z = centerZ + zOffset;
				if (!isSuitableLandColumn(context, x, z, MAX_ALLOWED_WATER_DEPTH)) {
					return false;
				}

				int surface = getSurfaceHeight(context, x, z);
				if (Math.abs(surface - centerSurface) > MAX_MAUSOLEUM_SURFACE_VARIATION) {
					return false;
				}
			}
		}

		return true;
	}

	private static boolean isSuitableLandColumn(Structure.GenerationContext context, int x, int z, int maxAllowedWaterDepth) {
		int surface = getSurfaceHeight(context, x, z);
		if (surface >= MAX_LAND_STRUCTURE_HEIGHT) {
			return false;
		}

		int oceanFloor = context.chunkGenerator().getFirstOccupiedHeight(x, z,
				Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
		return surface - oceanFloor <= maxAllowedWaterDepth;
	}

	private static int getSurfaceHeight(Structure.GenerationContext context, int x, int z) {
		return context.chunkGenerator().getFirstOccupiedHeight(x, z,
				Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
	}
}
