package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecollectionRulesTest {
	@Test
	void onlyTheEighthDegreeTakesARecollectionFromTheArchive() {
		assertTrue(RecollectionRules.mayTake(true, false), "an Apotheos walks the Archive freely");
		assertTrue(RecollectionRules.mayTake(false, true), "a Silentium reaches it through a possessed facsimile");
		assertFalse(RecollectionRules.mayTake(false, false));
	}
}
