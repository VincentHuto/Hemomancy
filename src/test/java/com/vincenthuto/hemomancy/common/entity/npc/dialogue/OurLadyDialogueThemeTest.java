package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OurLadyDialogueThemeTest {
	@Test
	void ourLadyWhispersUseTheStillMaterialThemeRule() {
		assertEquals(DialogueTheme.STILL_MATERIAL, OurLadyDialogueThemeRules.theme());
	}

	@Test
	void stillThemesAppendWithoutChangingExistingWireOrdinals() {
		assertEquals("BLOOD", DialogueTheme.fromOrdinal(0).name());
		assertEquals("UNSTAINED", DialogueTheme.fromOrdinal(1).name());
		assertEquals("FUNGAL", DialogueTheme.fromOrdinal(2).name());
		assertEquals("STILL", DialogueTheme.fromOrdinal(3).name());
		assertEquals("STILL_MATERIAL", DialogueTheme.fromOrdinal(4).name());
	}
}
