package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuickCasterRulesTest {
	@Test
	void enabledQuickSelectionCasts() {
		assertTrue(QuickCasterRules.shouldCastOnSelect(true, EnumManipulationType.QUICK, false, false, false));
	}

	@Test
	void disabledSkillOnlySelects() {
		assertFalse(QuickCasterRules.shouldCastOnSelect(false, EnumManipulationType.QUICK, false, false, false));
	}

	@Test
	void heldAndToggledTypesStillWaitForTheCastKey() {
		for (EnumManipulationType type : new EnumManipulationType[] {
				EnumManipulationType.CHARGED, EnumManipulationType.CONTINUOUS, EnumManipulationType.PASSIVE }) {
			assertFalse(QuickCasterRules.shouldCastOnSelect(true, type, false, false, false), type.name());
		}
	}

	@Test
	void staffFormsAndConjurationsAreNeverCastOnSelect() {
		// Selection already applies a staff form; a cast would toggle it back off.
		assertFalse(QuickCasterRules.shouldCastOnSelect(true, EnumManipulationType.QUICK, true, false, false));
		// A conjuration cast dispels whatever conjured item is held.
		assertFalse(QuickCasterRules.shouldCastOnSelect(true, EnumManipulationType.QUICK, false, true, false));
	}

	@Test
	void coolingManipulationIsSelectedQuietly() {
		assertFalse(QuickCasterRules.shouldCastOnSelect(true, EnumManipulationType.QUICK, false, false, true));
	}
}
