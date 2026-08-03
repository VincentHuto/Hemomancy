package com.vincenthuto.hemomancy.common.item.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MnemonicBlueprintStackingTest {
	@Test
	void componentFreeBlankBlueprintsUseTheNormalSixtyFourItemStackLimit() {
		assertEquals(64, MnemonicBlueprintStacking.maxStackSize(false));
	}

	@Test
	void imprintedBlueprintsRemainUnstackable() {
		assertEquals(1, MnemonicBlueprintStacking.maxStackSize(true));
	}
}
