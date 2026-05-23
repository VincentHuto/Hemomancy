package com.vincenthuto.hemomancy.common.entity.mob.animal;

final class VerdigrisMothRulesTest {
	public static void main(String[] args) {
		onlySpawnsAtNightOnSolidGround();
		shedsOnlyWhileLivingAirborneAndOffCooldown();
		brushHarvestIsLimitedAndNonLethal();
	}

	private static void onlySpawnsAtNightOnSolidGround() {
		assertTrue(VerdigrisMothRules.isNight(18000L),
				"midnight should count as a verdigris moth spawn window");
		assertFalse(VerdigrisMothRules.isNight(6000L),
				"daytime should not count as a verdigris moth spawn window");
		assertTrue(VerdigrisMothRules.canNaturalSpawn(true, true, true, 4),
				"verdigris moths should be eligible for dark forest/cold night spawns");
		assertFalse(VerdigrisMothRules.canNaturalSpawn(false, true, true, 4),
				"verdigris moths should not naturally spawn in dark daytime shade");
		assertFalse(VerdigrisMothRules.canNaturalSpawn(true, true, true, 12),
				"verdigris moths should not naturally spawn in bright daylight");
		assertFalse(VerdigrisMothRules.canNaturalSpawn(true, false, true, 4),
				"verdigris moths need a solid surface below their initial spawn position");
		assertFalse(VerdigrisMothRules.canNaturalSpawn(true, true, false, 4),
				"verdigris moths need open air where they spawn");
	}

	private static void shedsOnlyWhileLivingAirborneAndOffCooldown() {
		assertTrue(VerdigrisMothRules.canShedNaturally(false, true, false, false, 0),
				"living airborne moths on the server should be able to shed Virid Salis");
		assertFalse(VerdigrisMothRules.canShedNaturally(true, true, false, false, 0),
				"client ticks should never create real Virid Salis drops");
		assertFalse(VerdigrisMothRules.canShedNaturally(false, false, false, false, 0),
				"dead moths should not shed Virid Salis");
		assertFalse(VerdigrisMothRules.canShedNaturally(false, true, true, false, 0),
				"landed moths should not passively shed Virid Salis");
		assertFalse(VerdigrisMothRules.canShedNaturally(false, true, false, true, 0),
				"moths in water should not shed Virid Salis");
		assertFalse(VerdigrisMothRules.canShedNaturally(false, true, false, false, 20),
				"natural shedding should respect its cooldown");
	}

	private static void brushHarvestIsLimitedAndNonLethal() {
		assertTrue(VerdigrisMothRules.canBrushShed(false, true, 0),
				"brushing a living off-cooldown moth should provide a gentle Virid Salis harvest");
		assertFalse(VerdigrisMothRules.canBrushShed(true, true, 0),
				"client-side brushing should not create drops");
		assertFalse(VerdigrisMothRules.canBrushShed(false, false, 0),
				"dead moths should not be brush-harvestable");
		assertFalse(VerdigrisMothRules.canBrushShed(false, true, 1),
				"brush harvesting should respect its cooldown");
		assertTrue(VerdigrisMothRules.BRUSH_SHED_COOLDOWN_TICKS > VerdigrisMothRules.MIN_NATURAL_SHED_COOLDOWN_TICKS,
				"brushing should be useful but not a rapid farm");
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
