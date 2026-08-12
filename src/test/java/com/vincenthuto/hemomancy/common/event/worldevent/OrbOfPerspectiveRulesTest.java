package com.vincenthuto.hemomancy.common.event.worldevent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class OrbOfPerspectiveRulesTest {
	@Test
	void activationIsOwnerAwareOneShotAndEncounterExcluded() {
		assertEquals(OrbOfPerspectiveRules.Activation.HANDLED,
				OrbOfPerspectiveRules.activation(true, true, true, true, false, false, 2));
		assertEquals(OrbOfPerspectiveRules.Activation.REJECT_NO_OWNER,
				OrbOfPerspectiveRules.activation(false, false, true, false, false, false, 2));
		assertEquals(OrbOfPerspectiveRules.Activation.REJECT_OUTSIDE_CELL,
				OrbOfPerspectiveRules.activation(false, true, true, true, false, false, 2));
		assertEquals(OrbOfPerspectiveRules.Activation.REJECT_NOT_BEYOND_PLATFORM,
				OrbOfPerspectiveRules.activation(false, true, false, false, false, false, 2));
		assertEquals(OrbOfPerspectiveRules.Activation.REJECT_ENCOUNTER,
				OrbOfPerspectiveRules.activation(false, true, true, false, true, false, 2));
		assertEquals(OrbOfPerspectiveRules.Activation.REJECT_ENCOUNTER,
				OrbOfPerspectiveRules.activation(false, true, true, false, false, true, 2));
		assertEquals(OrbOfPerspectiveRules.Activation.NO_OTHER_THEME,
				OrbOfPerspectiveRules.activation(false, true, true, false, false, false, 1));
		assertEquals(OrbOfPerspectiveRules.Activation.CYCLE,
				OrbOfPerspectiveRules.activation(false, true, true, false, false, false, 3));
	}

	@Test
	void cyclesForwardAndFallsBackFromAnIneligibleOverride() {
		assertEquals(1, OrbOfPerspectiveRules.nextThemeIndex(0, 3));
		assertEquals(0, OrbOfPerspectiveRules.nextThemeIndex(2, 3));
		assertEquals(0, OrbOfPerspectiveRules.nextThemeIndex(-1, 3));
	}

	@Test
	void fullInventoryReturnsOrbBesideOwner() {
		assertEquals(OrbOfPerspectiveRules.ReturnTarget.INVENTORY,
				OrbOfPerspectiveRules.returnTarget(true));
		assertEquals(OrbOfPerspectiveRules.ReturnTarget.BESIDE_OWNER,
				OrbOfPerspectiveRules.returnTarget(false));
	}

	@Test
	void ownerlessOrbReturnsAboveItsCellInsteadOfRemainingBelowTheRescuePlane() {
		OrbOfPerspectiveRules.SafePosition position =
				OrbOfPerspectiveRules.ownerlessReturnPosition(128.0D, 64.0D, -256.0D);
		assertEquals(128.5D, position.x(), 0.0001D);
		assertEquals(65.0D, position.y(), 0.0001D);
		assertEquals(-255.5D, position.z(), 0.0001D);
	}
}
