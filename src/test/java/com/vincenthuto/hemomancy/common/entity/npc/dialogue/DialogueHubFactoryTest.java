package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueHubFactoryTest {
	@Test
	void optionalScriptoriumTeachingStaysInLoreWithItsServerAction() {
		DialogueTree base = HarbingerMnemonistDialogueTrees.forDegree(3, 42, false, false, false, false);
		DialogueTree decorated = DialogueHubFactory.decorate(base, "mnemonist", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.LORE).stream()
				.filter(candidate -> candidate.titleKey().equals(
						"hemomancy.dialogue.mnemonist.option.ask_about_scriptorium"))
				.findFirst().orElseThrow(() -> new AssertionError("Optional Scriptorium teaching leaked into Quest Work"));
		DialogueOption teaching = decorated.getNode(topic.targetNodeId()).options().getFirst();
		assertEquals(HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON, teaching.eventId());
		assertEquals("scriptorium", teaching.nextNodeId());
		assertEquals(base.getNode("scriptorium"), decorated.getNode("scriptorium"));
		assertEquals(DialogueTopicState.AVAILABLE, topic.state());
		assertTrue(decorated.presentation().topics(DialogueCategory.QUESTS).stream()
				.noneMatch(candidate -> candidate.titleKey().equals(topic.titleKey())));
	}

	@Test
	void firstWeaveLessonAppearsInQuestWorkWithoutSkippingRecipeTeaching() {
		DialogueTree base = HarbingerMnemonistDialogueTrees.forDegree(3, 42, false, false, false, false);
		DialogueTree decorated = DialogueHubFactory.decorate(base, "mnemonist", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.QUESTS).stream()
				.filter(candidate -> candidate.id().equals("quests/woven_vessel"))
				.findFirst().orElseThrow(() -> new AssertionError("The Woven Vessel is missing from Quest Work"));

		DialogueOption teaching = base.getStartNode().options().stream()
				.filter(option -> "woven_vessel".equals(option.nextNodeId())).findFirst().orElseThrow();
		assertEquals(List.of(teaching), decorated.getNode(topic.targetNodeId()).options());
		assertEquals(HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE, teaching.eventId());
		assertEquals(base.getNode("woven_vessel"), decorated.getNode("woven_vessel"));
		assertEquals(DialogueTopicState.ACTIVE, topic.state());
		assertEquals(DialogueAttention.NOTICE, topic.attention());
		assertTrue(decorated.presentation().topics(DialogueCategory.LORE).stream()
				.anyMatch(candidate -> candidate.titleKey().equals("hemomancy.dialogue.mnemonist.option.ask_about_loom")));
	}

	@Test
	void indexedVesselDoesNotReofferItsMaterialBridgeInQuestWork() {
		DialogueTree base = HarbingerMnemonistDialogueTrees.forDegree(3, 42, false, false, false, true);
		DialogueTree decorated = DialogueHubFactory.decorate(base, "mnemonist", new DialogueKnowledge());

		assertTrue(decorated.presentation().topics().stream()
				.noneMatch(topic -> topic.id().equals("quests/woven_vessel")));
		assertTrue(decorated.presentation().topics(DialogueCategory.LORE).stream()
				.anyMatch(topic -> topic.titleKey().equals("hemomancy.dialogue.mnemonist.option.ask_about_loom")));
	}

	@Test
	void enteringARewardTopicDoesNotSkipItsEventBearingChoice() {
		DialogueOption claim = new DialogueOption("claim_assignment_reward", "thanks", "claim_reward");
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(claim)))
				.addNode(new DialogueNode("thanks", List.of("reward_delivered"), List.of()))
				.build();
		DialogueTree decorated = DialogueHubFactory.decorate(base, "vicar", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.QUESTS).getFirst();
		assertEquals(List.of("hemomancy.dialogue.action.confirm"),
				decorated.getNode(topic.targetNodeId()).lines(),
				"generated action nodes must never open with an empty dialogue body");
		assertEquals(List.of(claim), decorated.getNode(topic.targetNodeId()).options(),
				"opening a topic must expose the server action before its success node");
	}

	@Test
	void authoredActionPromptReplacesTheGenericSafetyLine() {
		DialogueOption claim = new DialogueOption("claim_assignment_reward", "thanks", "claim_reward",
				DialogueOptionPresentation.normal().withPrompt("vicar.reward.prompt"));
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(claim)))
				.addNode(new DialogueNode("thanks", List.of("reward_delivered"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "vicar", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.QUESTS).getFirst();

		assertEquals(List.of("vicar.reward.prompt"), decorated.getNode(topic.targetNodeId()).lines());
	}

	@Test
	void speakFreelyMenuHasAVisibleConversationPrompt() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("say_something", "answer", null))))
				.addNode(new DialogueNode("answer", List.of("answer.line"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "vicar", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.CONVERSATION).getFirst();

		assertEquals(List.of("hemomancy.dialogue.conversation.prompt"),
				decorated.getNode(topic.targetNodeId()).lines());
	}

	@Test
	void singleConversationActionCanSupplyItsOwnPrompt() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("take_the_vows", null, "begin_vows",
								DialogueOptionPresentation.prompt("vows.prompt")))))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "zealot", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.CONVERSATION).getFirst();

		assertEquals(List.of("vows.prompt"), decorated.getNode(topic.targetNodeId()).lines());
	}

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
