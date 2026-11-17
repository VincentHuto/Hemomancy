package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** The pledge offer shows only the speaker's consider line; the accept line waits for the pledge itself (UN-66). */
class RecruitOfferNodeTest {
	@Test
	void theOfferSaysOnlyWhatTheSpeakerConsiders() {
		check(HarbingerAlchemistDialogueTrees.archon(42, true, false), "alchemist");
		check(HarbingerAlchemistDialogueTrees.silentium(42, true, false), "alchemist");
		check(HarbingerVicarDialogueTrees.silentium(42, true, false, false), "vicar");
	}

	private static void check(DialogueTree tree, String speaker) {
		DialogueNode offer = tree.getNode("recruit_offer");
		assertNotNull(offer, speaker + " has a pledge offer");
		assertEquals(List.of("hemomancy.dialogue.recruit." + speaker + ".consider"), offer.lines());
		EnglishText.of("hemomancy.dialogue.recruit." + speaker + ".accept");
	}
}
