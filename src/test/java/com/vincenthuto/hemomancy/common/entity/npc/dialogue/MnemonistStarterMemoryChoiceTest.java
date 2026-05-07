package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public final class MnemonistStarterMemoryChoiceTest {
	private MnemonistStarterMemoryChoiceTest() {
	}

	public static void main(String[] args) {
		eventIdsResolveToStarterChoices();
		unknownEventDoesNotResolve();
		claimRulesRequireDegreeOneHarbingerState();
	}

	private static void eventIdsResolveToStarterChoices() {
		assertChoice("blood shot event",
				"mnemonist_grant_crude_blood_shot",
				"blood_shot",
				"crude_memory_blood_shot");
		assertChoice("blood rush event",
				"mnemonist_grant_crude_blood_rush",
				"blood_rush",
				"crude_memory_blood_rush");
		assertChoice("deadly gaze event",
				"mnemonist_grant_crude_deadly_gaze",
				"deadly_gaze",
				"crude_memory_deadly_gaze");
	}

	private static void unknownEventDoesNotResolve() {
		assertFalse("unknown event",
				MnemonistStarterMemoryChoice.fromEventId("mnemonist_grant_crude_void_shroud").isPresent());
	}

	private static void claimRulesRequireDegreeOneHarbingerState() {
		assertTrue("degree one can claim",
				MnemonistStarterMemoryChoice.canClaim(1, false, false, false));
		assertFalse("degree zero cannot claim",
				MnemonistStarterMemoryChoice.canClaim(0, false, false, false));
		assertFalse("claimed cannot claim again",
				MnemonistStarterMemoryChoice.canClaim(1, false, false, true));
		assertFalse("purifying cannot claim",
				MnemonistStarterMemoryChoice.canClaim(1, true, false, false));
		assertFalse("clarity cannot claim",
				MnemonistStarterMemoryChoice.canClaim(1, false, true, false));
	}

	private static void assertChoice(String label, String eventId, String manipName, String itemName) {
		MnemonistStarterMemoryChoice choice = MnemonistStarterMemoryChoice.fromEventId(eventId)
				.orElseThrow(() -> new AssertionError(label + ": missing choice"));
		assertEquals(label + " manip", manipName, choice.manipulationName());
		assertEquals(label + " item", itemName, choice.itemName());
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
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
}
