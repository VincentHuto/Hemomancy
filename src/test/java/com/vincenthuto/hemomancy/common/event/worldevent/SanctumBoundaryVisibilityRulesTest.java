package com.vincenthuto.hemomancy.common.event.worldevent;

public final class SanctumBoundaryVisibilityRulesTest {
	private SanctumBoundaryVisibilityRulesTest() {
	}

	public static void main(String[] args) {
		classifiesBloodlineMembersAsMembers();
		classifiesMundaneNonMembersAsMundaneOutsiders();
		classifiesInitiatedNonMembersAsOutsiders();
		classifiesDegreeSixNonMembersAsRivalElders();
		classifiesUnstainedViewersAsOutsiders();
		prioritizesHostileInsideEffects();
	}

	private static void classifiesBloodlineMembersAsMembers() {
		assertEquals(SanctumBoundaryRelation.MEMBER,
				SanctumBoundaryVisibilityRules.classifyViewer(true, 0),
				"bloodline members should be attuned to their own sanctum");
	}

	private static void classifiesMundaneNonMembersAsMundaneOutsiders() {
		assertEquals(SanctumBoundaryRelation.MUNDANE_OUTSIDER,
				SanctumBoundaryVisibilityRules.classifyViewer(false, 0),
				"non-Harbinger non-members should see a deeper red mundane boundary");
	}

	private static void classifiesInitiatedNonMembersAsOutsiders() {
		assertEquals(SanctumBoundaryRelation.OUTSIDER,
				SanctumBoundaryVisibilityRules.classifyViewer(false, 1),
				"initiated Harbinger non-members should see the hostile omen boundary");
	}

	private static void classifiesDegreeSixNonMembersAsRivalElders() {
		assertEquals(SanctumBoundaryRelation.RIVAL_ELDER,
				SanctumBoundaryVisibilityRules.classifyViewer(false, 6),
				"degree 6 and above non-members should see the muted elder boundary");
	}

	private static void classifiesUnstainedViewersAsOutsiders() {
		assertEquals(SanctumBoundaryRelation.OUTSIDER,
				SanctumBoundaryVisibilityRules.classifyViewer(true, 8, true),
				"Unstained viewers should see the hostile omen boundary even if old data still links them to the sanctum");
		assertEquals(SanctumBoundaryRelation.OUTSIDER,
				SanctumBoundaryVisibilityRules.classifyViewer(false, 8, true),
				"Unstained viewers should see the full outsider boundary instead of the muted elder boundary");
	}

	private static void prioritizesHostileInsideEffects() {
		assertEquals(SanctumBoundaryRelation.OUTSIDER,
				SanctumBoundaryVisibilityRules.strongerInsideEffect(
						SanctumBoundaryRelation.MEMBER, SanctumBoundaryRelation.OUTSIDER),
				"outsider full omen should win over member shimmer");
		assertEquals(SanctumBoundaryRelation.OUTSIDER,
				SanctumBoundaryVisibilityRules.strongerInsideEffect(
						SanctumBoundaryRelation.RIVAL_ELDER, SanctumBoundaryRelation.OUTSIDER),
				"outsider full omen should win over rival elder fog");
		assertEquals(SanctumBoundaryRelation.RIVAL_ELDER,
				SanctumBoundaryVisibilityRules.strongerInsideEffect(
						SanctumBoundaryRelation.MEMBER, SanctumBoundaryRelation.RIVAL_ELDER),
				"rival elder fog should win over member shimmer");
		assertEquals(SanctumBoundaryRelation.MUNDANE_OUTSIDER,
				SanctumBoundaryVisibilityRules.strongerInsideEffect(
						SanctumBoundaryRelation.MEMBER, SanctumBoundaryRelation.MUNDANE_OUTSIDER),
				"mundane outsider shell should win over member shimmer");
		assertEquals(SanctumBoundaryRelation.RIVAL_ELDER,
				SanctumBoundaryVisibilityRules.strongerInsideEffect(
						SanctumBoundaryRelation.MUNDANE_OUTSIDER, SanctumBoundaryRelation.RIVAL_ELDER),
				"rival elder fog should win over mundane outsider shell");
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + ": expected " + expected + " but got " + actual);
		}
	}
}
