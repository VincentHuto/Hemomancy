package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CircusIntroductionRulesTest {
	@Test
	void theCircusIsHiddenBelowTheThirdDegreeAndTeasedAtIt() {
		assertEquals(CircusIntroductionRules.Introduction.HIDDEN, CircusIntroductionRules.introductionFor(2, false));
		assertEquals(CircusIntroductionRules.Introduction.TEASER, CircusIntroductionRules.introductionFor(3, false));
		assertEquals(CircusIntroductionRules.Introduction.UNDISCOVERED, CircusIntroductionRules.introductionFor(4, false));
		assertEquals(CircusIntroductionRules.Introduction.DISCOVERED, CircusIntroductionRules.introductionFor(4, true));
	}

	@Test
	void theWaybillStillStartsAtTheFourthDegree() {
		assertTrue(CircusIntroductionRules.canRequestWaybill(4, false));
		assertFalse(CircusIntroductionRules.canRequestWaybill(3, false));
		assertFalse(CircusIntroductionRules.canRequestWaybill(4, true));
	}
}
