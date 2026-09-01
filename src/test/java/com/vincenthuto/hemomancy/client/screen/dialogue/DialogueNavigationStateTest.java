package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DialogueNavigationStateTest {
	@Test
	void hubCategoryNodeBackStackIsDeterministic() {
		DialogueNavigationState state = DialogueNavigationState.hub();

		state.openCategory(DialogueCategory.LORE);
		state.openNode("rites");

		assertEquals(DialogueNavigationState.View.NODE, state.view());
		assertEquals("rites", state.nodeId());
		assertTrue(state.back());
		assertEquals(DialogueNavigationState.View.CATEGORY, state.view());
		assertTrue(state.back());
		assertEquals(DialogueNavigationState.View.HUB, state.view());
		assertFalse(state.back());
	}

	@Test
	void focusWrapsAcrossAvailableEntries() {
		DialogueNavigationState state = DialogueNavigationState.hub();

		state.moveFocus(-1, 4);
		assertEquals(3, state.focusIndex());
		state.moveFocus(1, 4);
		assertEquals(0, state.focusIndex());
	}

	@Test
	void explicitRootNavigationReturnsDirectlyToHub() {
		DialogueNavigationState state = DialogueNavigationState.hub();
		state.openCategory(DialogueCategory.QUESTS);
		state.openNode("assignment");

		state.toHub();

		assertEquals(DialogueNavigationState.View.HUB, state.view());
		assertEquals(null, state.category());
		assertEquals(null, state.nodeId());
	}
}
