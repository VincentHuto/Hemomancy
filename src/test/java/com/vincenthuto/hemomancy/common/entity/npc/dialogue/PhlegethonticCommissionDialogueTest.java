package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhlegethonticCommissionDialogueTest {
    @Test
    void d5LeadOffersBearingAndOnlyReadyProofCanBeReported() {
        DialogueTree base = HarbingerAlchemistDialogueTrees.adept(42);
        assertFalse(hasCommission(PhlegethonticCommissionDialogue.withAlchemistCommission(
                base, 4, true, 5, false)));

        DialogueTree lead = PhlegethonticCommissionDialogue.withAlchemistCommission(base, 5, false, 0, false);
        assertTrue(hasCommission(lead));
        assertTrue(hasEvent(lead, PhlegethonticCommissionDialogue.BEARING));
        assertFalse(hasEvent(lead, PhlegethonticCommissionDialogue.REPORT));

        DialogueTree shortCount = PhlegethonticCommissionDialogue.withAlchemistCommission(base, 5, true, 4, false);
        assertFalse(hasEvent(shortCount, PhlegethonticCommissionDialogue.REPORT));
        DialogueTree ready = PhlegethonticCommissionDialogue.withAlchemistCommission(base, 5, true, 5, false);
        assertTrue(hasEvent(ready, PhlegethonticCommissionDialogue.REPORT));
        assertTrue(DialogueHubFactory.decorate(ready, "alchemist",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null)
                .presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "phlegethontic_commission".equals(topic.targetNodeId())));
        assertFalse(hasCommission(PhlegethonticCommissionDialogue.withAlchemistCommission(
                base, 5, true, 5, true)));
    }

    private static boolean hasCommission(DialogueTree tree) {
        return tree.getStartNode().options().stream()
                .anyMatch(option -> "phlegethontic_commission".equals(option.nextNodeId()));
    }

    private static boolean hasEvent(DialogueTree tree, String eventId) {
        return tree.getNode("phlegethontic_commission").options().stream()
                .anyMatch(option -> eventId.equals(option.eventId()));
    }
}
