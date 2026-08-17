package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingUpkeepRulesTest {
	private MorphlingUpkeepRulesTest() {
	}

	public static void main(String[] args) {
		zeroCostMorphlingsUseConfiguredBaselineDrain();
		disabledUpkeepDoesNotDrainPaidMorphlings();
		positiveCostsScaleConfiguredUpkeep();
		activePuppetWillIncreasesMorphlingUpkeepOnce();
		interferenceBloodDoesNotAccelerateBonding();
		equippedPowersRemainScheduledWithoutUpkeep();
		bloodBondingUsesEscalatingStageQuotas();
		bloodBondingResetsProgressWhenTheStageChanges();
		disabledUpkeepBypassesTheBloodGate();
		enzymeSelectionPreservesUnneededInputs();
		insufficientEnzymesCannotStartIncubation();
		incubationTargetsExactlyOneNewStage();
	}

	private static void zeroCostMorphlingsUseConfiguredBaselineDrain() {
		assertClose("zero-cost upkeep uses baseline", 0.5D,
				MorphlingUpkeepRules.upkeepAmount(true, 0.5D, 0));
	}

	private static void disabledUpkeepDoesNotDrainPaidMorphlings() {
		assertClose("disabled upkeep", 0.0D,
				MorphlingUpkeepRules.upkeepAmount(false, 0.5D, 100));
	}

	private static void positiveCostsScaleConfiguredUpkeep() {
		assertClose("positive cost scales upkeep", 1.0D,
				MorphlingUpkeepRules.upkeepAmount(true, 0.5D, 100));
	}

	private static void activePuppetWillIncreasesMorphlingUpkeepOnce() {
		assertClose("active puppet will raises morphling upkeep", 1.25D,
				MorphlingUpkeepRules.withPuppetInterference(1.0D, true));
		assertClose("no puppet leaves morphling upkeep unchanged", 1.0D,
				MorphlingUpkeepRules.withPuppetInterference(1.0D, false));
		assertClose("zero upkeep remains zero", 0.0D,
				MorphlingUpkeepRules.withPuppetInterference(0.0D, true));
	}

	private static void interferenceBloodDoesNotAccelerateBonding() {
		assertClose("bonding receives only ordinary upkeep", 1.0D,
				MorphlingUpkeepRules.bondingCredit(1.25D, 1.0D));
		assertClose("partial payment cannot create bonding credit", 0.5D,
				MorphlingUpkeepRules.bondingCredit(0.5D, 1.0D));
	}

	private static void equippedPowersRemainScheduledWithoutUpkeep() {
		assertTrue("equipped interval schedules powers",
				MorphlingUpkeepRules.shouldRunEquippedTick(true, true));
		assertFalse("missing morphling does not schedule powers",
				MorphlingUpkeepRules.shouldRunEquippedTick(false, true));
		assertFalse("off-interval tick does not schedule powers",
				MorphlingUpkeepRules.shouldRunEquippedTick(true, false));
	}

	private static void bloodBondingUsesEscalatingStageQuotas() {
		assertClose("unfed is ungated", 0.0D,
				MorphlingBloodBondingRules.requiredBlood(0, 50.0D, 100.0D, 200.0D));
		assertClose("fledgling quota", 50.0D,
				MorphlingBloodBondingRules.requiredBlood(1, 50.0D, 100.0D, 200.0D));
		assertClose("developing quota", 100.0D,
				MorphlingBloodBondingRules.requiredBlood(2, 50.0D, 100.0D, 200.0D));
		assertClose("mature quota", 200.0D,
				MorphlingBloodBondingRules.requiredBlood(3, 50.0D, 100.0D, 200.0D));
	}

	private static void bloodBondingResetsProgressWhenTheStageChanges() {
		MorphlingBloodBondingRules.Progress sameStage = MorphlingBloodBondingRules.recordAbsorption(
				1, 12.5D, 1, 0.5D, 50.0D);
		assertEquals("same-stage progress accumulates", 1, sameStage.stage());
		assertClose("actual drain is recorded", 13.0D, sameStage.absorbed());

		MorphlingBloodBondingRules.Progress changedStage = MorphlingBloodBondingRules.recordAbsorption(
				1, 49.5D, 2, 0.5D, 100.0D);
		assertEquals("new stage marker", 2, changedStage.stage());
		assertClose("old-stage blood does not carry", 0.5D, changedStage.absorbed());
	}

	private static void disabledUpkeepBypassesTheBloodGate() {
		assertTrue("disabled upkeep cannot soft-lock incubation",
				MorphlingBloodBondingRules.isReady(3, 0.0D, false, 50.0D, 100.0D, 200.0D));
		assertFalse("enabled upkeep enforces quota",
				MorphlingBloodBondingRules.isReady(3, 199.5D, true, 50.0D, 100.0D, 200.0D));
		assertTrue("completed quota unlocks incubation",
				MorphlingBloodBondingRules.isReady(3, 200.0D, true, 50.0D, 100.0D, 200.0D));
	}

	private static void enzymeSelectionPreservesUnneededInputs() {
		assertEquals("least excess wins", 0b0011,
				MorphlingBloodBondingRules.selectEnzymeSlots(new double[] { 7.5D, 5.0D, 10.0D, 10.0D }, 12.0D));
		assertEquals("ties preserve later slots", 0b0001,
				MorphlingBloodBondingRules.selectEnzymeSlots(new double[] { 10.0D, 10.0D, 10.0D, 10.0D }, 10.0D));
	}

	private static void insufficientEnzymesCannotStartIncubation() {
		assertEquals("insufficient power has no selection", 0,
				MorphlingBloodBondingRules.selectEnzymeSlots(new double[] { 5.0D, 5.0D, 0.0D, 0.0D }, 15.0D));
	}

	private static void incubationTargetsExactlyOneNewStage() {
		float[] thresholds = { 0.0F, 10.0F, 30.0F, 60.0F, 100.0F, 100.0F };
		assertClose("unfed targets fledgling only", 10.0D,
				MorphlingBloodBondingRules.nextEnzymeTarget(0, thresholds));
		assertClose("developing targets mature only", 60.0D,
				MorphlingBloodBondingRules.nextEnzymeTarget(2, thresholds));
		assertClose("apex has no enzyme target", -1.0D,
				MorphlingBloodBondingRules.nextEnzymeTarget(4, thresholds));
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

	private static void assertClose(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
