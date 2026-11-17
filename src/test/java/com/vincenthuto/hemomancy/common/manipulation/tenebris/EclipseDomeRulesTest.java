package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Blood Eclipse's carried dome: how it grows with holding, and what its flash is worth. */
class EclipseDomeRulesTest {
	@Test
	void theDomeGrowsFromTwoBlocksToEightOverThreeSeconds() {
		assertEquals(2.0D, EclipseDomeRules.radius(0), 1.0E-9D);
		assertEquals(5.0D, EclipseDomeRules.radius(30), 1.0E-9D);
		assertEquals(8.0D, EclipseDomeRules.radius(60), 1.0E-9D);
		assertEquals(8.0D, EclipseDomeRules.radius(600), 1.0E-9D, "the dome grew past its limit");
		assertEquals(2.0D, EclipseDomeRules.radius(-5), 1.0E-9D);
	}

	@Test
	void theFlashHitsHarderTheWiderTheDome() {
		assertEquals(3.0F, EclipseDomeRules.flashDamage(0), 1.0E-6F);
		assertEquals(5.5F, EclipseDomeRules.flashDamage(30), 1.0E-6F);
		assertEquals(8.0F, EclipseDomeRules.flashDamage(60), 1.0E-6F);
		assertEquals(8.0F, EclipseDomeRules.flashDamage(6000), 1.0E-6F);
	}

	@Test
	void aBodyIsInsideOnlyOutToTheDomesEdge() {
		assertTrue(EclipseDomeRules.inside(1.9D, 0));
		assertFalse(EclipseDomeRules.inside(2.1D, 0));
		assertTrue(EclipseDomeRules.inside(7.9D, 60));
		assertFalse(EclipseDomeRules.inside(8.1D, 60));
	}
}
