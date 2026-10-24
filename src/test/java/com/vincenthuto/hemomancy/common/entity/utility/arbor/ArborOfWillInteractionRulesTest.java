package com.vincenthuto.hemomancy.common.entity.utility.arbor;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborOfWillInteractionRulesTest {
	@Test
	void anyLabelledFruitCanBeFocusedByItsOwnerInTheChamber() {
		UUID owner = UUID.randomUUID();
		double labelled = ArborOfWillInteractionRules.FRUIT_TARGET_REACH;
		assertTrue(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, true, true, labelled));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, UUID.randomUUID(), true, true, 1.0));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, false, true, 1.0));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, true, false, 1.0),
				"fruit hidden above the owner's Degree is never labelled, so it cannot be focused");
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, true, true,
				ArborOfWillInteractionRules.FRUIT_SERVER_REACH + .01));
	}

	@Test
	void serverAllowsMovementSlackBeyondTheLabelReach() {
		assertTrue(ArborOfWillInteractionRules.FRUIT_SERVER_REACH > ArborOfWillInteractionRules.FRUIT_TARGET_REACH);
	}

	@Test
	void trunkAccessStillRequiresOwnerAndChamber() {
		UUID owner = UUID.randomUUID();
		assertTrue(ArborOfWillInteractionRules.mayOpenTree(owner, owner, true));
		assertFalse(ArborOfWillInteractionRules.mayOpenTree(owner, UUID.randomUUID(), true));
		assertFalse(ArborOfWillInteractionRules.mayOpenTree(owner, owner, false));
	}
}
