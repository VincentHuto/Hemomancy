package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class BloodAbsorptionChannelRulesTest {
	private BloodAbsorptionChannelRulesTest() {
	}

	public static void main(String[] args) {
		fullBloodStillAllowsNonDrainAbsorptionRites();
		defaultChannelLocksMovementUntilSkillUnlocksSlowMovement();
		advancedMovementSkillsReduceAndRemoveTheSlowdown();
		clientInputMultiplierCompensatesForVanillaUseItemSlowdown();
		livingTargetPulseFrequencyImprovesAtDegreeThreeFourAndFive();
		strainTierThresholdsDelayWithToleranceSkill();
		strainDecaysWhenNoLivingTargetWasDrained();
	}

	private static void fullBloodStillAllowsNonDrainAbsorptionRites() {
		assertEquals("active blood can channel while full for brazier and Will rites", true,
				BloodAbsorptionChannelRules.canStartChannel(true));
		assertEquals("inactive blood cannot channel", false,
				BloodAbsorptionChannelRules.canStartChannel(false));
		assertEquals("a full reservoir blocks ordinary living-target drain", false,
				BloodAbsorptionChannelRules.canDrainLivingTarget(true, true, false));
		assertEquals("sated siphon permits living-target drain while full", true,
				BloodAbsorptionChannelRules.canDrainLivingTarget(true, true, true));
		assertEquals("available capacity permits ordinary living-target drain", true,
				BloodAbsorptionChannelRules.canDrainLivingTarget(true, false, false));
	}

	private static void defaultChannelLocksMovementUntilSkillUnlocksSlowMovement() {
		assertDouble("default absorption channel movement lock", 0.0D,
				BloodAbsorptionChannelRules.movementMultiplier(0, 0, false));
		assertDouble("initial movement skill permits slow channeling", 0.25D,
				BloodAbsorptionChannelRules.movementMultiplier(1, 0, false));
	}

	private static void advancedMovementSkillsReduceAndRemoveTheSlowdown() {
		assertDouble("slowdown tier one", 0.40D,
				BloodAbsorptionChannelRules.movementMultiplier(1, 1, false));
		assertDouble("slowdown tier two", 0.55D,
				BloodAbsorptionChannelRules.movementMultiplier(1, 2, false));
		assertDouble("slowdown tier three", 0.70D,
				BloodAbsorptionChannelRules.movementMultiplier(1, 3, false));
		assertDouble("free channel capstone removes movement penalty", 1.0D,
				BloodAbsorptionChannelRules.movementMultiplier(1, 3, true));
	}

	private static void clientInputMultiplierCompensatesForVanillaUseItemSlowdown() {
		assertDouble("locked input stays locked",
				0.0D, BloodAbsorptionChannelRules.clientInputMultiplier(0.0D));
		assertDouble("dragging siphon survives vanilla use-item slowdown",
				1.25D, BloodAbsorptionChannelRules.clientInputMultiplier(0.25D));
		assertDouble("mobile conduit tier three survives vanilla use-item slowdown",
				3.5D, BloodAbsorptionChannelRules.clientInputMultiplier(0.70D));
		assertDouble("unbound siphon cancels vanilla use-item slowdown",
				5.0D, BloodAbsorptionChannelRules.clientInputMultiplier(1.0D));
	}

	private static void livingTargetPulseFrequencyImprovesAtDegreeThreeFourAndFive() {
		assertEquals("base bare pulse interval", 10,
				BloodAbsorptionChannelRules.livingTargetPulseInterval(false, 0));
		assertEquals("base staff pulse interval", 10,
				BloodAbsorptionChannelRules.livingTargetPulseInterval(true, 0));
		assertEquals("first cadence skill", 8,
				BloodAbsorptionChannelRules.livingTargetPulseInterval(true, 1));
		assertEquals("second cadence skill", 6,
				BloodAbsorptionChannelRules.livingTargetPulseInterval(true, 2));
		assertEquals("third cadence skill", 4,
				BloodAbsorptionChannelRules.livingTargetPulseInterval(true, 3));
	}

	private static void strainTierThresholdsDelayWithToleranceSkill() {
		assertEquals("base none threshold", BloodAbsorptionChannelRules.StrainTier.NONE,
				BloodAbsorptionChannelRules.strainTier(99, 0));
		assertEquals("base weakness threshold", BloodAbsorptionChannelRules.StrainTier.WEAKNESS,
				BloodAbsorptionChannelRules.strainTier(100, 0));
		assertEquals("base nausea threshold", BloodAbsorptionChannelRules.StrainTier.NAUSEA,
				BloodAbsorptionChannelRules.strainTier(180, 0));
		assertEquals("base blood poisoning threshold", BloodAbsorptionChannelRules.StrainTier.BLOOD_POISONING,
				BloodAbsorptionChannelRules.strainTier(260, 0));
		assertEquals("tolerance level two delays weakness threshold",
				BloodAbsorptionChannelRules.StrainTier.NONE,
				BloodAbsorptionChannelRules.strainTier(139, 2));
		assertEquals("tolerance level two eventually reaches weakness",
				BloodAbsorptionChannelRules.StrainTier.WEAKNESS,
				BloodAbsorptionChannelRules.strainTier(140, 2));
	}

	private static void strainDecaysWhenNoLivingTargetWasDrained() {
		assertEquals("living drain increases strain", 51,
				BloodAbsorptionChannelRules.nextStrainTicks(50, true));
		assertEquals("nonliving channel decays strain", 48,
				BloodAbsorptionChannelRules.nextStrainTicks(50, false));
		assertEquals("strain never decays below zero", 0,
				BloodAbsorptionChannelRules.nextStrainTicks(1, false));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
