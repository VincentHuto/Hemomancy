package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtMembershipRules.Outcome;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
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
				for (Outcome join : List.of(Outcome.JOINED, Outcome.REFUSED_DEGREE, Outcome.REFUSED_STANDING,
						Outcome.REFUSED_SWITCH_USED, Outcome.ALREADY_MEMBER)) {
					for (StrainMatch strain : StrainMatch.values()) {
						views.add(new View(standing, teachable, flag, flag, flag, !flag, join, strain));
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
				DialogueTree tree = CourtMemberDialogue.build(role, 1, view);
				assertTrue(LANG.has(tree.speakerName()), tree.speakerName());
				for (DialogueNode node : tree.nodes().values()) {
					node.lines().stream().filter(key -> !LANG.has(key)).forEach(missing::add);
					node.options().stream().map(DialogueOption::text).filter(key -> !LANG.has(key)).forEach(missing::add);
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
	void theGardenTurnsATransgressorAwayUntilAmends() {
		DialogueTree tree = CourtMemberDialogue.build(CourtRole.GARDENER, 1,
				new View(Standing.TRANSGRESSOR, true, true, false, true, false, Outcome.JOINED, StrainMatch.NONE));
		var events = tree.getStartNode().options().stream().map(DialogueOption::eventId).filter(e -> e != null).toList();
		assertEquals(List.of(CourtMemberDialogue.AMENDS), events, "only amends are open to a transgressor");
	}
}
