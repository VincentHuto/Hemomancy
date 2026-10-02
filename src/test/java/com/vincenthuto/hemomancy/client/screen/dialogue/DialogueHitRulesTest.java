package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueHitRulesTest {
	@Test
	void partiallyVisibleTopicCannotInterceptFooterOrHeaderClicks() {
		DialogueLayout.Rect viewport = new DialogueLayout.Rect(20, 50, 200, 100);
		DialogueLayout.Rect bottomTopic = new DialogueLayout.Rect(25, 140, 190, 54);
		DialogueLayout.Rect topTopic = new DialogueLayout.Rect(25, 30, 190, 54);

		assertTrue(DialogueHitRules.containsVisible(bottomTopic, viewport, 200, 145));
		assertFalse(DialogueHitRules.containsVisible(bottomTopic, viewport, 200, 175));
		assertTrue(DialogueHitRules.containsVisible(topTopic, viewport, 200, 60));
		assertFalse(DialogueHitRules.containsVisible(topTopic, viewport, 200, 40));
	}

	@Test
	void rejectsScrolledTargetsOutsideContentViewport() {
		DialogueLayout.Rect viewport = new DialogueLayout.Rect(20, 50, 200, 100);

		assertFalse(DialogueHitRules.intersects(new DialogueLayout.Rect(25, 20, 100, 20), viewport));
		assertFalse(DialogueHitRules.intersects(new DialogueLayout.Rect(25, 151, 100, 20), viewport));
		assertTrue(DialogueHitRules.intersects(new DialogueLayout.Rect(25, 140, 100, 20), viewport));
	}
}
