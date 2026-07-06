package com.vincenthuto.hemomancy.common.manipulation;

public final class ManipulationStatusRulesTest {
	private ManipulationStatusRulesTest() {
	}

	public static void main(String[] args) {
		conductiveMarkOnlyRespondsToPlannedSchools();
		conductiveMarkThrottlesPerTarget();
		insatiableHungerUsesPlannedHealingAndFoodPenalties();
		graveDebtDetectsLowHealthCrossing();
		ironRetortUsesPlannedGuardValues();
		sanguineMagnetismTargetsHostilesOnly();
	}

	private static void conductiveMarkOnlyRespondsToPlannedSchools() {
		assertTrue("ductilis primary arcs",
				ManipulationStatusRules.isConductiveSchoolName("DUCTILIS", null));
		assertTrue("lux primary arcs",
				ManipulationStatusRules.isConductiveSchoolName("LUX", null));
		assertTrue("ferric primary arcs",
				ManipulationStatusRules.isConductiveSchoolName("FERRIC", null));
		assertTrue("secondary ferric arcs",
				ManipulationStatusRules.isConductiveSchoolName("ANIMUS", "FERRIC"));
		assertFalse("mortem alone does not arc",
				ManipulationStatusRules.isConductiveSchoolName("MORTEM", null));
		assertFalse("tenebris/lux opposition is not automatic unless one school is present",
				ManipulationStatusRules.isConductiveSchoolName("TENEBRIS", "MORTEM"));
	}

	private static void conductiveMarkThrottlesPerTarget() {
		assertTrue("first arc with no prior tick", ManipulationStatusRules.canTriggerConductiveArc(100L, -1L));
		assertFalse("arc is throttled inside interval", ManipulationStatusRules.canTriggerConductiveArc(110L, 100L));
		assertTrue("arc is allowed at interval", ManipulationStatusRules.canTriggerConductiveArc(115L, 100L));
		assertEquals("arc target cap", 3, ManipulationStatusRules.CONDUCTIVE_ARC_TARGETS);
		assertEquals("arc duration", 160, ManipulationStatusRules.CONDUCTIVE_MARK_TICKS);
	}

	private static void insatiableHungerUsesPlannedHealingAndFoodPenalties() {
		assertEquals("heal multiplier", 0.25F, ManipulationStatusRules.INSATIABLE_HEAL_MULTIPLIER);
		assertEquals("food hunger duration", 160, ManipulationStatusRules.INSATIABLE_FOOD_HUNGER_TICKS);
		assertEquals("food hunger amplifier", 1, ManipulationStatusRules.INSATIABLE_FOOD_HUNGER_AMPLIFIER);
		assertEquals("food exhaustion", 8.0F, ManipulationStatusRules.INSATIABLE_FOOD_EXHAUSTION);
	}

	private static void graveDebtDetectsLowHealthCrossing() {
		assertFalse("above threshold not triggered",
				ManipulationStatusRules.crossedGraveDebtThreshold(8.0F, 6.0F, 20.0F));
		assertTrue("crosses threshold",
				ManipulationStatusRules.crossedGraveDebtThreshold(8.0F, 5.0F, 20.0F));
		assertFalse("already below threshold is not a new crossing",
				ManipulationStatusRules.crossedGraveDebtThreshold(4.0F, 3.0F, 20.0F));
		assertEquals("death refund", 250.0D, ManipulationStatusRules.GRAVE_DEBT_DEATH_REFUND);
	}

	private static void ironRetortUsesPlannedGuardValues() {
		assertEquals("retort duration", 60, ManipulationStatusRules.IRON_RETORT_TICKS);
		assertEquals("retort reduction multiplier", 0.5F, ManipulationStatusRules.IRON_RETORT_DAMAGE_MULTIPLIER);
		assertEquals("retort retaliation damage", 4.0F, ManipulationStatusRules.IRON_RETORT_DAMAGE);
	}

	private static void sanguineMagnetismTargetsHostilesOnly() {
		assertTrue("hostile non-owner can be magnetized",
				ManipulationStatusRules.canMagnetize(true, false, false));
		assertFalse("non-hostile ignored",
				ManipulationStatusRules.canMagnetize(false, false, false));
		assertFalse("allied hostile ignored",
				ManipulationStatusRules.canMagnetize(true, true, false));
		assertFalse("owner ignored",
				ManipulationStatusRules.canMagnetize(true, false, true));
		assertEquals("magnetism radius", 8.0D, ManipulationStatusRules.SANGUINE_MAGNETISM_RADIUS);
		assertEquals("magnetism pin radius", 1.3D, ManipulationStatusRules.SANGUINE_MAGNETISM_PIN_RADIUS);
		assertEquals("magnetism duration", 120, ManipulationStatusRules.SANGUINE_MAGNETISM_TICKS);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
