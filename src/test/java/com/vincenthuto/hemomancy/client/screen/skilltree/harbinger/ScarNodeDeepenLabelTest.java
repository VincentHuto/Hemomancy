package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScarNodeDeepenLabelTest {
	@Test
	void formatsWholeDeepenAmountsAsCompactPositiveValues() {
		assertEquals("+1", ScarsTabView.deepenAmountLabel(1.0f));
		assertEquals("+3", ScarsTabView.deepenAmountLabel(3.0f));
	}

	@Test
	void retainsFractionalDeepenAmountsWhenPresent() {
		assertEquals("+1.5", ScarsTabView.deepenAmountLabel(1.5f));
	}
}
