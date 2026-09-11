package com.vincenthuto.hemomancy.common.vein;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EarthenVeinDestinationValidationTest {
	@Test
	void missingOrReplacedVeinsArePruned() {
		assertTrue(EarthenVeinDestinationValidation.shouldPrune(false, false, false, false));
		assertTrue(EarthenVeinDestinationValidation.shouldPrune(true, true, true, true));
		assertTrue(EarthenVeinDestinationValidation.shouldPrune(true, false, false, true));
		assertTrue(EarthenVeinDestinationValidation.shouldPrune(true, false, true, false));
	}

	@Test
	void aValidButObstructedVeinRemainsClaimed() {
		assertFalse(EarthenVeinDestinationValidation.shouldPrune(true, false, true, true));
	}
}
