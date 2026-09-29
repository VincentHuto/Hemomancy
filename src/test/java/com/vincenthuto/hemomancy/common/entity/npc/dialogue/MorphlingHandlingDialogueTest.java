package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphlingHandlingDialogueTest {
    @Test
    void alchemistOffersInspectionOnlyForD4PolypProof() {
        DialogueTree base = HarbingerAlchemistDialogueTrees.adept(42);
        DialogueTree ready = MorphlingHandlingDialogue.withAlchemistInspection(base, 4, true, false);
        assertTrue(ready.getNode("morphling_handling").options().stream()
                .anyMatch(option -> MorphlingHandlingDialogue.INSPECT.equals(option.eventId())));
        DialogueTree waiting = MorphlingHandlingDialogue.withAlchemistInspection(base, 4, false, false);
        assertFalse(waiting.getNode("morphling_handling").options().stream()
                .anyMatch(option -> MorphlingHandlingDialogue.INSPECT.equals(option.eventId())));
        assertFalse(MorphlingHandlingDialogue.withAlchemistInspection(base, 3, true, false)
                .nodes().containsKey("morphling_handling"));
        assertFalse(MorphlingHandlingDialogue.withAlchemistInspection(base, 4, true, true)
                .nodes().containsKey("morphling_handling"));
        DialogueTree hub = DialogueHubFactory.decorate(ready, "alchemist",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null);
        assertTrue(hub.presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "morphling_handling".equals(topic.targetNodeId())));
    }
}
