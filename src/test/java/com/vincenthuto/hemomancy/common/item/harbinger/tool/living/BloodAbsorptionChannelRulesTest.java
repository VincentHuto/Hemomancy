package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

public final class BloodAbsorptionChannelRulesTest {
	private BloodAbsorptionChannelRulesTest() {
	}

	@Test
	void allChecksPass() {
		fullBloodStillAllowsNonDrainAbsorptionRites();
		defaultChannelLocksMovementUntilSkillUnlocksSlowMovement();
		advancedMovementSkillsReduceAndRemoveTheSlowdown();
		clientInputMultiplierCompensatesForVanillaUseItemSlowdown();
		livingTargetPulseStaysAtTheHurtCooldownAndCadenceRaisesTheDrain();
		strainTierThresholdsDelayWithToleranceSkill();
		strainCountsOnePerTickOfDrinkingAndEasesTwoPerTickOtherwise();
	}

	private static void fullBloodStillAllowsNonDrainAbsorptionRites() {
		assertEquals("active blood can channel while full for brazier and Will rites", true,
				BloodAbsorptionChannelRules.canStartChannel(true));
		assertEquals("inactive blood cannot channel", false,
				BloodAbsorptionChannelRules.canStartChannel(false));
		assertEquals("CQ-9: a full body still draws a living target (for its Temper)", true,
				BloodAbsorptionChannelRules.canDrainLivingTarget(true));
		assertEquals("inactive blood draws nothing", false,
				BloodAbsorptionChannelRules.canDrainLivingTarget(false));
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

	private static void livingTargetPulseStaysAtTheHurtCooldownAndCadenceRaisesTheDrain() {
		assertEquals("pulse interval matches the 10-tick hurt cooldown", 10,
				BloodAbsorptionChannelRules.livingTargetPulseInterval());
		assertDouble("untrained drain", 1.0D, BloodAbsorptionChannelRules.cadenceDrainMultiplier(0));
		assertDouble("first cadence skill drains as the old 8-tick pulse promised", 1.25D,
				BloodAbsorptionChannelRules.cadenceDrainMultiplier(1));
		assertDouble("second cadence skill drains as the old 6-tick pulse promised", 10.0D / 6.0D,
				BloodAbsorptionChannelRules.cadenceDrainMultiplier(2));
		assertDouble("third cadence skill drains as the old 4-tick pulse promised", 2.5D,
				BloodAbsorptionChannelRules.cadenceDrainMultiplier(3));
		assertDouble("levels past the last skill change nothing", 2.5D,
				BloodAbsorptionChannelRules.cadenceDrainMultiplier(7));
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

	private static void strainCountsOnePerTickOfDrinkingAndEasesTwoPerTickOtherwise() {
		// Five seconds of drinking reaches Weakness; the old arithmetic sat at 0 or 1 forever.
		BloodAbsorptionChannelRules.Strain strain = BloodAbsorptionChannelRules.Strain.NONE;
		for (long tick = 1000; tick < 1100; tick++) {
			strain = BloodAbsorptionChannelRules.nextStrain(strain, tick, (tick - 1000) % 10 == 0);
		}
		assertEquals("one strain per tick of continuous drinking", 100, strain.ticks());
		assertEquals("five seconds of drinking reaches weakness", BloodAbsorptionChannelRules.StrainTier.WEAKNESS,
				BloodAbsorptionChannelRules.strainTier(strain.ticks(), 0));

		BloodAbsorptionChannelRules.Strain paused = BloodAbsorptionChannelRules.nextStrain(strain, 1109, false);
		assertEquals("ticks after the last pulse's cover ease two each", 80, paused.ticks());

		BloodAbsorptionChannelRules.Strain away = BloodAbsorptionChannelRules.nextStrain(strain, 1200, false);
		assertEquals("time out of the channel eases strain too", 0, away.ticks());

		BloodAbsorptionChannelRules.Strain blocks = strain;
		for (long tick = 1100; tick < 1105; tick++) {
			blocks = BloodAbsorptionChannelRules.nextStrain(blocks, tick, false);
		}
		assertEquals("draining blocks or Wills is not drinking from a creature", 90, blocks.ticks());
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
