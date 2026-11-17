package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OfferedDialoguesTest {
	private static final ResourceLocation DIALOGUE = ResourceLocation.fromNamespaceAndPath("hemomancy", "vicar");

	/**
	 * A carried scar pattern adds a stack-aware inquiry card whose id ends in {@code #<hash>}. Matching a lore card's
	 * read id against that tree threw "Non [a-z0-9/._-] character in path" on the server (successionByPlay).
	 */
	@Test
	void aStackAwareInquiryCardDoesNotBreakLoreTopicMatching() {
		String inquiryNode = DialogueItemInquiryNodes.inventoryNodeId(
				ResourceLocation.fromNamespaceAndPath("hemomancy", "scar_pattern"), "hemomancy:scar_pattern/brand");
		assertTrue(inquiryNode.contains("#"), "the fixture needs the stack-aware id form");
		DialogueTopic inquiry = DialogueTopic.available(
				"inquiries/" + inquiryNode.substring(DialogueItemInquiryNodes.INVENTORY_NODE_PREFIX.length()),
				DialogueCategory.INQUIRIES, "title", "summary", inquiryNode);
		DialogueTopic lore = new DialogueTopic(DialogueTopic.conversationId("the_order"),
				DialogueCategory.CONVERSATION, "title", "summary", "the_order", null, DialogueTopicState.AVAILABLE,
				null, null, null, true, DialogueAttention.NONE, true);
		DialogueTree tree = DialogueTree.builder("vicar", DIALOGUE, 7)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of()))
				.presentation(DialoguePresentation.hub(DIALOGUE, DIALOGUE, List.of(inquiry, lore)))
				.build();

		assertTrue(OfferedDialogues.listsLoreTopic(tree,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "vicar/lore/the_order")));
		assertFalse(OfferedDialogues.listsLoreTopic(tree,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "vicar/lore/unoffered")));
	}

	@Test
	void aNonLoreCardIsNeverRecordedAsRead() {
		DialogueTopic quest = DialogueTopic.available("quests/woven_vessel", DialogueCategory.QUESTS,
				"title", "summary", "woven_vessel");
		DialogueTree tree = DialogueTree.builder("vicar", DIALOGUE, 7)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of()))
				.presentation(DialoguePresentation.hub(DIALOGUE, DIALOGUE, List.of(quest)))
				.build();

		assertFalse(OfferedDialogues.listsLoreTopic(tree,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "vicar/quests/woven_vessel")));
	}
}
