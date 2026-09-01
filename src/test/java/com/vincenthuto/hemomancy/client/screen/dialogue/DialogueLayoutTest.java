package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueLayoutTest {
	@Test
	void wideScreensUseCompactLeftSidebar() {
		DialogueLayout layout = DialogueLayout.calculate(854, 480);

		assertEquals(320, layout.panel().width());
		assertEquals(464, layout.panel().height());
		assertEquals(2, layout.categoryColumns());
		assertEquals(8, layout.panel().x());
		assertEquals(8, layout.panel().y());
		assertTrue(layout.content().bottom() + DialogueLayout.CONTENT_FOOTER_GAP <= layout.footer().y());
		assertTrue(layout.footer().bottom() <= layout.panel().bottom());
	}

	@Test
	void narrowScreensRemainInsideEightPixelMargin() {
		DialogueLayout layout = DialogueLayout.calculate(360, 240);

		assertEquals(320, layout.panel().width());
		assertEquals(224, layout.panel().height());
		assertEquals(2, layout.categoryColumns());
		assertEquals(8, layout.panel().x());
		assertTrue(layout.panel().bottom() <= 232);
	}
}
