package com.vincenthuto.hemomancy.client.screen.dialogue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DialogueHitRulesTest {
	@Test
	void rejectsScrolledTargetsOutsideContentViewport() {
		DialogueLayout.Rect viewport = new DialogueLayout.Rect(20, 50, 200, 100);

		assertFalse(DialogueHitRules.intersects(new DialogueLayout.Rect(25, 20, 100, 20), viewport));
		assertFalse(DialogueHitRules.intersects(new DialogueLayout.Rect(25, 151, 100, 20), viewport));
		assertTrue(DialogueHitRules.intersects(new DialogueLayout.Rect(25, 140, 100, 20), viewport));
	}
}
