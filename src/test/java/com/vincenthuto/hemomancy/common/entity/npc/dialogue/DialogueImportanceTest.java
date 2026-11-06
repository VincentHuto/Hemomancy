package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules.ClaimState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Important tab: special actions and cards that need the player now, never unread lore. */
class DialogueImportanceTest {
	private static DialogueTree hub(DialogueTree tree) {
		return DialogueHubFactory.decorate(tree, SanguineMonolithDialogueTrees.SPEAKER_ID, (DialogueKnowledge) null);
	}

	@Test
	void theStonesPressFurtherAndAReadyCornerstoneAreImportantAndItsUnreadLoreIsNot() {
		DialogueTree archon = hub(SanguineMonolithDialogueTrees.archon(ClaimState.READY, false));
		List<Integer> actions = DialogueImportance.specialActions(archon);
		assertEquals(1, actions.size());
		assertTrue(archon.getStartNode().options().get(actions.getFirst()).text().endsWith("press_further"));
		List<DialogueTopic> topics = DialogueImportance.topics(archon.presentation());
		assertEquals(1, topics.size(), "only the claimable Cornerstone: " + topics);
		assertEquals(DialogueTopicState.TURN_IN, topics.getFirst().state());
		assertTrue(archon.presentation().topics().stream().anyMatch(topic -> topic.lore() && topic.unread()),
				"the unread lore is there, and still not important");
		assertEquals(2, DialogueImportance.count(archon));
	}

	@Test
	void aStoneWithNothingPressingShowsNoTab() {
		CovenantProgress fresh = new CovenantProgress(false, false, false, false, false);
		assertEquals(0, DialogueImportance.count(hub(SanguineMonolithDialogueTrees.illuminatus(fresh))));
		assertEquals(0, DialogueImportance.count(hub(SanguineMonolithDialogueTrees.sanctified(fresh))));
	}

	@Test
	void aCardWithASigilIsImportantAndALockedOneIsNot() {
		DialogueTopic urgent = DialogueTopic.available("quests/reward", DialogueCategory.QUESTS, "t", "s", "n");
		urgent = new DialogueTopic(urgent.id(), urgent.category(), urgent.titleKey(), urgent.summaryKey(), urgent.targetNodeId(),
				null, DialogueTopicState.ACTIVE, null, null, null, false, DialogueAttention.URGENT, false);
		DialogueTopic locked = new DialogueTopic("conversation/x", DialogueCategory.CONVERSATION, "t", "s", "n", null,
				DialogueTopicState.LOCKED, null, null, null, false, DialogueAttention.NOTICE, false);
		DialoguePresentation presentation = DialoguePresentation.hub(null, null, List.of(urgent, locked));
		assertEquals(List.of(urgent), DialogueImportance.topics(presentation));
	}
}
