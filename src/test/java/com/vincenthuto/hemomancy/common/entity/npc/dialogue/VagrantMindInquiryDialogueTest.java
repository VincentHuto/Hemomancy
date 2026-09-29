package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VagrantMindInquiryDialogueTest {
    @Test
    void sharedDiscoveryOpensTwoIndependentReports() {
        DialogueTree mnemonist = HarbingerMnemonistDialogueTrees.forDegree(6, 42,
                false, false, false);
        DialogueTree alchemist = HarbingerAlchemistDialogueTrees.adept(43);
        var unseen = new VagrantMindInquiryProgress(false, false, false, false);
        var visited = new VagrantMindInquiryProgress(true, false, false, false);
        var observed = new VagrantMindInquiryProgress(true, true, false, false);

        assertTrue(hasEvent(VagrantMindInquiryDialogue.memory(mnemonist, unseen),
                "vagrant_memory_inquiry", VagrantMindInquiryDialogue.BEARING));
        assertFalse(hasEvent(VagrantMindInquiryDialogue.memory(mnemonist, unseen),
                "vagrant_memory_inquiry", VagrantMindInquiryDialogue.MEMORY_REPORT));
        assertTrue(hasEvent(VagrantMindInquiryDialogue.memory(mnemonist, visited),
                "vagrant_memory_inquiry", VagrantMindInquiryDialogue.MEMORY_REPORT));
        assertFalse(hasEvent(VagrantMindInquiryDialogue.biology(alchemist, visited),
                "vagrant_biology_inquiry", VagrantMindInquiryDialogue.BIOLOGY_REPORT));
        DialogueTree readyBiology = VagrantMindInquiryDialogue.biology(alchemist, observed);
        assertTrue(hasEvent(readyBiology, "vagrant_biology_inquiry", VagrantMindInquiryDialogue.BIOLOGY_REPORT));
        assertTrue(DialogueHubFactory.decorate(readyBiology, "alchemist",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null)
                .presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "vagrant_biology_inquiry".equals(topic.targetNodeId())));
        assertTrue(VagrantMindInquiryDialogue.memory(mnemonist,
                new VagrantMindInquiryProgress(true, true, true, false))
                .getNode("vagrant_memory_inquiry") == null);
        assertTrue(VagrantMindInquiryDialogue.biology(alchemist,
                new VagrantMindInquiryProgress(true, true, false, true))
                .getNode("vagrant_biology_inquiry") == null);
    }

    private static boolean hasEvent(DialogueTree tree, String nodeId, String eventId) {
        return tree.getNode(nodeId).options().stream().anyMatch(option -> eventId.equals(option.eventId()));
    }
}
