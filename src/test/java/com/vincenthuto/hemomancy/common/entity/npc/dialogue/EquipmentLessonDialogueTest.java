package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.Step;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquipmentLessonDialogueTest {
    @Test
    void staffConstructionBeginsAtSecondDegree() {
        assertFalse(hasOption(HarbingerArtificerDialogueTrees.forState(42, progress(1)), "living_staff"));
        assertTrue(hasOption(HarbingerArtificerDialogueTrees.forState(42, progress(2)), "living_staff"));
    }

    @Test
    void staffLessonNamesUtilitySelectionAndTheEarnedBond() throws Exception {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        String lesson = language.get("hemomancy.artificer.living_staff.use").getAsString();
        assertTrue(lesson.contains("Select Blood Absorption or Blood Projection"),
                "Staff lesson must explain which selected memory enables its utility channel");
        assertTrue(lesson.contains("Conjure Staff"), "Staff lesson must explain how to recall the earned bond");
    }

    @Test
    void vicarTeachesRootedVeinAtSecondDegree() {
        DialogueTree tree = HarbingerVicarDialogueTrees.votary(42);
        assertTrue(hasOption(tree, "rooted_vein"));
        assertTrue(tree.getNode("rooted_vein").lines().contains("hemomancy.vicar.votary.rooted_vein.floor"));
    }

    @Test
    void rootedVeinLessonNamesTheActualProjectionAndRecoveryActions() throws Exception {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        var tree = HarbingerVicarDialogueTrees.votary(42);
        assertTrue(tree.getNode("rooted_vein").options().stream()
                .anyMatch(option -> "rooted_vein_channel".equals(option.nextNodeId())));
        String lesson = java.util.stream.Stream.concat(tree.getNode("rooted_vein").lines().stream(),
                        tree.getNode("rooted_vein_channel").lines().stream())
                .map(key -> language.get(key).getAsString()).collect(java.util.stream.Collectors.joining(" "));
        assertTrue(lesson.contains("light"), "Rooted Vein lesson must explain lighting the offering");
        assertTrue(lesson.contains("Blood Projection") && lesson.contains("four anchors") && lesson.contains("daemon"),
                "Rooted Vein lesson must name anchor payment and daemon sealing");
        assertTrue(lesson.contains("Blood Absorption"), "Rooted Vein lesson must explain Staff recovery");
        assertTrue(lesson.contains("above the Focus clear"), "Rooted Vein lesson must identify the result's required space");
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

    @Test
    void everyVesselLessonKeepsReachableFillAndWithdrawalControls() throws Exception {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        String[] lessons = {"pallid_vessel", "crimson_vessel", "ashen_vessel", "curved_horn"};
        for (int degree = 3; degree <= 6; degree++) {
            for (DialogueTree tree : java.util.List.of(
                    HarbingerAlchemistDialogueTrees.forDegree(degree, 42, false, false),
                    HarbingerAlchemistDialogueTrees.withCurrentVesselLesson(
                            HarbingerAlchemistDialogueTrees.votary(42), degree, 42))) {
                var vessel = tree.getNode(lessons[degree - 3]);
                assertTrue(vessel.options().stream().anyMatch(option -> "gourd_use".equals(option.nextNodeId())),
                        "Each current vessel lesson must lead to its practical controls");
                var controls = tree.getNode("gourd_use");
                assertTrue(controls != null, "Specimen conversations must copy the controls node too");
                String text = controls.lines().stream().map(key -> language.get(key).getAsString())
                        .collect(java.util.stream.Collectors.joining(" "));
                assertTrue(text.contains("blood-output slot") && text.contains("Scarlet Vanity"));
                assertTrue(text.contains("Use the gourd in your hand") && text.contains("Toggle Gourd Open/Closed"),
                        "The lesson must name both ordinary hand use and equipped toggle");
                assertTrue(text.contains("active") && text.contains("cork"),
                        "The lesson must explain active blood and stopping withdrawal");
            }
        }
    }

    @Test
    void laterVesselLessonsNameTheirRequiredBastion() throws Exception {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        String[] keys = {"adept.crimson_vessel", "illuminatus.ashen_vessel", "sanctified.curved_horn"};
        for (String key : keys) {
            assertTrue(language.get("hemomancy.alchemist." + key).getAsString().contains("Bastion"),
                    "The vessel lesson must name the sigil required before sealing: " + key);
        }
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
