package com.vincenthuto.hemomancy.common.worldgen.arbor;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborOfWillInteractionRulesTest {
	@Test
	void fruitFocusRequiresOwnerUnlockedSkillChamberAndNormalReach() {
		UUID owner = UUID.randomUUID();
		assertTrue(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, true, true, 4.49, 4.5));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, UUID.randomUUID(), true, true, 1.0, 4.5));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, false, true, 1.0, 4.5));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, true, false, 1.0, 4.5));
		assertFalse(ArborOfWillInteractionRules.mayFocusFruit(owner, owner, true, true, 4.51, 4.5));
	}

	@Test
	void trunkAccessStillRequiresOwnerAndChamber() {
		UUID owner = UUID.randomUUID();
		assertTrue(ArborOfWillInteractionRules.mayOpenTree(owner, owner, true));
		assertFalse(ArborOfWillInteractionRules.mayOpenTree(owner, UUID.randomUUID(), true));
		assertFalse(ArborOfWillInteractionRules.mayOpenTree(owner, owner, false));
	}
}
