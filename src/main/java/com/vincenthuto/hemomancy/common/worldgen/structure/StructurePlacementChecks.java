package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import java.util.OptionalInt;

final class StructurePlacementChecks {
	private static final int MAX_LAND_STRUCTURE_HEIGHT = 150;
	private static final int MAX_ALLOWED_WATER_DEPTH = 2;
	private static final int MAX_ALLOWED_SWAMP_WATER_DEPTH = 8;
	private static final int MAUSOLEUM_FOOTPRINT_RADIUS = 32;
	private static final int MAUSOLEUM_SAMPLE_STEP = 16;
	private static final int MAX_MAUSOLEUM_SURFACE_VARIATION = 10;
	private static final int OUTPOST_RADIUS = 28;
	private static final int OUTPOST_SAMPLE_STEP = 7;
	private static final int MAX_OUTPOST_SURFACE_VARIATION = 8;
	private static final int CIRCUS_PAVILION_FOOTPRINT_RADIUS = 24;
	private static final int CIRCUS_PAVILION_SAMPLE_STEP = 12;

	private StructurePlacementChecks() {
	}

	static boolean canPlaceOverworldHemomancyStructure(Structure.GenerationContext context) {
		return !isVoidRefugeContext(context);
	}

	static boolean isSuitableLandChunk(Structure.GenerationContext context) {
		return canPlaceOverworldHemomancyStructure(context)
				&& isSuitableLandChunk(context, MAX_ALLOWED_WATER_DEPTH);
	}

	static boolean isSuitableSwampChunk(Structure.GenerationContext context) {
		return canPlaceOverworldHemomancyStructure(context)
				&& isSuitableLandChunk(context, MAX_ALLOWED_SWAMP_WATER_DEPTH);
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

	/** The complete courtyard and its four wings must fit on one modestly sloped site. */
	static OptionalInt harbingerOutpostSurface(Structure.GenerationContext context) {
		if (!canPlaceOverworldHemomancyStructure(context)) return OptionalInt.empty();
		int centerX = context.chunkPos().getMiddleBlockX();
		int centerZ = context.chunkPos().getMiddleBlockZ();
		int low = Integer.MAX_VALUE;
		int high = Integer.MIN_VALUE;
		for (int dx = -OUTPOST_RADIUS; dx <= OUTPOST_RADIUS; dx += OUTPOST_SAMPLE_STEP) {
			for (int dz = -OUTPOST_RADIUS; dz <= OUTPOST_RADIUS; dz += OUTPOST_SAMPLE_STEP) {
				int x = centerX + dx;
				int z = centerZ + dz;
				if (!isSuitableLandColumn(context, x, z, MAX_ALLOWED_WATER_DEPTH)) return OptionalInt.empty();
				int surface = getSurfaceHeight(context, x, z);
				low = Math.min(low, surface);
				high = Math.max(high, surface);
			}
		}
		// The gate and the opposing hall project beyond the square. Check every
		// cardinal approach because the root jigsaw can rotate before placement.
		for (int reach : new int[] {31, 40}) {
			for (int cross : new int[] {-8, -4, 0, 4, 8}) {
				for (int[] offset : new int[][] {{reach, cross}, {-reach, cross}, {cross, reach}, {cross, -reach}}) {
					int x = centerX + offset[0];
					int z = centerZ + offset[1];
					if (!isSuitableLandColumn(context, x, z, MAX_ALLOWED_WATER_DEPTH)) return OptionalInt.empty();
					int surface = getSurfaceHeight(context, x, z);
					low = Math.min(low, surface);
					high = Math.max(high, surface);
				}
			}
		}
		return high - low <= MAX_OUTPOST_SURFACE_VARIATION ? OptionalInt.of(high) : OptionalInt.empty();
	}

	static boolean isSuitableCircusPavilionSite(Structure.GenerationContext context) {
		if (!canPlaceOverworldHemomancyStructure(context)) {
			return false;
		}

		ChunkPos chunkPos = context.chunkPos();
		int centerX = chunkPos.getMinBlockX() + 8;
		int centerZ = chunkPos.getMinBlockZ() + 8;
		int sampleCount = (CIRCUS_PAVILION_FOOTPRINT_RADIUS * 2 / CIRCUS_PAVILION_SAMPLE_STEP + 1);
		int[] heights = new int[sampleCount * sampleCount];
		int sampleIndex = 0;

		for (int xOffset = -CIRCUS_PAVILION_FOOTPRINT_RADIUS;
				xOffset <= CIRCUS_PAVILION_FOOTPRINT_RADIUS;
				xOffset += CIRCUS_PAVILION_SAMPLE_STEP) {
			for (int zOffset = -CIRCUS_PAVILION_FOOTPRINT_RADIUS;
					zOffset <= CIRCUS_PAVILION_FOOTPRINT_RADIUS;
					zOffset += CIRCUS_PAVILION_SAMPLE_STEP) {
				int x = centerX + xOffset;
				int z = centerZ + zOffset;
				if (!isSuitableLandColumn(context, x, z, 0)) {
					return false;
				}
				heights[sampleIndex++] = getSurfaceHeight(context, x, z);
			}
		}

		return CircusPavilionPlacementRules.canPlacePavilion(true, true, true,
				CircusPavilionPlacementRules.surfaceVariation(heights));
	}

    // Validate the selected rotation and template, rather than a guessed radius around the chunk.
    static boolean isSuitableTroupeFootprint(Structure.GenerationContext context,
            net.minecraft.world.level.levelgen.structure.BoundingBox box) {
        int low = Integer.MAX_VALUE, high = Integer.MIN_VALUE;
        for (int x = box.minX(); x <= box.maxX(); x++) for (int z = box.minZ(); z <= box.maxZ(); z++) {
            if (!isSuitableLandColumn(context, x, z, 0)) return false;
            int ground = getOceanFloorHeight(context, x, z);
            low = Math.min(low, ground); high = Math.max(high, ground);
            if (high - low > 3 || Math.abs(ground - box.minY()) > 3) return false;
            var column = context.chunkGenerator().getBaseColumn(x, z, context.heightAccessor(), context.randomState());
            for (int y = Math.max(ground, box.minY()) + 1; y <= box.maxY(); y++)
                if (!column.getBlock(y).isAir()) return false;
        }
        return true;
    }

	static boolean isSuitableOceanWreckChunk(Structure.GenerationContext context) {
		if (!canPlaceOverworldHemomancyStructure(context)) {
			return false;
		}
		ChunkPos chunkPos = context.chunkPos();
		int minX = chunkPos.getMinBlockX();
		int minZ = chunkPos.getMinBlockZ();
		int[][] samples = {
				{ 8, 8 },
				{ 2, 2 },
				{ 13, 2 },
				{ 2, 13 },
				{ 13, 13 }
		};

		int minFloor = Integer.MAX_VALUE;
		int maxFloor = Integer.MIN_VALUE;
		for (int[] sample : samples) {
			int x = minX + sample[0];
			int z = minZ + sample[1];
			int surface = getSurfaceHeight(context, x, z);
			int floor = getOceanFloorHeight(context, x, z);
			minFloor = Math.min(minFloor, floor);
			maxFloor = Math.max(maxFloor, floor);
			if (!HarbingerVoyagerWreckPlacementRules.canPlaceWreck(surface > floor, true, false,
					surface - floor, 0)) {
				return false;
			}
		}

		return HarbingerVoyagerWreckPlacementRules.canPlaceWreck(true, true, false,
				HarbingerVoyagerWreckPlacementRules.MIN_WATER_DEPTH, maxFloor - minFloor);
	}

	static boolean isSuitableActiveVoyagerVesselChunk(Structure.GenerationContext context) {
		if (!canPlaceOverworldHemomancyStructure(context)) {
			return false;
		}
		ChunkPos chunkPos = context.chunkPos();
		int minX = chunkPos.getMinBlockX();
		int minZ = chunkPos.getMinBlockZ();
		int[][] samples = {
				{ 8, 8 },
				{ 2, 2 },
				{ 13, 2 },
				{ 2, 13 },
				{ 13, 13 }
		};

		int minSurface = Integer.MAX_VALUE;
		int maxSurface = Integer.MIN_VALUE;
		int seaLevel = context.chunkGenerator().getSeaLevel();
		for (int[] sample : samples) {
			int x = minX + sample[0];
			int z = minZ + sample[1];
			int surface = getSurfaceHeight(context, x, z);
			int floor = getOceanFloorHeight(context, x, z);
			minSurface = Math.min(minSurface, surface);
			maxSurface = Math.max(maxSurface, surface);
			boolean waterColumn = surface <= seaLevel + 2 && surface > floor;
			int waterDepth = surface - floor;
			if (!ActiveHarbingerVoyagerVesselPlacementRules.canPlaceVessel(waterColumn, true, waterDepth, 0)) {
				return false;
			}
		}

		return ActiveHarbingerVoyagerVesselPlacementRules.canPlaceVessel(true, true,
				ActiveHarbingerVoyagerVesselPlacementRules.MIN_WATER_DEPTH, maxSurface - minSurface);
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

	private static int getOceanFloorHeight(Structure.GenerationContext context, int x, int z) {
		return context.chunkGenerator().getFirstOccupiedHeight(x, z,
				Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
	}

	private static boolean isVoidRefugeContext(Structure.GenerationContext context) {
		var possibleBiomes = context.biomeSource().possibleBiomes();
		if (possibleBiomes.size() != 1) {
			return false;
		}
		Holder<Biome> onlyBiome = possibleBiomes.iterator().next();
		return onlyBiome.is(Biomes.THE_VOID);
	}
}
