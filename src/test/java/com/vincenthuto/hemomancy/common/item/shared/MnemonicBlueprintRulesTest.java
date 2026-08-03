package com.vincenthuto.hemomancy.common.item.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MnemonicBlueprintRulesTest {
	@Test
	void onlyUnlockedRiteAndCraftingNodesCanBeImprinted() {
		assertTrue(MnemonicBlueprintRules.isEligible(MnemonicBlueprintTarget.Type.CARDINAL_RITE, true));
		assertTrue(MnemonicBlueprintRules.isEligible(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, true));
		assertFalse(MnemonicBlueprintRules.isEligible(MnemonicBlueprintTarget.Type.CARDINAL_RITE, false));
	}

	@Test
	void cueRequiresBothEligibilityAndABlankBlueprint() {
		assertTrue(MnemonicBlueprintRules.shouldShowCue(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, true, true));
		assertFalse(MnemonicBlueprintRules.shouldShowCue(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, true, false));
		assertFalse(MnemonicBlueprintRules.shouldShowCue(MnemonicBlueprintTarget.Type.CARDINAL_RITE, false, true));
	}
}
