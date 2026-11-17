package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtChampionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtMembershipRules.Outcome;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue.Ripeness;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue.Standing;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue.StrainMatch;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue.View;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Brined Court Track 9: every built Court role's tree, in every state the Court can see a player in, resolves every
 * line and option to a lang key; the shunned get one refusal and nothing else; before the fifth Degree nothing is
 * taught or joined.
 */
final class CourtMemberDialogueTest {
	private static final List<CourtRole> BUILT = List.of(CourtRole.GARDENER, CourtRole.ARBOR, CourtRole.LIGHTKEEPER,
			CourtRole.TIDEWARDEN, CourtRole.BRACED_ELDER, CourtRole.EDGE_SWIMMER, CourtRole.NURSERY_TENDER,
			CourtRole.LINESMAN, CourtRole.LAMP_COLONY, CourtRole.HOST, CourtRole.VEILED_RESIDENT, CourtRole.BELLWARDEN,
			CourtRole.SLEEPER, CourtRole.MARROW_TENDER, CourtRole.ROOTED_ELDER,
			CourtRole.OFFERING_BEARER, CourtRole.KINDLED_ELDER, CourtRole.HEAD_TENDER);
	private static final JsonObject LANG = ResourceReader.asset("lang/en_us.json");

	private static List<View> views(CourtRole role) {
		List<View> views = new ArrayList<>();
		for (Standing standing : Standing.values()) {
			if (standing == Standing.TRANSGRESSOR && role.house() != CourtHouse.GIVING_LIGHT) continue;
			for (boolean teachable : new boolean[] {false, true}) for (boolean flag : new boolean[] {false, true}) {
				for (Outcome join : List.of(Outcome.JOINED, Outcome.SWITCHED, Outcome.REFUSED_DEGREE, Outcome.REFUSED_STANDING,
						Outcome.REFUSED_SWITCH_USED, Outcome.ALREADY_MEMBER)) {
					for (StrainMatch strain : StrainMatch.values()) {
						views.add(new View(standing, teachable, flag, flag, flag, !flag, join, strain));
						for (Ripeness ripeness : Ripeness.values()) {
							views.add(new View(standing, teachable, flag, flag, flag, !flag, join, strain, true,
									strain == StrainMatch.NONE ? Ripeness.YOUNG : ripeness));
						}
					}
				}
			}
		}
		return views;
	}

	@Test
	void everyLineAndOptionHasEnglish() {
		Set<String> missing = new TreeSet<>();
		for (CourtRole role : BUILT) {
			for (View view : views(role)) {
				List<CourtChampionRules.Gate> gates = new ArrayList<>();
				gates.add(null);
				if (role == CourtRole.HEAD_TENDER) gates.addAll(List.of(CourtChampionRules.Gate.values()));
				for (CourtChampionRules.Gate gate : gates) {
					DialogueTree tree = CourtMemberDialogue.build(role, 1, view, gate);
					assertTrue(LANG.has(tree.speakerName()), tree.speakerName());
					for (DialogueNode node : tree.nodes().values()) {
						node.lines().stream().filter(key -> !LANG.has(key)).forEach(missing::add);
						node.options().stream().map(DialogueOption::text).filter(key -> !LANG.has(key)).forEach(missing::add);
						node.options().stream().map(option -> option.presentation().promptKey())
								.filter(key -> key != null && !LANG.has(key)).forEach(missing::add);
					}
				}
			}
			assertTrue(LANG.has("hemomancy.dialogue.newcomer." + role.entityId()), role + " newcomer line");
		}
		assertEquals(Set.of(), missing);
	}

	@Test
	void theShunnedGetARefusalAndNothingElse() {
		for (CourtRole role : BUILT) {
			DialogueTree tree = CourtMemberDialogue.build(role, 1,
					new View(Standing.SHUNNED, true, true, false, true, false, Outcome.JOINED, StrainMatch.OWN));
			assertEquals(1, tree.nodes().size(), role + " shows the shunned only the refusal");
			DialogueNode root = tree.getStartNode();
			assertEquals(List.of("hemomancy.court." + role.id() + ".shunned.line1", "hemomancy.court." + role.id() + ".shunned.line2"),
					root.lines());
			assertTrue(root.options().stream().allMatch(option -> option.eventId() == null), role + " gives the shunned nothing");
		}
	}

	@Test
	void beforeTheFifthDegreeNothingIsTaughtOrJoined() {
		for (CourtRole role : BUILT) {
			DialogueTree tree = CourtMemberDialogue.build(role, 1,
					new View(Standing.KNOWN, false, true, false, false, false, Outcome.REFUSED_DEGREE, StrainMatch.NONE));
			for (DialogueNode node : tree.nodes().values()) {
				for (DialogueOption option : node.options()) {
					assertFalse(CourtMemberDialogue.LESSON.equals(option.eventId()), role + " teaches before the fifth Degree");
					assertFalse(CourtMemberDialogue.JOIN.equals(option.eventId()), role + " takes a member before the fifth Degree");
				}
			}
			if (role.offers(CourtRole.Offer.FOOD)) {
				assertTrue(tree.getStartNode().options().stream().anyMatch(o -> CourtMemberDialogue.FOOD.equals(o.eventId())),
						role + " still feeds a hungry visitor before the fifth Degree");
			}
		}
	}

	@Test
	void theSwitchWarnsBeforeTheMoveIsSpentAndAFirstEntryDoesNot() {
		View switching = new View(Standing.KNOWN, true, false, true, false, false, Outcome.SWITCHED, StrainMatch.NONE);
		View first = new View(Standing.KNOWN, true, false, true, false, false, Outcome.JOINED, StrainMatch.NONE);
		for (CourtRole role : BUILT) {
			if (!role.offers(CourtRole.Offer.JOIN)) continue;
			DialogueOption join = option(CourtMemberDialogue.build(role, 1, switching), CourtMemberDialogue.JOIN);
			assertEquals(CourtMemberDialogue.SWITCH_PROMPT, join.presentation().promptKey(), role + " warns before a switch");
			assertNull(option(CourtMemberDialogue.build(role, 1, first), CourtMemberDialogue.JOIN).presentation().promptKey(),
					role + " keeps the plain confirm for a first entry");
			DialogueTree hub = DialogueHubFactory.decorate(CourtMemberDialogue.build(role, 1, switching), role.entityId(),
					(DialogueKnowledge) null);
			assertTrue(hub.nodes().values().stream().anyMatch(node -> node.lines().equals(List.of(CourtMemberDialogue.SWITCH_PROMPT))
					&& node.options().stream().anyMatch(o -> CourtMemberDialogue.JOIN.equals(o.eventId()))),
					role + " shows the warning on the step that joins");
		}
	}

	@Test
	void theChallengeWarnsBeforeTheChampionIsCalled() {
		View known = new View(Standing.KNOWN, true, false, true, false, false, Outcome.REFUSED_STANDING, StrainMatch.OWN);
		DialogueTree open = CourtMemberDialogue.build(CourtRole.HEAD_TENDER, 1, known, CourtChampionRules.Gate.OPEN);
		String warning = "hemomancy.court.head_tender.challenge.line2";
		assertEquals(warning, option(open, CourtMemberDialogue.CHALLENGE).presentation().promptKey());
		assertEquals(List.of("hemomancy.court.head_tender.challenge.line1"), open.getNode("challenge").lines(),
				"the reply after the Champion is called no longer asks");
		DialogueTree hub = DialogueHubFactory.decorate(open, CourtRole.HEAD_TENDER.entityId(), (DialogueKnowledge) null);
		assertTrue(hub.nodes().values().stream().anyMatch(node -> node.lines().equals(List.of(warning))
				&& node.options().stream().anyMatch(o -> CourtMemberDialogue.CHALLENGE.equals(o.eventId()))),
				"the hub's confirm step is the warning");

		DialogueTree early = CourtMemberDialogue.build(CourtRole.HEAD_TENDER, 1, known, CourtChampionRules.Gate.REFUSED_DEGREE);
		assertEquals(List.of("hemomancy.court.head_tender.challenge.refused_degree"),
				early.getNode("challenge_refused_degree").lines());
		assertFalse(early.getStartNode().options().stream().anyMatch(o -> CourtMemberDialogue.CHALLENGE.equals(o.eventId())),
				"no challenge below the fifth Degree");
	}

	@Test
	void theLateDegreesAddAGreetingLineAtThreeSpeakers() {
		for (CourtRole role : BUILT) {
			List<String> early = greeting(role, new View(Standing.KNOWN, true, false, true, false, false, Outcome.JOINED,
					StrainMatch.NONE));
			List<String> late = greeting(role, new View(Standing.KNOWN, true, false, true, false, false, Outcome.JOINED,
					StrainMatch.NONE, true, Ripeness.YOUNG));
			String line = "hemomancy.court." + role.id() + ".degree.late";
			assertFalse(early.contains(line), role + " says nothing of the late Degrees before the sixth");
			assertEquals(CourtMemberDialogue.LATE_GREETERS.contains(role), late.contains(line), role + " at the sixth Degree");
		}
	}

	@Test
	void aPrimalStrainIsAnsweredAndTheLeadsSpeakOfTheStep() {
		for (CourtRole role : BUILT) {
			String base = "hemomancy.court." + role.id() + ".";
			for (Ripeness ripeness : Ripeness.values()) {
				View view = new View(Standing.KNOWN, true, false, true, false, false, Outcome.JOINED, StrainMatch.OWN, true, ripeness);
				DialogueTree tree = CourtMemberDialogue.build(role, 1, view);
				List<String> greeting = tree.getStartNode().lines();
				boolean primal = ripeness == Ripeness.PRIMAL && CourtMemberDialogue.PRIMAL_GREETERS.contains(role);
				assertEquals(primal, greeting.contains(base + "strain.primal"), role + " greets a " + ripeness + " strain");
				assertEquals(!primal, greeting.contains(base + "strain.own"), role + " keeps its own-strain line otherwise");
				boolean topic = ripeness != Ripeness.YOUNG && CourtMemberDialogue.PRIMAL_SPEAKERS.contains(role);
				assertEquals(topic, tree.getStartNode().options().stream().anyMatch(o -> "primal".equals(o.nextNodeId())),
						role + " offers the Primal topic with a " + ripeness + " strain");
				if (topic) assertEquals(List.of(base + "primal.line1", base + "primal.line2"), tree.getNode("primal").lines());
			}
		}
		assertEquals(9, CourtMemberDialogue.PRIMAL_GREETERS.size());
	}

	@Test
	void directionsNameEachHousesSite() {
		// Each house named in a directions line comes with its site (PT-50); the Still Bell's site is its bell.
		java.util.Map<String, String> sites = new java.util.LinkedHashMap<>();
		sites.put("Holding Stone", "Tide Refuge");
		sites.put("Shared Wake", "nursery");
		sites.put("Woven Current", "Line Camp");
		sites.put("Veiled Fold", "Screened Cleft");
		sites.put("Last Marrow", "Whale Fall");
		sites.put("Kindling", "Vent Sanctum");
		sites.put("Giving Light", "Garden");
		for (CourtRole role : BUILT) {
			if (!role.offers(CourtRole.Offer.DIRECTIONS)) continue;
			for (String line : List.of("line1", "line2")) {
				String key = "hemomancy.court." + role.id() + ".directions." + line;
				String english = LANG.get(key).getAsString();
				sites.forEach((house, site) -> {
					if (english.contains(house)) assertTrue(english.contains(site), key + " names " + house + " without " + site);
				});
				if (english.contains("Still Bell")) {
					assertTrue(english.toLowerCase(java.util.Locale.ROOT).split("bell", -1).length > 2, key + " names the Still Bell without its bell");
				}
			}
		}
		for (CourtRole role : List.of(CourtRole.GARDENER, CourtRole.ARBOR, CourtRole.LIGHTKEEPER)) {
			String both = LANG.get("hemomancy.court." + role.id() + ".directions.line1").getAsString()
					+ LANG.get("hemomancy.court." + role.id() + ".directions.line2").getAsString();
			assertTrue(both.contains("Vent Sanctum"), role + " names the Vent Sanctum past the Garden");
		}
		for (String key : List.of("edge_swimmer.directions.line2", "gardener.directions.line1", "lightkeeper.directions.line1")) {
			String english = LANG.get("hemomancy.court." + key).getAsString();
			assertTrue(english.contains("nursery") && english.contains("Nursery tender"), key + " points to the nursery and its tender");
		}
	}

	@Test
	void theLessonIsAboutWearingAStrainNotFeedingIt() {
		List<String> keys = new ArrayList<>(List.of("lightkeeper.lesson.line1", "lightkeeper.lesson.line2", "lightkeeper.lesson.known",
				"lightkeeper.item_inquiry.morphling_sunburst.line2", "lesson.taught",
				"bellwarden.item_inquiry.morphling_hushbell.line2", "edge_swimmer.item_inquiry.morphling_mermaids_purse.line2",
				"gardener.item_inquiry.morphling_sunburst.line2", "host.item_inquiry.morphling_ink_cap.line2",
				"lamp_colony.item_inquiry.morphling_dead_mans_rope.line2", "linesman.item_inquiry.morphling_dead_mans_rope.line2",
				"marrow_tender.item_inquiry.morphling_marrowplume.line2", "nursery_tender.item_inquiry.morphling_mermaids_purse.line2",
				"rooted_elder.item_inquiry.morphling_marrowplume.line2", "tidewarden.item_inquiry.morphling_holdfast.line1",
				"tidewarden.item_inquiry.morphling_holdfast.line2", "veiled_resident.item_inquiry.morphling_ink_cap.line2"));
		for (String key : keys) {
			String english = LANG.get("hemomancy.court." + key).getAsString().toLowerCase(java.util.Locale.ROOT);
			assertFalse(english.matches(".*\\bfe(e)?d\\b.*"), key + " still says the strain is fed: " + english);
		}
	}

	private static List<String> greeting(CourtRole role, View view) {
		return CourtMemberDialogue.build(role, 1, view).getStartNode().lines();
	}

	private static DialogueOption option(DialogueTree tree, String eventId) {
		return tree.getStartNode().options().stream().filter(o -> eventId.equals(o.eventId())).findFirst().orElseThrow();
	}

	@Test
	void theGardenTurnsATransgressorAwayUntilAmends() {
		DialogueTree tree = CourtMemberDialogue.build(CourtRole.GARDENER, 1,
				new View(Standing.TRANSGRESSOR, true, true, false, true, false, Outcome.JOINED, StrainMatch.NONE));
		var events = tree.getStartNode().options().stream().map(DialogueOption::eventId).filter(e -> e != null).toList();
		assertEquals(List.of(CourtMemberDialogue.AMENDS), events, "only amends are open to a transgressor");
	}
}
