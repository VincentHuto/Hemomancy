package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.support.EnglishText;
import com.vincenthuto.hemomancy.support.RepoPaths;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The core-loop lessons (round two P10): every key they show exists and has a brief, their ids stay out of Quest Work,
 * the decorator builds a reachable lore card per lesson the Degree allows, every core-loop Liber page is taught by some
 * lesson, and the voice pins the sheets in {@code docs/NPC_VOICES.md} name hold.
 */
class CoreLoopLessonsTest {
	/**
	 * {@code DialogueHubFactory.isQuest}'s keywords (any of them in an id files the card under Quest Work), plus the words
	 * that make it skip an option as an item question or a farewell.
	 */
	private static final List<String> QUEST_WORDS = List.of("quest", "assignment", "reward", "claim", "brief", "task",
			"report", "lesson", "directive", "diagnosis", "observance", "taxonomy", "bestiary", "fungal_survey",
			"voyager_introduction", "morphling_handling", "circus_referral", "deep_dark_commission",
			"phlegethontic_commission", "vagrant_inquiry", "woven_vessel", "item_hint", "ask_about_item", "leave");
	private static final List<String> SPEAKERS = List.of(CoreLoopLessons.HERMIT, CoreLoopLessons.VICAR,
			CoreLoopLessons.ALCHEMIST, CoreLoopLessons.MNEMONIST, CoreLoopLessons.ARTIFICER, CoreLoopLessons.VEIN_MASON);
	/** Contracted forms, including the "'s" of it's, that's and the like (a possessive "'s" is not one). */
	private static final Pattern CONTRACTION = Pattern.compile(
			"n't|'ll|'re|'ve|'m\\b|'d\\b|(?i)\\b(it|that|there|here|what|where|who|let|he|she)'s\\b");
	private static final Pattern DEGREE_SHORTHAND = Pattern.compile("\\bD\\d\\b");

	@Test
	void everyKeyALessonShowsExistsAndEveryNewKeyHasABrief() throws IOException {
		Set<String> briefs = briefKeys();
		List<String> problems = new ArrayList<>();
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.all()) {
			EnglishText.of(lesson.leaveOption());
			for (String key : ownKeys(lesson)) {
				EnglishText.of(key);
				if (!briefs.contains(key)) problems.add(key + " has no dialogue brief");
			}
		}
		assertEquals(List.of(), problems);
	}

	@Test
	void noIdFilesALessonUnderQuestWork() {
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.all()) {
			List<String> ids = new ArrayList<>(List.of(lesson.rootOption(), lesson.node(), lesson.event()));
			for (CoreLoopLessons.Branch branch : lesson.branches()) {
				ids.add(lesson.branchOption(branch));
				ids.add(lesson.node(branch));
			}
			for (String id : ids) {
				for (String word : QUEST_WORDS) {
					assertFalse(id.toLowerCase(Locale.ROOT).contains(word), id + " contains '" + word + "'");
				}
			}
		}
	}

	@Test
	void eventIdsAreUniqueAndCarryTheirSpeakersPrefix() {
		Set<String> events = new HashSet<>();
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.all()) {
			assertTrue(events.add(lesson.event()), "duplicate event " + lesson.event());
			assertTrue(lesson.event().startsWith(lesson.npcKey() + "_craft_"), lesson.event());
			assertEquals(lesson, CoreLoopLessons.byEvent(lesson.event()));
			assertTrue(SPEAKERS.contains(lesson.speaker()), lesson.speaker());
			assertFalse(lesson.branches().isEmpty(), lesson.event() + " has no branches");
		}
		assertEquals(null, CoreLoopLessons.byEvent("vicar_craft_nothing"));
	}

	@Test
	void theDecoratorAddsALoreCardPerLessonTheDegreeAllows() {
		for (String speaker : SPEAKERS) {
			for (int degree = 0; degree <= 8; degree++) {
				DialogueTree tree = CoreLoopLessonDialogue.append(base(speaker), speaker, degree);
				List<DialogueOption> greeting = tree.getStartNode().options();
				for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
					DialogueOption card = greeting.stream().filter(option -> option.text().equals(lesson.rootOption()))
							.findFirst().orElse(null);
					if (!lesson.offeredAt(degree)) {
						assertEquals(null, card, lesson.event() + " offered at Degree " + degree);
						continue;
					}
					assertNotNull(card, lesson.event() + " missing at Degree " + degree);
					assertTrue(card.presentation().lore(), lesson.event() + " is not a lore card");
					assertEquals(null, card.eventId(), "the card itself carries no event");
					assertTrue(greeting.indexOf(card) < greeting.size() - 2, "lessons go before the item question and leave");
					DialogueNode node = tree.getNode(lesson.node());
					assertEquals(lesson.lineKeys(), node.lines());
					for (CoreLoopLessons.Branch branch : lesson.branches()) {
						DialogueOption question = node.options().stream()
								.filter(option -> option.text().equals(lesson.branchOption(branch))).findFirst().orElseThrow();
						assertEquals(lesson.event(), question.eventId(), "asking a branch teaches");
						DialogueNode answer = tree.getNode(question.nextNodeId());
						assertEquals(lesson.lineKeys(branch), answer.lines());
						assertTrue(answer.options().stream().anyMatch(option -> lesson.node().equals(option.nextNodeId())),
								"a branch leads back to its lesson");
						assertTrue(answer.options().stream().anyMatch(option -> option.nextNodeId() == null
								&& option.eventId() == null), "a branch has a way out");
					}
				}
			}
		}
		assertTrue(CoreLoopLessons.forSpeaker(CoreLoopLessons.HERMIT).stream()
				.noneMatch(lesson -> lesson.offeredAt(CoreLoopLessons.HERMIT_LAST_DEGREE + 1)),
				"a Hermit teaches nothing from the fifth Degree");
		assertTrue(CoreLoopLessons.forSpeaker(CoreLoopLessons.VEIN_MASON).stream().noneMatch(lesson -> lesson.offeredAt(3)),
				"the Vein-Mason teaches from the fourth Degree");
	}

	@Test
	void theHubFilesEveryLessonInConversationAsUnreadLore() {
		for (String speaker : SPEAKERS) {
			int degree = speaker.equals(CoreLoopLessons.HERMIT) ? 3 : 6;
			DialogueTree hub = DialogueHubFactory.decorate(CoreLoopLessonDialogue.append(base(speaker), speaker, degree),
					speaker, new DialogueKnowledge());
			for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.forSpeaker(speaker)) {
				assertTrue(lesson.offeredAt(degree), lesson.event() + " is not offered at Degree " + degree);
				DialogueTopic topic = hub.presentation().topics(DialogueCategory.CONVERSATION).stream()
						.filter(candidate -> candidate.titleKey().equals(lesson.rootOption())).findFirst()
						.orElseThrow(() -> new AssertionError(lesson.event() + " is not in Conversation"));
				assertTrue(topic.lore() && topic.unread(), lesson.event() + " is not unread lore");
				assertEquals(CoreLoopLessons.CARD_SUMMARY, topic.summaryKey(), "a lesson card has its own summary");
				assertEquals(lesson.node(), topic.targetNodeId(), "the card opens the lesson, with no confirm step");
				assertTrue(hub.presentation().topics(DialogueCategory.QUESTS).stream()
						.noneMatch(candidate -> candidate.titleKey().equals(lesson.rootOption())));
			}
		}
	}

	@Test
	void everyCoreLoopPageIsTaughtBySomeLesson() {
		Set<ResourceLocation> taught = new LinkedHashSet<>();
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.all()) {
			for (ResourceLocation page : lesson.pages()) {
				assertTrue(LiberEntryDefinitions.CORE_LOOP_PAGES.contains(page)
						|| LiberEntryDefinitions.OLDER_PAGES.contains(page), page + " is not a registered page");
				taught.add(page);
			}
		}
		List<ResourceLocation> untaught = LiberEntryDefinitions.CORE_LOOP_PAGES.stream()
				.filter(page -> !taught.contains(page)).toList();
		assertEquals(List.of(), untaught, "core-loop pages no lesson teaches");
	}

	@Test
	void theVoiceSheetsPinsHold() {
		List<String> problems = new ArrayList<>();
		for (CoreLoopLessons.Lesson lesson : CoreLoopLessons.all()) {
			for (String key : spokenKeys(lesson)) {
				String line = EnglishText.of(key);
				if (lesson.speaker().equals(CoreLoopLessons.MNEMONIST) && line.contains("?")) {
					problems.add(key + ": the Mnemonist asks no questions");
				}
				if (lesson.speaker().equals(CoreLoopLessons.VEIN_MASON) && CONTRACTION.matcher(line).find()) {
					problems.add(key + ": the Vein-Mason uses no contractions");
				}
				if (line.toLowerCase(Locale.ROOT).contains("tendenc")) problems.add(key + ": says tendency");
				if (line.contains("—") || line.contains("–") || line.contains("...")) {
					problems.add(key + ": dash or ellipsis");
				}
				if (DEGREE_SHORTHAND.matcher(line).find()) problems.add(key + ": Degree shorthand");
			}
		}
		assertEquals(List.of(), problems);
	}

	/** A greeting with a quest card, the item question and a leave option, the shape every speaker's start node has. */
	private static DialogueTree base(String speaker) {
		String npcKey = CoreLoopLessons.forSpeaker(speaker).getFirst().npcKey();
		return DialogueTree.builder("speaker", ResourceLocation.fromNamespaceAndPath("hemomancy", "portrait"), 42)
				.addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
						new DialogueOption("hemomancy.dialogue." + npcKey + ".option.what_next", "hint", null).withQuest(),
						new DialogueOption("hemomancy.dialogue." + npcKey + ".option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue." + npcKey + ".option.leave", null, null))))
				.addNode(new DialogueNode("hint", List.of("hint"), List.of()))
				.addNode(new DialogueNode("item_hint", List.of("item_hint"), List.of()))
				.build();
	}

	private static List<String> ownKeys(CoreLoopLessons.Lesson lesson) {
		List<String> keys = new ArrayList<>(List.of(lesson.rootOption(), lesson.backOption()));
		for (CoreLoopLessons.Branch branch : lesson.branches()) keys.add(lesson.branchOption(branch));
		keys.addAll(spokenKeys(lesson));
		return keys;
	}

	private static List<String> spokenKeys(CoreLoopLessons.Lesson lesson) {
		List<String> keys = new ArrayList<>(lesson.lineKeys());
		for (CoreLoopLessons.Branch branch : lesson.branches()) keys.addAll(lesson.lineKeys(branch));
		return keys;
	}

	private static Set<String> briefKeys() throws IOException {
		Set<String> keys = new HashSet<>();
		Path folder = RepoPaths.ASSETS.resolve("dialogue_briefs");
		try (Stream<Path> files = Files.list(folder)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				keys.addAll(JsonParser.parseString(Files.readString(file)).getAsJsonObject().keySet());
			}
		}
		return keys;
	}
}
