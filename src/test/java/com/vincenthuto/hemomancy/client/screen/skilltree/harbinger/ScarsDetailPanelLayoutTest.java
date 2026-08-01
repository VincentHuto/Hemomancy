package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScarsDetailPanelLayoutTest {
	@Test
	void scarDetailsMatchExpandedCraftingAndRitesInspectorWidth() {
		assertEquals(260, ScarsTabView.detailPanelWidth(1200));
		assertEquals(190, ScarsTabView.detailPanelWidth(800));
		assertEquals(220, ScarsTabView.detailPanelWidth(640));
	}
}
