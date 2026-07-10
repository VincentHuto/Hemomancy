package com.vincenthuto.hemomancy.client.screen.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DialogueHubLayoutTest {
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
