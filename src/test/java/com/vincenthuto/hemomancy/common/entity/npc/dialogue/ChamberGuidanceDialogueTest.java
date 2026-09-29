package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChamberGuidanceDialogueTest {
    @Test
    void unfinishedGuidanceIsAvailableAfterEarlyVisit() {
        DialogueTree tree = HarbingerMnemonistDialogueTrees.withGuidedChamber(baseTree(), 3, false, true);
        assertTrue(tree.getStartNode().options().stream()
                .anyMatch(option -> "guided_chamber".equals(option.nextNodeId())));
        assertTrue(tree.getNode("guided_chamber").lines()
                .contains("hemomancy.mnemonist.chamber.guided.familiar"));
        assertTrue(tree.getNode("guided_chamber").options().stream()
                .anyMatch(option -> HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER.equals(option.eventId())));
    }

    @Test
    void completedOrEarlyDegreeDoesNotOfferAnotherGuidedTrip() {
        assertFalse(HarbingerMnemonistDialogueTrees.withGuidedChamber(baseTree(), 2, false, false)
                .getStartNode().options().stream().anyMatch(option -> "guided_chamber".equals(option.nextNodeId())));
        assertFalse(HarbingerMnemonistDialogueTrees.withGuidedChamber(baseTree(), 5, true, false)
                .getStartNode().options().stream().anyMatch(option -> "guided_chamber".equals(option.nextNodeId())));
    }

    @Test
    void laterChamberInstructionMatchesSeatAndRiteStages() {
        DialogueTree adept = HarbingerMnemonistDialogueTrees.forDegree(4, 42, false, false, false);
        assertTrue(adept.getNode("chamber").lines()
                .contains("hemomancy.mnemonist.chamber.seat.controls"));
        DialogueTree illuminatus = HarbingerMnemonistDialogueTrees.forDegree(5, 42, false, false, false);
        assertTrue(illuminatus.getNode("chamber").lines()
                .contains("hemomancy.mnemonist.chamber.support"));
        DialogueTree sanctified = HarbingerMnemonistDialogueTrees.forDegree(6, 42, false, false, false);
        assertTrue(sanctified.getNode("chamber").lines()
                .contains("hemomancy.mnemonist.chamber.rite"));
    }

    private static DialogueTree baseTree() {
        return DialogueTree.builder("mnemonist", ResourceLocation.fromNamespaceAndPath("hemomancy", "test"), 42)
                .addNode(new DialogueNode("greeting", List.of("greeting"), List.of(
                        new DialogueOption("leave", null, null))))
                .build();
    }
}
