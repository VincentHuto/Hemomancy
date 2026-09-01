package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueTopicCardLayoutTest {
	@Test
	void unreadMarkerHasDedicatedSpaceAfterTheTitle() {
		DialogueLayout.Rect card = new DialogueLayout.Rect(14, 100, 292, 44);
		DialogueTopicCardLayout layout = DialogueTopicCardLayout.calculate(card, true);

		assertTrue(layout.title().right() + DialogueTopicCardLayout.TEXT_STATUS_GAP
				<= layout.topStatus().x());
		assertTrue(layout.topStatus().right() <= card.right());
	}
}
