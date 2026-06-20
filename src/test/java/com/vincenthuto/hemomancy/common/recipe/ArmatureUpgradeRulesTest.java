package com.vincenthuto.hemomancy.common.recipe;

import java.util.List;

public final class ArmatureUpgradeRulesTest {
	private ArmatureUpgradeRulesTest() {
	}

	public static void main(String[] args) {
		slotMappingMatchesArmorOrder();
		localBowlHitMappingMatchesArmorOrder();
		bowlInsertionAndWithdrawalOrdersMatchLifoUse();
		bowlsAcceptOnlyOneItemEach();
		nextInsertableBowlUsesFirstEmptyBowl();
		matchingReagentInAnyBowlCanUpgradeArmorSlot();
		partialCompletionConsumesOnlySuccessfulSlots();
		reagentBaseDegreeAndPersistentGatesBlockPieces();
		armatureTierGatesHighDegreeRecipes();
		insufficientBloodStopsAtTheMatchingPiece();
	}

	private static void slotMappingMatchesArmorOrder() {
		assertEquals("helmet bowl slot", 0, ArmatureUpgradeRules.bowlSlotFor(ArmatureUpgradeRules.ArmatureSlot.HEAD));
		assertEquals("chest bowl slot", 1, ArmatureUpgradeRules.bowlSlotFor(ArmatureUpgradeRules.ArmatureSlot.CHEST));
		assertEquals("legs bowl slot", 2, ArmatureUpgradeRules.bowlSlotFor(ArmatureUpgradeRules.ArmatureSlot.LEGS));
		assertEquals("boots bowl slot", 3, ArmatureUpgradeRules.bowlSlotFor(ArmatureUpgradeRules.ArmatureSlot.FEET));
	}

	private static void localBowlHitMappingMatchesArmorOrder() {
		assertEquals("northwest bowl maps to helmet",
				ArmatureUpgradeRules.ArmatureSlot.HEAD, ArmatureUpgradeRules.bowlSlotForLocalHit(0.25D, 0.25D));
		assertEquals("northeast bowl maps to chest",
				ArmatureUpgradeRules.ArmatureSlot.CHEST, ArmatureUpgradeRules.bowlSlotForLocalHit(0.75D, 0.25D));
		assertEquals("southwest bowl maps to legs",
				ArmatureUpgradeRules.ArmatureSlot.LEGS, ArmatureUpgradeRules.bowlSlotForLocalHit(0.25D, 0.75D));
		assertEquals("southeast bowl maps to feet",
				ArmatureUpgradeRules.ArmatureSlot.FEET, ArmatureUpgradeRules.bowlSlotForLocalHit(0.75D, 0.75D));
	}

	private static void bowlInsertionAndWithdrawalOrdersMatchLifoUse() {
		assertEquals("normal right-click fills armor order",
				List.of(0, 1, 2, 3), ArmatureUpgradeRules.bowlSlotsInInsertionOrder());
		assertEquals("shift right-click withdraws newest bowl first",
				List.of(3, 2, 1, 0), ArmatureUpgradeRules.bowlSlotsInWithdrawalOrder());
	}

	private static void bowlsAcceptOnlyOneItemEach() {
		assertEquals("bowl reagent stack limit", 1, ArmatureUpgradeRules.BOWL_STACK_LIMIT);
		assertEquals("empty bowl takes one item from a held stack",
				1, ArmatureUpgradeRules.insertedCountForBowl(true, 64));
		assertEquals("empty bowl takes one item from a single held item",
				1, ArmatureUpgradeRules.insertedCountForBowl(true, 1));
		assertEquals("occupied bowl never accepts another item",
				0, ArmatureUpgradeRules.insertedCountForBowl(false, 64));
		assertEquals("empty bowl cannot take from empty hand",
				0, ArmatureUpgradeRules.insertedCountForBowl(true, 0));
	}

	private static void nextInsertableBowlUsesFirstEmptyBowl() {
		assertEquals("empty armature inserts into helmet bowl",
				0, ArmatureUpgradeRules.nextInsertableBowlSlot(List.of(false, false, false, false)));
		assertEquals("occupied helmet sends next item to chest bowl",
				1, ArmatureUpgradeRules.nextInsertableBowlSlot(List.of(true, false, false, false)));
		assertEquals("occupied helmet and chest send next item to legs bowl",
				2, ArmatureUpgradeRules.nextInsertableBowlSlot(List.of(true, true, false, false)));
		assertEquals("full armature has no insertable bowl",
				-1, ArmatureUpgradeRules.nextInsertableBowlSlot(List.of(true, true, true, true)));
	}

	private static void partialCompletionConsumesOnlySuccessfulSlots() {
		ArmatureUpgradeRules.ProcessResult result = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.HEAD, 0, false, true, true, 2, 2, 50),
				candidate(ArmatureUpgradeRules.ArmatureSlot.CHEST, 3, true, true, true, 2, 2, 100),
				candidate(ArmatureUpgradeRules.ArmatureSlot.LEGS, 1, true, true, true, 2, 2, 100)
		), 2, 150);

		assertEquals("one piece upgraded", 1, result.upgradedSlots().size());
		assertTrue("chest upgraded", result.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.CHEST));
		assertFalse("helmet skipped", result.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.HEAD));
		assertFalse("legs left for later because blood ran out", result.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.LEGS));
		assertDouble("blood spent", 100, result.bloodSpent());
		assertEquals("actual chest reagent bowl consumed", List.of(3), result.consumedBowlSlots());
	}

	private static void matchingReagentInAnyBowlCanUpgradeArmorSlot() {
		ArmatureUpgradeRules.ProcessResult result = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.HEAD, 3, true, true, true, 2, 2, 50)
		), 2, 100);

		assertEquals("one piece upgraded", 1, result.upgradedSlots().size());
		assertTrue("helmet upgraded from reagent in unrelated bowl",
				result.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.HEAD));
		assertEquals("actual reagent bowl consumed", List.of(3), result.consumedBowlSlots());
		assertDouble("blood spent", 50, result.bloodSpent());
	}

	private static void reagentBaseDegreeAndPersistentGatesBlockPieces() {
		ArmatureUpgradeRules.ProcessResult result = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.HEAD, 0, false, true, true, 2, 2, 50),
				candidate(ArmatureUpgradeRules.ArmatureSlot.CHEST, 1, true, false, true, 2, 2, 50),
				candidate(ArmatureUpgradeRules.ArmatureSlot.LEGS, 2, true, true, true, 3, 2, 50),
				candidate(ArmatureUpgradeRules.ArmatureSlot.FEET, 3, true, true, false, 2, 2, 50)
		), 2, 500);

		assertTrue("no blocked candidates upgrade", result.upgradedSlots().isEmpty());
		assertDouble("no blood spent", 0, result.bloodSpent());
		assertTrue("no bowls consumed", result.consumedBowlSlots().isEmpty());
	}

	private static void armatureTierGatesHighDegreeRecipes() {
		ArmatureUpgradeRules.ProcessResult baseResult = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.HEAD, 0, true, true, true, 5, 5, 50)
		), 5, ArmatureUpgradeRules.ArmatureTier.BASE, 100);

		assertTrue("base armature cannot craft degree 5 upgrades", baseResult.upgradedSlots().isEmpty());

		ArmatureUpgradeRules.ProcessResult vicarResult = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.HEAD, 0, true, true, true, 5, 5, 50),
				candidate(ArmatureUpgradeRules.ArmatureSlot.CHEST, 1, true, true, true, 7, 7, 50)
		), 7, ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED, 500);

		assertEquals("vicar armature crafts degree 5 only", 1, vicarResult.upgradedSlots().size());
		assertTrue("degree 5 upgrade allowed after vicar kit",
				vicarResult.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.HEAD));
		assertFalse("degree 7 upgrade still blocked before cornerstone",
				vicarResult.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.CHEST));

		ArmatureUpgradeRules.ProcessResult monolithicResult = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.CHEST, 1, true, true, true, 7, 7, 50)
		), 7, ArmatureUpgradeRules.ArmatureTier.MONOLITHIC, 100);

		assertTrue("degree 7 upgrade allowed after cornerstone",
				monolithicResult.upgradedSlots().contains(ArmatureUpgradeRules.ArmatureSlot.CHEST));
	}

	private static void insufficientBloodStopsAtTheMatchingPiece() {
		ArmatureUpgradeRules.ProcessResult result = ArmatureUpgradeRules.process(List.of(
				candidate(ArmatureUpgradeRules.ArmatureSlot.HEAD, 3, true, true, true, 2, 2, 200),
				candidate(ArmatureUpgradeRules.ArmatureSlot.CHEST, 0, true, true, true, 2, 2, 50)
		), 2, 100);

		assertTrue("matching helmet blocks later pieces when blood is short", result.upgradedSlots().isEmpty());
		assertEquals("no bowl consumed", 0, result.consumedBowlSlots().size());
		assertDouble("no blood spent", 0, result.bloodSpent());
	}

	private static ArmatureUpgradeRules.Candidate candidate(ArmatureUpgradeRules.ArmatureSlot slot, int bowlSlot,
			boolean baseMatches,
			boolean reagentMatches, boolean persistentGateMatches, int requiredDegree, int playerDegree,
			double bloodCost) {
		return new ArmatureUpgradeRules.Candidate(slot, bowlSlot, baseMatches, reagentMatches,
				persistentGateMatches, requiredDegree, playerDegree, bloodCost);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label + ": expected true");
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label + ": expected false");
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, List<Integer> expected, List<Integer> actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, ArmatureUpgradeRules.ArmatureSlot expected,
			ArmatureUpgradeRules.ArmatureSlot actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
