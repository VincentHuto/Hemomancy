package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTheme;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Every theme draws its one flat sprite set; the textured option is gone. */
class DialogueThemeStyleConfigTest {
	@Test
	void everyThemeSelectsItsFlatSprites() {
		assertFrame(DialogueTheme.BLOOD, "hemomancy:dialogue/blood/frame");
		assertFrame(DialogueTheme.UNSTAINED, "hemomancy:dialogue/unstained/frame");
		assertFrame(DialogueTheme.FUNGAL, "hemomancy:dialogue/fungal/frame");
		assertFrame(DialogueTheme.STILL, "hemomancy:dialogue/still/frame");
		assertFrame(DialogueTheme.STILL_MATERIAL, "hemomancy:dialogue/still/frame");
	}

	@Test
	void hubBoxCardsComeFromTheOneCategorySet() {
		assertEquals("hemomancy:dialogue/categories/quests", DialogueThemeStyle.categoryCard(DialogueCategory.QUESTS, false).toString());
		assertEquals("hemomancy:dialogue/categories/inquiries_selected",
				DialogueThemeStyle.categoryCard(DialogueCategory.INQUIRIES, true).toString());
	}

	private static void assertFrame(DialogueTheme requested, String expected) {
		assertEquals(expected, DialogueThemeStyle.forTheme(requested).frameSprite().toString());
	}
}
