package com.vincenthuto.hemomancy.common.worldgen.feature;

final class FungalFloorPattern {
	private FungalFloorPattern() {
	}

	static int heightAt(long seed, int x, int z) {
		return 1 + Math.floorMod(mix(seed, x >> 2, z >> 2), 3);
	}

	static int surfaceAt(long seed, int x, int z) {
		int cellX = Math.floorDiv(x, 8);
		int cellZ = Math.floorDiv(z, 8);
		int material = coarseSurfaceAt(seed, cellX, cellZ);
		int localX = Math.floorMod(x, 8);
		int localZ = Math.floorMod(z, 8);
		int neighborX = localX < 2 ? cellX - 1 : localX > 5 ? cellX + 1 : cellX;
		int neighborZ = localZ < 2 ? cellZ - 1 : localZ > 5 ? cellZ + 1 : cellZ;
		int xMaterial = coarseSurfaceAt(seed, neighborX, cellZ);
		int zMaterial = coarseSurfaceAt(seed, cellX, neighborZ);
		int candidate = xMaterial != material && zMaterial != material
				? ((mix(seed, x, z) & 1) == 0 ? xMaterial : zMaterial)
				: xMaterial != material ? xMaterial : zMaterial;

		if (candidate == material) return material;
		int edgeDistance = Math.min(Math.min(localX, 7 - localX), Math.min(localZ, 7 - localZ));
		int chance = edgeDistance == 0 ? 2 : 1;
		return Math.floorMod(mix(seed ^ 0xD1B54A32D192ED03L, x, z), 4) < chance ? candidate : material;
	}

	private static int coarseSurfaceAt(long seed, int cellX, int cellZ) {
		return Math.floorMod(mix(seed ^ 0x9E3779B97F4A7C15L, cellX, cellZ), 3);
	}

	private static int mix(long seed, int x, int z) {
		long value = seed ^ x * 0x632BE59BD9B4E019L ^ z * 0x9E3779B97F4A7C15L;
		value ^= value >>> 30;
		value *= 0xBF58476D1CE4E5B9L;
		value ^= value >>> 27;
		return (int) (value ^ value >>> 31);
	}
}
