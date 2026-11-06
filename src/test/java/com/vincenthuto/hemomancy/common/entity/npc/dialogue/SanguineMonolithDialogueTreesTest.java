package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules.ClaimState;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The stone's trees as the hub files them. Trade and the Inquiries need a loaded server; see {@code NpcTradeGameTests}. */
class SanguineMonolithDialogueTreesTest {
	private static final CovenantProgress NEW = new CovenantProgress(false, false, false, false, false);
	private static final String COUNSEL = "hemomancy.monolith.counsel.";

	private static DialogueTopic card(DialogueTree tree, DialogueCategory category, String titleEnd) {
		DialogueTree hub = DialogueHubFactory.decorate(tree, SanguineMonolithDialogueTrees.SPEAKER_ID, (DialogueKnowledge) null);
		return hub.presentation().topics(category).stream().filter(topic -> topic.titleKey().endsWith(titleEnd))
				.findFirst().orElse(null);
	}

	private static boolean offers(DialogueTree tree, String event) {
		return tree.nodes().values().stream().flatMap(node -> node.options().stream())
				.anyMatch(option -> event.equals(option.eventId()));
	}

	@Test
	void theCornerstoneCardReadsItsClaimState() {
		assertEquals(DialogueTopicState.TURN_IN, card(SanguineMonolithDialogueTrees.archon(ClaimState.READY, false),
				DialogueCategory.QUESTS, "anchor_armature").state());
		assertEquals(DialogueTopicState.ACTIVE, card(SanguineMonolithDialogueTrees.archon(ClaimState.USAGE_INCOMPLETE, false),
				DialogueCategory.QUESTS, "anchor_armature").state());
		assertEquals(DialogueTopicState.COMPLETE, card(SanguineMonolithDialogueTrees.archon(ClaimState.CLAIMED, false),
				DialogueCategory.QUESTS, "anchor_armature").state());
	}

	@Test
	void anUnreadyCornerstoneStillOffersTheClaimSoItsRefusalPlaysAndAClaimedOneDoesNot() {
		assertTrue(offers(SanguineMonolithDialogueTrees.archon(ClaimState.USAGE_INCOMPLETE, false), SanguineMonolithDialogueTrees.EVENT_CORNERSTONE));
		assertFalse(offers(SanguineMonolithDialogueTrees.archon(ClaimState.CLAIMED, false), SanguineMonolithDialogueTrees.EVENT_CORNERSTONE));
	}

	@Test
	void pressFurtherIsItsOwnButtonAboveTheWayOutAndFiledInNoBox() {
		DialogueTree archon = SanguineMonolithDialogueTrees.archon(ClaimState.READY, false);
		for (DialogueCategory category : DialogueCategory.values()) {
			assertEquals(null, card(archon, category, "press_further"), category + " holds no Press further card");
		}
		List<DialogueOption> root = archon.getStartNode().options();
		DialogueOption press = root.get(root.size() - 2);
		assertTrue(press.text().endsWith("press_further") && press.presentation().isSpecialAction(), "the button sits above Leave");
		assertEquals(null, press.eventId(), "the button only opens the warning; it breaks nothing itself");
		DialogueOption confirm = archon.getNode(press.nextNodeId()).options().getFirst();
		assertEquals(SanguineMonolithDialogueTrees.EVENT_SHATTER, confirm.eventId());
		assertTrue(confirm.presentation().isSpecialAction(), "the warning's own button glows too");
	}

	@Test
	void theStoneOffersNoSpecialActionBelowTheSeventhDegree() {
		for (DialogueTree tree : List.of(SanguineMonolithDialogueTrees.illuminatus(NEW), SanguineMonolithDialogueTrees.sanctified(NEW))) {
			assertFalse(tree.getStartNode().options().stream().anyMatch(option -> option.presentation().isSpecialAction()));
		}
	}

	@Test
	void counselFollowsTheCovenantAtEachDegree() {
		assertEquals(List.of(COUNSEL + "found_bloodline"), SanguineMonolithDialogueTrees.illuminatus(NEW).getNode("counsel").lines());
		assertEquals(List.of(COUNSEL + "found_fane"), SanguineMonolithDialogueTrees.illuminatus(
				new CovenantProgress(true, false, false, false, false)).getNode("counsel").lines());
		assertEquals(List.of(COUNSEL + "perform_bloodline_rite"), SanguineMonolithDialogueTrees.illuminatus(
				new CovenantProgress(true, true, false, false, false)).getNode("counsel").lines());
		assertEquals(List.of(COUNSEL + "chamber_return", COUNSEL + "bind_throne", COUNSEL + "complete_vigil"),
				SanguineMonolithDialogueTrees.sanctified(NEW).getNode("counsel").lines());
		assertEquals(List.of(COUNSEL + "perform_hematic_order_rite"), SanguineMonolithDialogueTrees.sanctified(
				new CovenantProgress(true, true, true, true, true)).getNode("counsel").lines());
		assertEquals(List.of(COUNSEL + "press_further"),
				SanguineMonolithDialogueTrees.archon(ClaimState.READY, false).getNode("counsel").lines());
	}

	@Test
	void counselPastTheSeedStopsSendingThePlayerIntoTheStone() {
		DialogueTree past = SanguineMonolithDialogueTrees.archon(ClaimState.READY, true);
		assertEquals(List.of(COUNSEL + "past_the_seed"), past.getNode("counsel").lines());
		assertTrue(offers(past, SanguineMonolithDialogueTrees.EVENT_SHATTER), "the stone can still be pressed");
	}

	@Test
	void theEscalatingAnswerIsANewCardAtEachDegree() {
		assertEquals(3, List.of(card(SanguineMonolithDialogueTrees.illuminatus(NEW), DialogueCategory.CONVERSATION, "what_are_you"),
				card(SanguineMonolithDialogueTrees.sanctified(NEW), DialogueCategory.CONVERSATION, "what_are_you"),
				card(SanguineMonolithDialogueTrees.archon(ClaimState.READY, false), DialogueCategory.CONVERSATION, "what_are_you"))
				.stream().map(DialogueTopic::readPath).distinct().count(), "a read D5 answer must not mark the D6 or D7 one read");
	}

	@Test
	void everyDegreeFilesItsCardsInTheRightBoxes() {
		for (DialogueTree tree : List.of(SanguineMonolithDialogueTrees.illuminatus(NEW), SanguineMonolithDialogueTrees.sanctified(NEW),
				SanguineMonolithDialogueTrees.archon(ClaimState.READY, false))) {
			assertNotNull(card(tree, DialogueCategory.QUESTS, "counsel"), "counsel is Quest Work");
			assertEquals(4, DialogueHubFactory.decorate(tree, SanguineMonolithDialogueTrees.SPEAKER_ID, (DialogueKnowledge) null)
					.presentation().topics(DialogueCategory.CONVERSATION).size(), "what_are_you and three questions");
			DialogueTopic what = card(tree, DialogueCategory.CONVERSATION, "what_are_you");
			assertTrue(what.lore() && what.unread(), "lore cards carry the glyph and the unread dot");
		}
	}

	@Test
	void everyAnswerLeadsBackAndTheGreetingEndsInDepart() {
		for (DialogueTree tree : List.of(SanguineMonolithDialogueTrees.illuminatus(NEW), SanguineMonolithDialogueTrees.sanctified(NEW),
				SanguineMonolithDialogueTrees.archon(ClaimState.READY, false), SanguineMonolithDialogueTrees.archon(ClaimState.CLAIMED, false))) {
			List<DialogueOption> root = tree.getStartNode().options();
			assertTrue(root.getLast().text().endsWith(".leave"), "the way out is last");
			for (DialogueNode node : tree.nodes().values()) {
				assertFalse(node.options().isEmpty(), node.id() + " has a way out");
				for (DialogueOption option : node.options()) {
					assertTrue(option.nextNodeId() == null || tree.getNode(option.nextNodeId()) != null,
							node.id() + " -> " + option.nextNodeId());
				}
			}
		}
	}

	@Test
	void everyKeyTheTreesUseHasEnglish() {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		List<String> missing = new ArrayList<>();
		for (DialogueTree tree : List.of(SanguineMonolithDialogueTrees.unworthy(), SanguineMonolithDialogueTrees.illuminatus(NEW),
				SanguineMonolithDialogueTrees.sanctified(NEW), SanguineMonolithDialogueTrees.sanctified(new CovenantProgress(true, true, true, true, true)),
				SanguineMonolithDialogueTrees.illuminatus(new CovenantProgress(true, false, false, false, false)),
				SanguineMonolithDialogueTrees.illuminatus(new CovenantProgress(true, true, false, false, false)),
				SanguineMonolithDialogueTrees.archon(ClaimState.READY, false), SanguineMonolithDialogueTrees.archon(ClaimState.READY, true),
				SanguineMonolithDialogueTrees.archon(ClaimState.CLAIMED, false))) {
			for (DialogueNode node : tree.nodes().values()) {
				for (String line : node.lines()) if (!lang.has(line)) missing.add(line);
				for (DialogueOption option : node.options()) if (!lang.has(option.text())) missing.add(option.text());
			}
		}
		assertEquals(List.of(), missing, "Add these keys to en_us.json");
	}
}
