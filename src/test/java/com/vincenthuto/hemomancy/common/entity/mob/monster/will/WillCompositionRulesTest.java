package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public final class WillCompositionRulesTest {
	private WillCompositionRulesTest() {
	}

	public static void main(String[] args) {
		degreeGatesMapToExpectedTiers();
		compositionsMatchThreatRoles();
		targetChoicePrefersHighestDegree();
	}

	private static void degreeGatesMapToExpectedTiers() {
		assertEquals("pre degree four has no ambush tier", 0, WillCompositionRules.tierFor(3, false));
		assertEquals("degree four starts tier one", 1, WillCompositionRules.tierFor(4, false));
		assertEquals("degree five enters tier two", 2, WillCompositionRules.tierFor(5, false));
		assertEquals("degree six enters tier three", 3, WillCompositionRules.tierFor(6, false));
		assertEquals("degree seven without communion remains tier three", 3, WillCompositionRules.tierFor(7, false));
		assertEquals("degree seven with communion enters tier four", 4, WillCompositionRules.tierFor(7, true));
	}

	private static void compositionsMatchThreatRoles() {
		WillCompositionRules.Composition tierOne = WillCompositionRules.compose(1, new Random(1L), 1);
		assertEquals("tier one broken count", 1, tierOne.brokenCount());
		assertFalse("tier one has no sent will", tierOne.sentPresent());
		WillCompositionRules.Composition tierFour = WillCompositionRules.compose(4, new Random(2L), 1);
		assertTrue("tier four has sent proctor", tierFour.sentPresent());
		assertTrue("tier four has escort fodder", tierFour.brokenCount() >= 2);
		assertEquals("tier four escorts use tier two broken stats", 2, tierFour.brokenTier());
		WillCompositionRules.Composition multiplayer = WillCompositionRules.compose(4, new Random(3L), 3);
		assertTrue("nearby players add capped escort pressure", multiplayer.brokenCount() <= tierFour.brokenCount() + 2);
	}

	private static void targetChoicePrefersHighestDegree() {
		WillCompositionRules.PlayerSnapshot low = new WillCompositionRules.PlayerSnapshot(UUID.randomUUID(), 4);
		WillCompositionRules.PlayerSnapshot high = new WillCompositionRules.PlayerSnapshot(UUID.randomUUID(), 7);
		Optional<WillCompositionRules.PlayerSnapshot> chosen = WillCompositionRules.chooseTarget(List.of(low, high));
		assertTrue("target is present", chosen.isPresent());
		assertEquals("highest degree target chosen", high.id(), chosen.get().id());
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

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean condition) {
		if (condition) {
			throw new AssertionError(label);
		}
	}
}
