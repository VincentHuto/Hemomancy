package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;

import net.minecraft.resources.ResourceLocation;

class DialogueHubFactoryTest {
	@Test
	void decoratesExistingRootOptionsWithoutChangingTheirEvents() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("ask_about_history", "history", null),
						new DialogueOption("claim_assignment_reward", null, "claim_reward"),
						new DialogueOption("leave", null, null))))
				.addNode(new DialogueNode("history", List.of("history.line"), List.of()))
				.addNode(new DialogueNode("item_hint", List.of("item.line"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "alchemist", new DialogueKnowledge());

		assertEquals(DialogueScreenMode.TOPIC_HUB, decorated.presentation().mode());
		assertTrue(decorated.presentation().hasTopics(DialogueCategory.QUESTS));
		assertTrue(!decorated.presentation().hasTopics(DialogueCategory.INQUIRIES));
		assertTrue(decorated.presentation().hasTopics(DialogueCategory.LORE));
		assertTrue(decorated.nodes().values().stream().flatMap(node -> node.options().stream())
				.anyMatch(option -> "claim_reward".equals(option.eventId())));
	}

	@Test
	void createsOneInquiryTopicForEveryGeneratedInventoryItemNode() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of()))
				.addNode(new DialogueNode("item_hint", List.of("hint"), List.of()))
				.addNode(new DialogueNode("item_inquiry/minecraft/stone", List.of("stone"), List.of()))
				.addNode(new DialogueNode("item_inquiry/hemomancy/bloody_vial", List.of("vial"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "alchemist", new DialogueKnowledge());
		List<DialogueTopic> inquiries = decorated.presentation().topics(DialogueCategory.INQUIRIES);

		assertEquals(2, inquiries.size());
		assertEquals(id("bloody_vial"), inquiries.get(1).displayItemId());
		assertEquals("item_inquiry/minecraft/stone", inquiries.getFirst().targetNodeId());
		assertEquals("hemomancy.dialogue.topic.inventory_item.summary", inquiries.getFirst().summaryKey());
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
