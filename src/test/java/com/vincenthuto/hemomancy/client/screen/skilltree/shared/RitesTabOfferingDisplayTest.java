package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class RitesTabOfferingDisplayTest {
	@Test
	void formatsOfferingRowsWithTheRequiredCountAndItemName() {
		assertEquals(" x2  Diamond", RitesTabView.offeringRowText(2, "Diamond"));
	}
}
