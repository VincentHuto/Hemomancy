package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusReferralDialogueTest {
    @Test
    void vicarRefersD4PlayersAndRecognizesEarlierDiscovery() {
        DialogueTree base = HarbingerVicarDialogueTrees.votary(42);
        assertFalse(hasReferral(CircusReferralDialogue.withVicarReferral(base, 3, false)));

        DialogueTree newVisitor = CircusReferralDialogue.withVicarReferral(base, 4, false);
        assertTrue(hasReferral(newVisitor));
        assertTrue(newVisitor.getNode("circus_referral").lines().contains(
                "hemomancy.vicar.circus_referral.lead"));

        DialogueTree priorVisitor = CircusReferralDialogue.withVicarReferral(base, 4, true);
        assertTrue(hasReferral(priorVisitor));
        assertTrue(priorVisitor.getNode("circus_referral").lines().contains(
                "hemomancy.vicar.circus_referral.discovered"));
        assertTrue(DialogueHubFactory.decorate(priorVisitor, "vicar",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null)
                .presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "circus_referral".equals(topic.targetNodeId())));
    }

    private static boolean hasReferral(DialogueTree tree) {
        return tree.getStartNode().options().stream()
                .anyMatch(option -> "circus_referral".equals(option.nextNodeId()));
    }
}
