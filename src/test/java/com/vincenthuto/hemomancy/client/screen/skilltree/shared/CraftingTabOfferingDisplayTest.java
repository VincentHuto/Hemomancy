package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CraftingTabOfferingDisplayTest {
	@Test
	void formatsOfferingRowsWithoutRepeatingTheBrazierHeading() {
		assertEquals(" x1  Glass Bottle", CraftingTabView.offeringRowText(1, "Glass Bottle"));
	}
}
