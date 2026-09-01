package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DialogueThemeStyleConfigTest {
	@Test
	void enabledToggleSelectsMaterialSpritesForEveryDialogueTheme() {
		assertFrame(DialogueTheme.BLOOD, true, "hemomancy:dialogue/blood_material/frame");
		assertFrame(DialogueTheme.UNSTAINED, true, "hemomancy:dialogue/unstained_material/frame");
		assertFrame(DialogueTheme.FUNGAL, true, "hemomancy:dialogue/fungal_material/frame");
		assertFrame(DialogueTheme.STILL_MATERIAL, true, "hemomancy:dialogue/still_material/frame");
	}

	@Test
	void disabledToggleSelectsAbstractSpritesForEveryDialogueTheme() {
		assertFrame(DialogueTheme.BLOOD, false, "hemomancy:dialogue/blood/frame");
		assertFrame(DialogueTheme.UNSTAINED, false, "hemomancy:dialogue/unstained/frame");
		assertFrame(DialogueTheme.FUNGAL, false, "hemomancy:dialogue/fungal/frame");
		assertFrame(DialogueTheme.STILL_MATERIAL, false, "hemomancy:dialogue/still/frame");
	}

	@Test
	void globalToggleAlsoSelectsMaterialHubCategoryCards() {
		assertCategoryCard(DialogueCategory.QUESTS, false, true,
				"hemomancy:dialogue/categories_material/quests");
		assertCategoryCard(DialogueCategory.INQUIRIES, true, true,
				"hemomancy:dialogue/categories_material/inquiries_selected");
		assertCategoryCard(DialogueCategory.LORE, false, false,
				"hemomancy:dialogue/categories/lore");
		assertCategoryCard(DialogueCategory.CONVERSATION, true, false,
				"hemomancy:dialogue/categories/conversation_selected");
	}

	private static void assertFrame(DialogueTheme requested, boolean textured, String expected) {
		DialogueThemeStyle style = DialogueThemeStyle.forTheme(requested, textured);
		assertEquals(expected, style.frameSprite().toString());
	}

	private static void assertCategoryCard(DialogueCategory category, boolean selected, boolean textured,
			String expected) {
		assertEquals(expected, DialogueThemeStyle.categoryCard(category, selected, textured).toString());
	}
}
