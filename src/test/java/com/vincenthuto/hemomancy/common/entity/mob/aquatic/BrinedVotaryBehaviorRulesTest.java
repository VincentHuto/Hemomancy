package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class BrinedVotaryBehaviorRulesTest {
	public static void main(String[] args) {
		onlyWakesForCloseLivingPlayers();
		swimsSlowerThanItWalks();
	}

	private static void onlyWakesForCloseLivingPlayers() {
		assertTrue(BrinedVotaryBehaviorRules.shouldWakeForTarget(true, true,
				BrinedVotaryBehaviorRules.WAKE_RANGE_BLOCKS - 0.5D),
				"brined votaries should wake when a living player comes close");
		assertFalse(BrinedVotaryBehaviorRules.shouldWakeForTarget(false, true, 2.0D),
				"brined votaries should ignore non-player targets for V1");
		assertFalse(BrinedVotaryBehaviorRules.shouldWakeForTarget(true, false, 2.0D),
				"brined votaries should not wake for dead targets");
		assertFalse(BrinedVotaryBehaviorRules.shouldWakeForTarget(true, true,
				BrinedVotaryBehaviorRules.WAKE_RANGE_BLOCKS + 0.5D),
				"brined votaries should stay dormant at longer ranges");
	}

	private static void swimsSlowerThanItWalks() {
		assertTrue(BrinedVotaryBehaviorRules.SWIM_SPEED < BrinedVotaryBehaviorRules.LAND_SPEED,
				"brined votaries should feel waterlogged and slow underwater");
		assertTrue(BrinedVotaryBehaviorRules.ATTACK_DAMAGE > 0.0D,
				"brined votaries should remain a real close-range hazard");
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}
}
