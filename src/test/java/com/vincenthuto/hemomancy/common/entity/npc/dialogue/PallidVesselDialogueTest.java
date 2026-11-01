package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.alchemist.PallidVesselProgress;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PallidVesselDialogueTest {
	private static final PallidVesselProgress BRIEFED = PallidVesselProgress.NONE.withBriefed();
	private static final PallidVesselProgress READY = BRIEFED.withRite().withFill();

	@Test
	void initiateOffersTheVesselAsAQuest() {
		DialogueTree tree = HarbingerAlchemistDialogueTrees.initiate(42);
		DialogueOption option = vesselOption(tree);
		assertTrue(option.presentation().quest());
		assertTrue(tree.getNode(PallidVesselDialogue.NODE).options().stream()
				.anyMatch(choice -> PallidVesselDialogue.BRIEF.equals(choice.eventId())));
	}

	@Test
	void nodeFollowsProgress() {
		DialogueTree base = HarbingerAlchemistDialogueTrees.initiate(42);
		assertEquals(PallidVesselDialogue.BRIEFING, lines(base, PallidVesselProgress.NONE));
		assertEquals("hemomancy.alchemist.pallid_vessel.progress.rite", lines(base, BRIEFED).get(0));
		assertEquals("hemomancy.alchemist.pallid_vessel.progress.fill", lines(base, BRIEFED.withRite()).get(0));
		DialogueTree ready = PallidVesselDialogue.withAlchemistQuest(base, READY);
		assertTrue(ready.getNode(PallidVesselDialogue.NODE).options().stream()
				.anyMatch(choice -> PallidVesselDialogue.REPORT.equals(choice.eventId())));
		assertEquals(DialogueAttention.URGENT, vesselOption(ready).presentation().attention());
		DialogueTree complete = PallidVesselDialogue.withAlchemistQuest(base, READY.withReport());
		assertFalse(vesselOption(complete).presentation().quest(), "a finished vessel stays as a plain lesson");
		assertFalse(complete.getNode(PallidVesselDialogue.NODE).options().stream()
				.anyMatch(choice -> choice.eventId() != null));
	}

	@Test
	void laterDegreesStillOfferAnUnfinishedVessel() {
		DialogueTree adept = HarbingerAlchemistDialogueTrees.adept(42);
		DialogueTree offered = PallidVesselDialogue.withAlchemistQuest(adept, BRIEFED);
		assertTrue(offered.getStartNode().options().stream()
				.anyMatch(choice -> PallidVesselDialogue.NODE.equals(choice.nextNodeId())));
		assertEquals(1, offered.getStartNode().options().stream()
				.filter(choice -> PallidVesselDialogue.NODE.equals(choice.nextNodeId())).count());
		DialogueTree finished = PallidVesselDialogue.withAlchemistQuest(adept, READY.withReport());
		assertFalse(finished.nodes().containsKey(PallidVesselDialogue.NODE));
	}

	@Test
	void briefingStaysOnTheVesselAndTheLoomKeepsItsOwnLesson() {
		DialogueTree tree = HarbingerAlchemistDialogueTrees.initiate(42);
		String briefing = PallidVesselDialogue.BRIEFING.stream().map(EnglishText::of)
				.collect(Collectors.joining(" "));
		assertTrue(briefing.contains("Dried Gourd") && briefing.contains("four anchors")
				&& briefing.contains("1,000 mL") && briefing.contains("blood-output slot"));
		assertFalse(briefing.contains("Somatic Loom"), "Loom raising belongs to the Loom topic");
		assertTrue(tree.getNode("loom_lore").options().stream()
				.anyMatch(choice -> "loom_raising".equals(choice.nextNodeId())));
		assertTrue(EnglishText.of("hemomancy.alchemist.initiate.loom_raising").contains("Sanguine Formation"));
	}

	private static java.util.List<String> lines(DialogueTree base, PallidVesselProgress progress) {
		return PallidVesselDialogue.withAlchemistQuest(base, progress).getNode(PallidVesselDialogue.NODE).lines();
	}

	private static DialogueOption vesselOption(DialogueTree tree) {
		return tree.getStartNode().options().stream()
				.filter(choice -> PallidVesselDialogue.NODE.equals(choice.nextNodeId())).findFirst().orElseThrow();
	}
}
