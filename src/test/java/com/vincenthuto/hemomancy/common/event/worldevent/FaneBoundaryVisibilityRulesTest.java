package com.vincenthuto.hemomancy.common.event.worldevent;

public final class FaneBoundaryVisibilityRulesTest {
	private FaneBoundaryVisibilityRulesTest() {
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
		assertEquals(FaneBoundaryRelation.MEMBER,
				FaneBoundaryVisibilityRules.classifyViewer(true, 0),
				"bloodline members should be attuned to their own fane");
	}

	private static void classifiesMundaneNonMembersAsMundaneOutsiders() {
		assertEquals(FaneBoundaryRelation.MUNDANE_OUTSIDER,
				FaneBoundaryVisibilityRules.classifyViewer(false, 0),
				"non-Harbinger non-members should see a deeper red mundane boundary");
	}

	private static void classifiesInitiatedNonMembersAsOutsiders() {
		assertEquals(FaneBoundaryRelation.OUTSIDER,
				FaneBoundaryVisibilityRules.classifyViewer(false, 1),
				"initiated Harbinger non-members should see the hostile omen boundary");
	}

	private static void classifiesDegreeSixNonMembersAsRivalElders() {
		assertEquals(FaneBoundaryRelation.RIVAL_ELDER,
				FaneBoundaryVisibilityRules.classifyViewer(false, 6),
				"degree 6 and above non-members should see the muted elder boundary");
	}

	private static void classifiesUnstainedViewersAsOutsiders() {
		assertEquals(FaneBoundaryRelation.OUTSIDER,
				FaneBoundaryVisibilityRules.classifyViewer(true, 8, true),
				"Unstained viewers should see the hostile omen boundary even if old data still links them to the fane");
		assertEquals(FaneBoundaryRelation.OUTSIDER,
				FaneBoundaryVisibilityRules.classifyViewer(false, 8, true),
				"Unstained viewers should see the full outsider boundary instead of the muted elder boundary");
	}

	private static void prioritizesHostileInsideEffects() {
		assertEquals(FaneBoundaryRelation.OUTSIDER,
				FaneBoundaryVisibilityRules.strongerInsideEffect(
						FaneBoundaryRelation.MEMBER, FaneBoundaryRelation.OUTSIDER),
				"outsider full omen should win over member shimmer");
		assertEquals(FaneBoundaryRelation.OUTSIDER,
				FaneBoundaryVisibilityRules.strongerInsideEffect(
						FaneBoundaryRelation.RIVAL_ELDER, FaneBoundaryRelation.OUTSIDER),
				"outsider full omen should win over rival elder fog");
		assertEquals(FaneBoundaryRelation.RIVAL_ELDER,
				FaneBoundaryVisibilityRules.strongerInsideEffect(
						FaneBoundaryRelation.MEMBER, FaneBoundaryRelation.RIVAL_ELDER),
				"rival elder fog should win over member shimmer");
		assertEquals(FaneBoundaryRelation.MUNDANE_OUTSIDER,
				FaneBoundaryVisibilityRules.strongerInsideEffect(
						FaneBoundaryRelation.MEMBER, FaneBoundaryRelation.MUNDANE_OUTSIDER),
				"mundane outsider shell should win over member shimmer");
		assertEquals(FaneBoundaryRelation.RIVAL_ELDER,
				FaneBoundaryVisibilityRules.strongerInsideEffect(
						FaneBoundaryRelation.MUNDANE_OUTSIDER, FaneBoundaryRelation.RIVAL_ELDER),
				"rival elder fog should win over mundane outsider shell");
	}

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!expected.equals(actual)) {
			throw new AssertionError(message + ": expected " + expected + " but got " + actual);
		}
	}
}
