package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueHubFactoryTest {
	@Test
	void aRapportLoreLineIsALockedCardAtTierZeroAndOpensAtItsTier() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"),
						List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
		DialogueRapportLore.Line trusted = DialogueRapportLore.lines("alchemist").get(1);
		assertEquals(com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules.TRUSTED, trusted.tier());

		DialogueTree stranger = DialogueHubFactory.decorate(DialogueRapportLore.with(base, "alchemist", 0),
				"alchemist", new DialogueKnowledge(), 0);
		DialogueTopic locked = topic(stranger, trusted.textKey());
		assertEquals(DialogueTopicState.LOCKED, locked.state());
		assertTrue(locked.lore());
		assertEquals(DialogueHubFactory.LORE_ICON, locked.icon());
		assertEquals(trusted.teaserKey(), locked.summaryKey());
		assertEquals(DialogueHubFactory.LOCKED_TARGET, locked.targetNodeId());
		assertTrue(!locked.unread(), "a locked line has nothing to read yet");
		assertEquals(null, stranger.getNode(trusted.nodeId()), "a locked line sends no answer to the client");
		DialogueOption greetingOption = stranger.getStartNode().options().stream()
				.filter(option -> option.text().equals(trusted.textKey())).findFirst().orElseThrow();
		assertTrue(!greetingOption.presentation().enabled(), "the greeting lists it as a disabled teaser");
		assertEquals(trusted.teaserKey(), greetingOption.presentation().detailKey());
		assertEquals(null, DialogueAttentionResolver.topic(stranger.presentation()));

		DialogueTree trustedTree = DialogueHubFactory.decorate(DialogueRapportLore.with(base, "alchemist", 2),
				"alchemist", new DialogueKnowledge(), 2);
		DialogueTopic open = topic(trustedTree, trusted.textKey());
		assertEquals(DialogueTopicState.AVAILABLE, open.state());
		assertTrue(open.unread(), "an opened rapport line gets the unread dot");
		assertEquals("lore/" + trusted.nodeId(), open.readPath());
		assertEquals(trusted.lineKeys(), trustedTree.getNode(trusted.nodeId()).lines());
		assertEquals("hemomancy.dialogue.alchemist.option.leave",
				trustedTree.getStartNode().options().getLast().text(), "rapport lines go before the farewell");
	}

	private static DialogueTopic topic(DialogueTree tree, String titleKey) {
		return tree.presentation().topics(DialogueCategory.CONVERSATION).stream()
				.filter(candidate -> candidate.titleKey().equals(titleKey)).findFirst()
				.orElseThrow(() -> new AssertionError(titleKey + " is not in Conversation"));
	}

	@Test
	void optionalScriptoriumTeachingStaysALoreCardWithItsServerAction() {
		DialogueTree base = HarbingerMnemonistDialogueTrees.forDegree(3, 42, false, false, false, false);
		DialogueTree decorated = DialogueHubFactory.decorate(base, "mnemonist", new DialogueKnowledge());
		DialogueTopic topic = decorated.presentation().topics(DialogueCategory.CONVERSATION).stream()
				.filter(candidate -> candidate.titleKey().equals(
						"hemomancy.dialogue.mnemonist.option.ask_about_scriptorium"))
				.findFirst().orElseThrow(() -> new AssertionError("Optional Scriptorium teaching leaked into Quest Work"));
		DialogueOption teaching = decorated.getNode(topic.targetNodeId()).options().getFirst();
		assertEquals(HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON, teaching.eventId());
		assertEquals("scriptorium", teaching.nextNodeId());
		assertEquals(base.getNode("scriptorium"), decorated.getNode("scriptorium"));
		assertEquals(DialogueTopicState.AVAILABLE, topic.state());
		assertTrue(topic.lore());
		assertEquals(DialogueHubFactory.LORE_ICON, topic.icon());
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
		assertTrue(decorated.presentation().topics(DialogueCategory.CONVERSATION).stream()
				.anyMatch(candidate -> candidate.lore()
						&& candidate.titleKey().equals("hemomancy.dialogue.mnemonist.option.ask_about_loom")));
	}

	@Test
	void indexedVesselDoesNotReofferItsMaterialBridgeInQuestWork() {
		DialogueTree base = HarbingerMnemonistDialogueTrees.forDegree(3, 42, false, false, false, true);
		DialogueTree decorated = DialogueHubFactory.decorate(base, "mnemonist", new DialogueKnowledge());

		assertTrue(decorated.presentation().topics().stream()
				.noneMatch(topic -> topic.id().equals("quests/woven_vessel")));
		assertTrue(decorated.presentation().topics(DialogueCategory.CONVERSATION).stream()
				.anyMatch(topic -> topic.lore()
						&& topic.titleKey().equals("hemomancy.dialogue.mnemonist.option.ask_about_loom")));
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
	void everyConversationOptionGetsItsOwnCardInAuthoredOrder() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("say_something", "answer", null),
						new DialogueOption("tell_me_of_the_order", "order", null).withLore(),
						new DialogueOption("say_more", "more", null))))
				.addNode(new DialogueNode("answer", List.of("answer.line"), List.of()))
				.addNode(new DialogueNode("order", List.of("order.line"), List.of()))
				.addNode(new DialogueNode("more", List.of("more.line"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "vicar", new DialogueKnowledge());
		List<DialogueTopic> cards = decorated.presentation().topics(DialogueCategory.CONVERSATION);

		assertEquals(List.of("conversation/answer", "conversation/order", "conversation/more"),
				cards.stream().map(DialogueTopic::id).toList());
		assertEquals(List.of("answer", "order", "more"), cards.stream().map(DialogueTopic::targetNodeId).toList(),
				"a talk option opens its own node, not a folded Speak Freely menu");
		assertEquals(List.of(false, true, false), cards.stream().map(DialogueTopic::lore).toList());
		assertEquals(null, cards.getFirst().icon());
		assertEquals(DialogueHubFactory.LORE_ICON, cards.get(1).icon());
		assertTrue(cards.get(1).unread());
		assertTrue(!cards.getFirst().unread(), "ordinary talk never carries an unread dot");
		assertTrue(decorated.nodes().keySet().stream().noneMatch(id -> id.equals("__hub_conversation")));
	}

	@Test
	void loreKeywordsNoLongerMakeAnOptionLore() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("ask_about_history", "history", null))))
				.addNode(new DialogueNode("history", List.of("history.line"), List.of()))
				.build();

		DialogueTopic card = DialogueHubFactory.decorate(base, "vicar", new DialogueKnowledge())
				.presentation().topics(DialogueCategory.CONVERSATION).getFirst();

		assertTrue(!card.lore() && !card.unread(), "only the explicit flag makes an option lore");
	}

	@Test
	void explicitQuestFlagFilesAnOptionUnderQuestWork() {
		DialogueTree base = DialogueTree.builder("speaker", id("portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("hemomancy.circus.option.trial", null, "circus_trial").withQuest(),
						new DialogueOption("hemomancy.circus.option.accept", "succession_warning", null).withQuest())))
				.addNode(new DialogueNode("succession_warning", List.of("warning"), List.of()))
				.build();

		DialogueTree decorated = DialogueHubFactory.decorate(base, "circus_ringmaster", new DialogueKnowledge());

		assertEquals(2, decorated.presentation().topics(DialogueCategory.QUESTS).size());
		assertTrue(decorated.presentation().topics(DialogueCategory.CONVERSATION).isEmpty());
		assertEquals("succession_warning",
				decorated.presentation().topics(DialogueCategory.QUESTS).get(1).targetNodeId());
	}

	@Test
	void theTradeBoxStaysEmptyUntilItHasCards() {
		DialogueTree base = HarbingerMnemonistDialogueTrees.forDegree(3, 42, false, false, false, false);
		DialogueTree decorated = DialogueHubFactory.decorate(base, "mnemonist", new DialogueKnowledge());

		assertTrue(!decorated.presentation().hasTopics(DialogueCategory.TRADE));
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
		assertTrue(decorated.presentation().hasTopics(DialogueCategory.CONVERSATION));
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
						new DialogueOption("ask_about_history", "history", null).withLore())))
				.addNode(new DialogueNode("history", List.of("history.line"), List.of()))
				.build();
		DialogueKnowledge knowledge = new DialogueKnowledge();

		DialogueTopic unread = DialogueHubFactory.decorate(base, "vicar", knowledge)
				.presentation().topics(DialogueCategory.CONVERSATION).getFirst();
		knowledge.markRead(id("vicar/lore/history"));
		DialogueTopic read = DialogueHubFactory.decorate(base, "vicar", knowledge)
				.presentation().topics(DialogueCategory.CONVERSATION).getFirst();

		assertEquals("conversation/history", unread.id());
		assertEquals("lore/history", unread.readPath(), "lore read before the merge must stay read");
		assertEquals(DialogueAttention.NOTICE, unread.attention());
		assertTrue(unread.unread());
		assertEquals(DialogueAttention.NONE, read.attention());
		assertTrue(!read.unread());
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
