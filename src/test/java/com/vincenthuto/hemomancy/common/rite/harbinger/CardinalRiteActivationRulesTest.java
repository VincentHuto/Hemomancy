package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

public final class CardinalRiteActivationRulesTest {
	@Test
	void templeInitiationDoesNotUseTheLegacyFormationOrStaffTriggers() {
		assertFalse(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.SANGUINE_FORMATION_BLOCK_USE,
				false, 0, "temple_medium"), "legacy formation bypass");
		assertFalse(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE,
				false, 0, "temple_medium"), "premature living staff block use");
	}

	@Test
	void authoredHematicMediumRitesBeginFromTheCardinalFocus() {
		assertTrue(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.HEMATIC_MEDIUM_BLOCK_USE,
				false, 1, "hematic_medium"), "iron medium focus use");
		assertFalse(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE,
				false, 1, "hematic_medium"), "staff bypass");
	}

	@Test
	void authoredStaffRitesRequireLivingStaffActivation() {
		assertTrue(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE,
				false, 2, "living_staff"),
				"living staff block use");
		assertFalse(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.HEMATIC_MEDIUM_BLOCK_USE,
				false, 2, "living_staff"), "iron medium bypass");
	}

	@Test
	void unstainedRitesKeepTheirExistingKeyActivation() {
		assertTrue(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.BLOOD_CRAFTING_KEY, true, 1),
				"unstained blood crafting key");
		assertFalse(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE, true, 1),
				"unstained living staff use");
		assertFalse(CardinalRiteActivationRules.mayInitiate(
				CardinalRiteActivationRules.Trigger.SANGUINE_FORMATION_BLOCK_USE, true, 0),
				"unstained sanguine formation use");
	}

	@Test
	void formationIsConsumedOnlyWhenItActuallyStartsARite() {
		assertTrue(CardinalRiteActivationRules.ActivationAttempt.STARTED
				.shouldConsumeActivator(false), "successful survival activation");
		assertFalse(CardinalRiteActivationRules.ActivationAttempt.HANDLED
				.shouldConsumeActivator(false), "rejected activation");
		assertFalse(CardinalRiteActivationRules.ActivationAttempt.NOT_HANDLED
				.shouldConsumeActivator(false), "unmatched activation");
		assertFalse(CardinalRiteActivationRules.ActivationAttempt.STARTED
				.shouldConsumeActivator(true), "creative activation");
	}

	@Test
	void solidFlatPatternsUseTheirCenterBlock() {
		String[][] pattern = {
				{"CRC"},
				{"RGR"},
				{"CRC"}
		};

		assertEquals(new CardinalRiteActivationRules.Cell(1, 0, 1),
				CardinalRiteActivationRules.activationCell(pattern),
				"flat center activation");
	}

	@Test
	void hollowCentersUseNearestOccupiedBlockOnLowestLayer() {
		String[][] pattern = {
				{"   ", "AAA"},
				{" B ", "A A"},
				{"   ", "ACA"}
		};

		assertEquals(new CardinalRiteActivationRules.Cell(0, 1, 1),
				CardinalRiteActivationRules.activationCell(pattern),
				"hollow altar activation");
	}

	private static void assertEquals(Object expected, Object actual, String label) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) throw new AssertionError(label + " should be allowed");
	}

	private static void assertFalse(boolean value, String label) {
		if (value) throw new AssertionError(label + " should be denied");
	}
}
