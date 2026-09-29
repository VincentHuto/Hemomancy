package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoyagerIntroductionDialogueTest {
    @Test
    void observationIsAnExplicitVoyagerChoice() {
        DialogueTree base = HarbingerVoyagerDialogueTrees.forDegree(2, 42);
        assertFalse(hasEvent(base.getNode("work"), VoyagerIntroductionDialogue.OBSERVE));
        DialogueTree available = VoyagerIntroductionDialogue.withFieldObservation(base, true);
        assertTrue(hasEvent(available.getNode("work"), VoyagerIntroductionDialogue.OBSERVE));
        assertFalse(hasEvent(VoyagerIntroductionDialogue.withFieldObservation(base, false).getNode("work"),
                VoyagerIntroductionDialogue.OBSERVE));
    }

    @Test
    void vicarRecognizesObservationMadeBeforeReferral() {
        DialogueTree base = HarbingerVicarDialogueTrees.votary(42);
        DialogueTree early = VoyagerIntroductionDialogue.withVicarReport(base, 2, true, false);
        assertFalse(hasOption(early, "voyager_introduction"));
        DialogueTree ready = VoyagerIntroductionDialogue.withVicarReport(base, 3, true, false);
        assertTrue(hasEvent(ready.getNode("voyager_introduction"), VoyagerIntroductionDialogue.REPORT));
        assertTrue(ready.getNode("voyager_introduction").lines().contains(
                "hemomancy.vicar.voyager_introduction.observed"));
        DialogueTree hub = DialogueHubFactory.decorate(ready, "vicar",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null);
        assertTrue(hub.presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "voyager_introduction".equals(topic.targetNodeId())));
        assertFalse(hasOption(VoyagerIntroductionDialogue.withVicarReport(base, 3, true, true),
                "voyager_introduction"));
    }

    private static boolean hasEvent(DialogueNode node, String eventId) {
        return node.options().stream().anyMatch(option -> eventId.equals(option.eventId()));
    }

    private static boolean hasOption(DialogueTree tree, String nodeId) {
        return tree.getStartNode().options().stream().anyMatch(option -> nodeId.equals(option.nextNodeId()));
    }
}
