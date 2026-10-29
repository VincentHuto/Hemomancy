package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitationRulesTest {
	@Test
	void theRiteLandsOnTheNodeOnlyWhenItBeatsAndThereIsRoom() {
		assertTrue(VisitationRules.landOnNode(true, true, true));
		assertFalse(VisitationRules.landOnNode(false, true, true), "a node that is gone is not a landing");
		assertFalse(VisitationRules.landOnNode(true, false, true), "something built on the node");
		assertFalse(VisitationRules.landOnNode(true, true, false), "a roof over the node");
	}
}
