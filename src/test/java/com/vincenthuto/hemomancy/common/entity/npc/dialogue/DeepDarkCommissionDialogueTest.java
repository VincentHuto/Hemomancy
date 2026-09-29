package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepDarkCommissionDialogueTest {
    @Test
    void d5CommissionRecognizesPriorAnalysisWithoutClosingTheAntecedentInquiry() {
        DialogueTree base = HarbingerAlchemistDialogueTrees.adept(42);
        assertFalse(hasCommission(DeepDarkCommissionDialogue.withAlchemistCommission(base, 4, true, false)));

        DialogueTree lead = DeepDarkCommissionDialogue.withAlchemistCommission(base, 5, false, false);
        assertTrue(hasCommission(lead));
        assertTrue(lead.getNode("deep_dark_commission").lines().contains(
                "hemomancy.alchemist.deep_dark_commission.lead"));
        assertFalse(hasReport(lead));

        DialogueTree ready = DeepDarkCommissionDialogue.withAlchemistCommission(base, 5, true, false);
        assertTrue(hasReport(ready));
        assertTrue(ready.getNode("deep_dark_commission").lines().contains(
                "hemomancy.alchemist.deep_dark_commission.ready"));
        assertTrue(DialogueHubFactory.decorate(ready, "alchemist",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null)
                .presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "deep_dark_commission".equals(topic.targetNodeId())));
        assertFalse(hasCommission(DeepDarkCommissionDialogue.withAlchemistCommission(base, 5, true, true)));
    }

    private static boolean hasCommission(DialogueTree tree) {
        return tree.getStartNode().options().stream()
                .anyMatch(option -> "deep_dark_commission".equals(option.nextNodeId()));
    }

    private static boolean hasReport(DialogueTree tree) {
        return tree.getNode("deep_dark_commission").options().stream()
                .anyMatch(option -> DeepDarkCommissionDialogue.REPORT.equals(option.eventId()));
    }
}
