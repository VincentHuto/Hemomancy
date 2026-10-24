package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BottomRightTabLayoutTest {
	@Test
	void presentsPrimaryTabsInTheRequestedOrder() {
		assertEquals(List.of("Skills", "Tendencies", "Rites & Crafts", "Materials"),
				HarbingerProgressScreen.topTabLabelKeys(4).stream().map(EnglishText::of).toList());
	}

	@Test
	void groupsSummonsBesideBestiaryAtTheBottomRight() {
		assertEquals(List.of("Summons", "Bestiary"),
				HarbingerProgressScreen.bottomRightTabLabelKeys(2).stream().map(EnglishText::of).toList());
	}

	@Test
	void keepsTheLastTabRightmostAndPlacesEarlierTabsBesideIt() {
		List<BottomRightTabLayout.Bounds> tabs = BottomRightTabLayout.layout(
				16, 16, 300, 200, 4, 16, List.of(60, 64));

		assertEquals(new BottomRightTabLayout.Bounds(184, 196, 60, 16), tabs.get(0));
		assertEquals(new BottomRightTabLayout.Bounds(248, 196, 64, 16), tabs.get(1));
	}
}
