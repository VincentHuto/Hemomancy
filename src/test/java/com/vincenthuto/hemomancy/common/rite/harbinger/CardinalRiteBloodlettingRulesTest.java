package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardinalRiteBloodlettingRulesTest {
	@Test
	void degreeOneCanTraceSigilsWithoutAProjectionTool() {
		assertTrue(CardinalRiteBloodlettingRules.canTraceSigil(1, CardinalRitePhase.INSCRIPTION));
		assertTrue(CardinalRiteBloodlettingRules.canTraceSigil(1, CardinalRitePhase.ORDEAL));
		assertTrue(CardinalRiteBloodlettingRules.canTraceSigil(1, CardinalRitePhase.STILL_INTERVAL));
		assertFalse(CardinalRiteBloodlettingRules.canTraceSigil(1, CardinalRitePhase.CONSECRATION));
		assertFalse(CardinalRiteBloodlettingRules.canTraceSigil(2, CardinalRitePhase.ORDEAL));
	}

	@Test
	void oneSafeBloodlettingStrokeFillsOneSigilNode() {
		assertEquals(50, CardinalRiteBloodlettingRules.offeringMl());
		assertEquals(18.0F, CardinalRiteBloodlettingRules.healthAfterStroke(20.0F));
		assertTrue(CardinalRiteBloodlettingRules.canOffer(20.0F));
		assertFalse(CardinalRiteBloodlettingRules.canOffer(2.0F));
	}

	@Test
	void degreeOneRepairsBoundaryDamageByDirectBloodletting() {
		assertTrue(CardinalRiteBloodlettingRules.canRepairBoundaryDirectly(1));
		assertFalse(CardinalRiteBloodlettingRules.canRepairBoundaryDirectly(2));
	}
}
