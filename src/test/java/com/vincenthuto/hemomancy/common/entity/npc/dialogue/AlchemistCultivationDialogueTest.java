package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlchemistCultivationDialogueTest {
    @Test
    void fourthDegreeTeachesCultivationBeforeIncubation() {
        DialogueTree initiate = HarbingerAlchemistDialogueTrees.initiate(42);
        DialogueTree adept = HarbingerAlchemistDialogueTrees.adept(42);
        assertFalse(hasOption(initiate, "lantern_cultivation"));
        assertTrue(hasOption(adept, "lantern_cultivation"));
        assertTrue(hasOption(adept, "wild_morphlings"));
        assertFalse(hasOption(adept, "incubator_lore"));
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.illuminatus(42, false, false), "incubator_lore"));
    }

    private static boolean hasOption(DialogueTree tree, String node) {
        return tree.getStartNode().options().stream().anyMatch(option -> node.equals(option.nextNodeId()));
    }
}
