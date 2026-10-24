package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class SummonsDetailPanelTest {

	@Test
	void panelsEndAboveTheBottomRightTabs() {
		// Bottom-right tabs: 16 px tall, 4 px padding (HarbingerProgressScreen TAB_HEIGHT / TAB_PAD)
		assertEquals(16 + 2 * 4, SummonsTabView.PANEL_BOTTOM_INSET);
		var tabs = BottomRightTabLayout.layout(16, 16, 448, 238, 4, 16, java.util.List.of(55, 57));
		int panelBottom = 16 + 238 - SummonsTabView.PANEL_BOTTOM_INSET;
		for (var tab : tabs) {
			assertEquals(4, tab.y() - panelBottom, "gap between the panels and the tab row");
		}
	}

	@Test
	void detailScrollIsBoundedByTheMeasuredText() {
		SummonsTabState state = new SummonsTabState();
		state.infoContentH = 520;
		state.infoVisibleH = 290;
		state.infoScroll = 1000;
		state.clampInfoScroll();
		assertEquals(230, state.infoScroll);
		assertEquals(230, state.maxInfoScroll());

		state.infoScroll = -14;
		state.clampInfoScroll();
		assertEquals(0, state.infoScroll);
	}

	@Test
	void ringmasterPatternNamesTheCircusSuccessionFinale() {
		assertEquals("Circus Succession finale", EnglishText.render(SummonsTabView.unlockHint(
				PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.RINGMASTER_PATTERN).orElseThrow())));
		assertEquals("Puppetry Cardinal Ordeal", EnglishText.render(SummonsTabView.unlockHint(
				PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.GOREBOUND_HULK).orElseThrow())));
	}
}
