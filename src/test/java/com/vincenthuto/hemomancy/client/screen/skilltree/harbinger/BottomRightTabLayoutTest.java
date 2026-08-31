package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BottomRightTabLayoutTest {
	@Test
	void presentsPrimaryTabsInTheRequestedOrder() {
		assertEquals(List.of("Skills", "Tendencies", "Rites", "Crafting", "Materials"),
				HarbingerProgressScreen.topTabLabels(4));
	}

	@Test
	void groupsSummonsBesideBestiaryAtTheBottomRight() {
		assertEquals(List.of("Summons", "Bestiary"),
				HarbingerProgressScreen.bottomRightTabLabels(2));
	}

	@Test
	void keepsTheLastTabRightmostAndPlacesEarlierTabsBesideIt() {
		List<BottomRightTabLayout.Bounds> tabs = BottomRightTabLayout.layout(
				16, 16, 300, 200, 4, 16, List.of(60, 64));

		assertEquals(new BottomRightTabLayout.Bounds(184, 196, 60, 16), tabs.get(0));
		assertEquals(new BottomRightTabLayout.Bounds(248, 196, 64, 16), tabs.get(1));
	}
}
