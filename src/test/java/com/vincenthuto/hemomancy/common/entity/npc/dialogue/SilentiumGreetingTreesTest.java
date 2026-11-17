package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.Step;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PT-83 and UN-19 (2026-11-13): a Degree 8 Silentium is greeted as a Silentium by the five outpost speakers. */
class SilentiumGreetingTreesTest {
	@Test
	void theVicarAndTheAlchemistHaveATreeOfTheirOwnForASilentium() {
		DialogueTree vicar = HarbingerVicarDialogueTrees.silentium(42, false, false, false);
		assertEquals(List.of("hemomancy.vicar.silentium.line1", "hemomancy.vicar.silentium.line2"),
				vicar.getStartNode().lines());
		assertTrue(hasOption(vicar, "silentium_record"));
		assertFalse(hasOption(vicar, "final_truth"), "the Archon's saying about an eighth Degree is behind them");

		DialogueTree alchemist = HarbingerAlchemistDialogueTrees.forDegree(8, 42, false, false, null, null,
				false, false, false, false, true);
		assertEquals(List.of("hemomancy.alchemist.silentium.line1", "hemomancy.alchemist.silentium.line2"),
				alchemist.getStartNode().lines());
		assertTrue(hasOption(alchemist, "reading"));
		assertEquals(List.of("hemomancy.alchemist.apotheos.line1", "hemomancy.alchemist.apotheos.line2"),
				HarbingerAlchemistDialogueTrees.forDegree(8, 42, false, false, null, null,
						false, false, false, false, false).getStartNode().lines(), "an Apotheos keeps theirs");
	}

	@Test
	void theMnemonistOpensAnEntryOnlyForAnEighthDegreeSilentium() {
		DialogueTree silentium = mnemonist(8, true);
		assertEquals(List.of("hemomancy.mnemonist.silentium.line1", "hemomancy.mnemonist.silentium.line2"),
				silentium.getStartNode().lines());
		assertTrue(hasOption(silentium, "silentium_entry"));
		assertTrue(hasOption(silentium, "loom"), "the rest of the archive stays open");
		assertFalse(hasOption(mnemonist(8, false), "silentium_entry"));
		assertFalse(hasOption(mnemonist(7, true), "silentium_entry"));
		for (String key : lines(silentium, "greeting", "silentium_entry")) {
			assertFalse(EnglishText.of(key).contains("?"), "the Mnemonist asks no questions: " + key);
		}
	}

	@Test
	void theArtificerOffersTheSilentArchonVestmentsToASilentiumOnly() {
		DialogueTree silentium = HarbingerArtificerDialogueTrees.forState(42, artificer(8, true));
		assertEquals(List.of("hemomancy.artificer.silentium.line1", "hemomancy.artificer.silentium.line2"),
				silentium.getStartNode().lines());
		assertTrue(hasOption(silentium, "silent_vestments"));
		String lesson = String.join(" ", lines(silentium, "silent_vestments").stream().map(EnglishText::of).toList());
		for (String fact : List.of("Silentium", "Blood Lust", "Monolithic Armature", "Monolith Imbued Cloth", "2,000 mL")) {
			assertTrue(lesson.contains(fact), "the vestments lesson names " + fact);
		}
		DialogueTree archon = HarbingerArtificerDialogueTrees.forState(42, artificer(7, false));
		assertFalse(hasOption(archon, "silent_vestments"));
		assertEquals(List.of("hemomancy.artificer.workshop.line1", "hemomancy.artificer.workshop.line2"),
				archon.getStartNode().lines());
	}

	@Test
	void theHermitKeepsTheArchonTreeWithOneSilentiumLine() {
		DialogueTree silentium = HarbingerHermitDialogueTrees.forState(8, true, true, false, true, 42);
		DialogueTree archon = HarbingerHermitDialogueTrees.forState(7, true, true, false, false, 42);
		assertEquals(List.of("hemomancy.hermit.archon.line1", "hemomancy.hermit.silentium.line2"),
				silentium.getStartNode().lines());
		assertEquals(archon.getStartNode().options(), silentium.getStartNode().options(),
				"the same choices as an Archon: he has nothing more to teach");
		assertEquals(List.of("hemomancy.hermit.apotheos.line1", "hemomancy.hermit.apotheos.line2"),
				HarbingerHermitDialogueTrees.forState(8, true, true, false, false, 42).getStartNode().lines());
	}

	@Test
	void everySilentiumLineHasEnglish() {
		List<DialogueTree> trees = List.of(HarbingerVicarDialogueTrees.silentium(42, true, false, true),
				HarbingerAlchemistDialogueTrees.forDegree(8, 42, true, false, null, null, false, false, false, false, true),
				mnemonist(8, true), HarbingerArtificerDialogueTrees.forState(42, artificer(8, true)),
				HarbingerHermitDialogueTrees.forState(8, true, true, false, true, 42));
		for (DialogueTree tree : trees) {
			tree.nodes().values().forEach(node -> {
				node.lines().forEach(EnglishText::of);
				node.options().forEach(option -> EnglishText.of(option.text()));
			});
		}
	}

	private static DialogueTree mnemonist(int degree, boolean silentium) {
		return HarbingerMnemonistDialogueTrees.forDegree(degree, 42, false, false, false, true, false, false, false,
				false, false, silentium);
	}

	private static ArtificerProgressSnapshot artificer(int degree, boolean silentium) {
		return new ArtificerProgressSnapshot(degree, true, false, false, false, ForkFamily.NONE, D7Lineage.NONE,
				Step.LOCKED, Step.LOCKED, Step.LOCKED, Step.LOCKED, Step.LOCKED, false, false, false, false, false, false,
				silentium);
	}

	private static List<String> lines(DialogueTree tree, String... nodes) {
		List<String> lines = new ArrayList<>();
		for (String node : nodes) lines.addAll(tree.getNode(node).lines());
		return lines;
	}

	private static boolean hasOption(DialogueTree tree, String nextNode) {
		return tree.getStartNode().options().stream().anyMatch(option -> nextNode.equals(option.nextNodeId()));
	}
}
