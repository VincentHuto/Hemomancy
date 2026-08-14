package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
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
