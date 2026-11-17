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
		assertFrame(DialogueTheme.CIRCUS, "hemomancy:dialogue/circus/frame");
	}

	/** HN-9: the Circus theme shows one troupe crest for every speaker, and the theme is the enum's last value. */
	@Test
	void theCircusShowsOneTroupeCrest() {
		assertEquals("hemomancy:dialogue/crests/circus",
				DialogueThemeStyle.crest(DialogueTheme.CIRCUS, com.vincenthuto.hemomancy.Hemomancy.rloc("circus_acrobat")).toString());
		assertEquals("hemomancy:dialogue/crests/vicar",
				DialogueThemeStyle.crest(DialogueTheme.BLOOD, com.vincenthuto.hemomancy.Hemomancy.rloc("vicar")).toString());
		DialogueTheme[] themes = DialogueTheme.values();
		assertEquals(DialogueTheme.CIRCUS, themes[themes.length - 1], "the tree writes the theme as an ordinal");
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
