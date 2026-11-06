package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueHubLayoutTest {
	@Test
	void theImportantTabSitsCenteredAboveLeaveAndClearOfTheCards() {
		for (int[] size : new int[][] {{854, 480}, {640, 360}, {320, 240}, {1920, 1080}}) {
			DialogueLayout shell = DialogueLayout.calculate(size[0], size[1]);
			DialogueHubLayout hub = DialogueHubLayout.calculate(shell);
			var action = hub.important();
			assertTrue(action.y() >= hub.cards().getLast().bottom() + DialogueActionGlow.HALO, "its glow clears the cards at " + size[0]);
			assertEquals(shell.content().x() + shell.content().width() / 2, action.x() + action.width() / 2, 1, "centred at " + size[0]);
			assertEquals(hub.leave().x() + hub.leave().width() / 2, action.x() + action.width() / 2, 1, "over Leave at " + size[0]);
			assertTrue(action.width() + DialogueActionGlow.HALO * 2 <= shell.content().width(), "its glow fits the width at " + size[0]);
		}
		DialogueLayout tall = DialogueLayout.calculate(854, 480);
		var action = DialogueHubLayout.calculate(tall).important();
		assertTrue(action.bottom() + DialogueActionGlow.HALO <= tall.content().bottom(), "with room, its glow stays inside the content");
		assertTrue(action.bottom() <= DialogueHubLayout.calculate(tall).leave().y(), "it is above Leave");
	}

	@Test
	void normalSidebarUsesTwoByTwoCardsAndCenteredCompactLeave() {
		DialogueLayout shell = DialogueLayout.calculate(854, 480);
		DialogueHubLayout hub = DialogueHubLayout.calculate(shell);

		assertEquals(4, hub.cards().size());
		assertEquals(hub.cards().get(0).y(), hub.cards().get(1).y());
		assertEquals(hub.cards().get(2).y(), hub.cards().get(3).y());
		assertEquals(hub.cards().get(0).x(), hub.cards().get(2).x());
		assertEquals(hub.cards().get(1).x(), hub.cards().get(3).x());
		assertTrue(hub.cards().get(0).right() < hub.cards().get(1).x());
		assertEquals(shell.footer().x() + (shell.footer().width() - hub.leave().width()) / 2,
				hub.leave().x());
		assertTrue(hub.leave().width() < shell.footer().width() / 2);
	}
}
