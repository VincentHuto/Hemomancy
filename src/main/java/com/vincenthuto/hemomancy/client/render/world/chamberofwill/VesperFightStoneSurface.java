package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import net.minecraft.util.Mth;

final class VesperFightStoneSurface {
	static final int SUBDIVISIONS = 4;
	private static final float MAX_RELIEF = 0.013F;
	private static final float TILE_DEPTH = 0.055F;

	private VesperFightStoneSurface() {
	}

	static float relief(VesperFightArenaLayout.Tile tile, int gridX, int gridZ) {
		return relief(tile.x(), tile.z(), tile.material().ordinal(), tile.variant().ordinal(), gridX, gridZ);
	}

	private static float relief(int tileX, int tileZ, int material, int variant, int gridX, int gridZ) {
		if (gridX == 0 || gridZ == 0 || gridX == SUBDIVISIONS || gridZ == SUBDIVISIONS) return 0.0F;
		float u = gridX / (float) SUBDIVISIONS;
		float v = gridZ / (float) SUBDIVISIONS;
		float edgeFade = Mth.sin(Mth.PI * u) * Mth.sin(Mth.PI * v);
		return signedUnit(hash(tileX, tileZ, material, variant, gridX, gridZ, 0x58BDE6A9L))
				* MAX_RELIEF * edgeFade;
	}

	static int tone(VesperFightArenaLayout.Tile tile, int gridX, int gridZ) {
		return tone(tile.x(), tile.z(), tile.material().ordinal(), tile.variant().ordinal(), gridX, gridZ);
	}

	static float slabBottom(float y00, float y10, float y11, float y01) {
		return Math.min(Math.min(y00, y10), Math.min(y11, y01)) - TILE_DEPTH;
	}

	private static int tone(int tileX, int tileZ, int material, int variant, int gridX, int gridZ) {
		return (int) Math.floorMod(hash(tileX, tileZ, material, variant, gridX, gridZ, 0x13D54A7FL), 15L) - 7;
	}

	private static long hash(int tileX, int tileZ, int material, int variant,
			int gridX, int gridZ, long salt) {
		long value = salt;
		value ^= (long) tileX * 0x9E3779B97F4A7C15L;
		value ^= (long) tileZ * 0xC2B2AE3D27D4EB4FL;
		value ^= (long) gridX * 0x165667B19E3779F9L;
		value ^= (long) gridZ * 0x85EBCA77C2B2AE63L;
		value ^= (long) material << 48;
		value ^= (long) variant << 56;
		value ^= value >>> 33;
		value *= 0xff51afd7ed558ccdl;
		value ^= value >>> 33;
		value *= 0xc4ceb9fe1a85ec53l;
		return value ^ value >>> 33;
	}

	private static float signedUnit(long hash) {
		return ((hash >>> 40) & 0xFFFFL) / 32767.5F - 1.0F;
	}
}
