package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class BloodLanternJellySpawnRulesTest {
	public static void main(String[] args) {
		allowsWaterInSpawnBoundingBox();
	}

	private static void allowsWaterInSpawnBoundingBox() {
		assertTrue(BloodLanternJellySpawnRules.shouldIgnoreLiquidSpawnObstruction(),
				"blood lantern jellies spawn in water, so liquid must not fail the final obstruction check");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
