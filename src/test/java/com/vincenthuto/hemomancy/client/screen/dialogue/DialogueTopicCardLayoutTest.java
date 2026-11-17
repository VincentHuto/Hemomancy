package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

	@Test
	void aCardGrowsALineForEveryExtraTitleOrSummaryLine() {
		assertEquals(44, DialogueTopicCardLayout.height(44, 2));
		assertEquals(44, DialogueTopicCardLayout.height(44, 1, 2));
		assertEquals(56, DialogueTopicCardLayout.height(44, 2, 2));
		assertEquals(68, DialogueTopicCardLayout.height(44, 1, 4));
		assertEquals(78, DialogueTopicCardLayout.height(54, 2, 3));
	}

	@Test
	void aWrappedTitlePushesTheSummaryDownAndBothStayInsideTheCard() {
		int height = DialogueTopicCardLayout.height(44, 2, 3);
		DialogueLayout.Rect card = new DialogueLayout.Rect(14, 100, 292, height);
		DialogueTopicCardLayout one = DialogueTopicCardLayout.calculate(new DialogueLayout.Rect(14, 100, 292, 44), false);
		DialogueTopicCardLayout two = DialogueTopicCardLayout.calculate(card, true, 2);

		assertEquals(one.summary().y() + DialogueTopicCardLayout.LINE, two.summary().y());
		assertTrue(two.title().bottom() <= two.summary().y(), "the title's second line clears the summary");
		assertTrue(two.summary().y() + 3 * DialogueTopicCardLayout.LINE <= card.bottom() + 4,
				"three summary lines fit under a two-line title");
		assertEquals(DialogueTopicCardLayout.titleWidth(292, true), two.title().width());
		assertEquals(DialogueTopicCardLayout.titleWidth(292, false), one.title().width());
	}
}
