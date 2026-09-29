package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.Step;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentLessonDialogueTest {
    @Test
    void staffConstructionBeginsAtSecondDegree() {
        assertFalse(hasOption(HarbingerArtificerDialogueTrees.forState(42, progress(1)), "living_staff"));
        assertTrue(hasOption(HarbingerArtificerDialogueTrees.forState(42, progress(2)), "living_staff"));
    }

    @Test
    void vicarTeachesRootedVeinAtSecondDegree() {
        DialogueTree tree = HarbingerVicarDialogueTrees.votary(42);
        assertTrue(hasOption(tree, "rooted_vein"));
        assertTrue(tree.getNode("rooted_vein").lines().contains("hemomancy.vicar.votary.rooted_vein.floor"));
    }

    @Test
    void firstGourdLessonBeginsAtThirdDegree() {
        assertFalse(hasOption(HarbingerAlchemistDialogueTrees.votary(42), "pallid_vessel"));
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.initiate(42), "pallid_vessel"));
    }

    @Test
    void laterVesselLessonsFollowTheirRiteDegrees() {
        assertFalse(hasOption(HarbingerAlchemistDialogueTrees.initiate(42), "crimson_vessel"));
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.adept(42), "crimson_vessel"));
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.illuminatus(42, false, false), "ashen_vessel"));
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.sanctified(42, false, false), "curved_horn"));
    }

    @Test
    void specimenConversationKeepsTheCurrentVesselLesson() {
        DialogueTree specimenConversation = HarbingerAlchemistDialogueTrees.votary(42);
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.withCurrentVesselLesson(
                specimenConversation, 3, 42), "pallid_vessel"));
        assertTrue(hasOption(HarbingerAlchemistDialogueTrees.withCurrentVesselLesson(
                specimenConversation, 5, 42), "ashen_vessel"));
        assertFalse(hasOption(HarbingerAlchemistDialogueTrees.withCurrentVesselLesson(
                specimenConversation, 2, 42), "pallid_vessel"));
    }

    private static boolean hasOption(DialogueTree tree, String node) {
        return tree.getStartNode().options().stream().anyMatch(option -> node.equals(option.nextNodeId()));
    }

    private static ArtificerProgressSnapshot progress(int degree) {
        return new ArtificerProgressSnapshot(degree, true, false, false, false,
                ForkFamily.NONE, D7Lineage.NONE, Step.LOCKED, Step.LOCKED, Step.LOCKED,
                Step.LOCKED, Step.LOCKED, false, false, false, false, false, false);
    }
}
