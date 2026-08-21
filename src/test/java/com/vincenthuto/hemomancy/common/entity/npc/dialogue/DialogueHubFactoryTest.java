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

	@Test
	void unknownInquiryNodesDoNotBecomeInquiryTopics() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of()))
				.addNode(new DialogueNode("item_hint", List.of("hint"), List.of()))
				.addNode(new DialogueNode("item_inquiry/unknown", List.of("unknown"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "alchemist", new DialogueKnowledge());
		assertTrue(decorated.presentation().topics(DialogueCategory.INQUIRIES).isEmpty());
	}

	@Test
	void unreadProgressionLoreRequestsNoticeUntilItIsRead() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("ask_about_history", "history", null))))
				.addNode(new DialogueNode("history", List.of("history.line"), List.of()))
				.build();
		DialogueKnowledge knowledge = new DialogueKnowledge();

		DialogueTopic unread = DialogueHubFactory.decorate(base, "vicar", knowledge)
				.presentation().topics(DialogueCategory.LORE).getFirst();
		knowledge.markRead(id("vicar/lore/history"));
		DialogueTopic read = DialogueHubFactory.decorate(base, "vicar", knowledge)
				.presentation().topics(DialogueCategory.LORE).getFirst();

		assertEquals(DialogueAttention.NOTICE, unread.attention());
		assertEquals(DialogueAttention.NONE, read.attention());
	}

	@Test
	void authoredQuestAttentionSurvivesHubDecoration() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("claim_assignment_reward", null, "claim_reward",
								DialogueOptionPresentation.attention(DialogueAttention.URGENT)))))
				.build();

		DialogueTopic quest = DialogueHubFactory.decorate(base, "alchemist", new DialogueKnowledge())
				.presentation().topics(DialogueCategory.QUESTS).getFirst();

		assertEquals(DialogueAttention.URGENT, quest.attention());
	}

	@Test
	void progressionMissionEventsReceiveTheExpectedDefaultAttention() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("accept_assignment", null, "alchemist_first_separation_brief"),
						new DialogueOption("claim_assignment_reward", null, "alchemist_first_separation_claim"),
						new DialogueOption("report_assignment", null, "hermit_road_report"),
						new DialogueOption("begin_diagnosis", null, "vein_mason_diagnosis"),
						new DialogueOption("record_specimen", null, "alchemist_bestiary_record"))))
				.build();

		List<DialogueTopic> quests = DialogueHubFactory.decorate(base, "alchemist", new DialogueKnowledge())
				.presentation().topics(DialogueCategory.QUESTS);

		assertEquals(DialogueAttention.NOTICE, quests.get(0).attention());
		assertEquals(DialogueAttention.URGENT, quests.get(1).attention());
		assertEquals(DialogueAttention.URGENT, quests.get(2).attention());
		assertEquals(DialogueAttention.NOTICE, quests.get(3).attention());
		assertEquals(DialogueAttention.NONE, quests.get(4).attention());
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
