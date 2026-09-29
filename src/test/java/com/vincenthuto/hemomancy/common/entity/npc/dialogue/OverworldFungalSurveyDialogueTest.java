package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverworldFungalSurveyDialogueTest {
    @Test
    void vicarReferralStartsAtDegreeTwoAndRecognizesEarlyVisit() {
        DialogueTree tree = HarbingerVicarDialogueTrees.votary(42);
        assertFalse(hasOption(OverworldFungalSurveyDialogue.withVicarReferral(tree, 1, false, false),
                "overworld_fungal_survey_referral"));
        DialogueTree visited = OverworldFungalSurveyDialogue.withVicarReferral(tree, 2, true, false);
        assertTrue(visited.getNode("overworld_fungal_survey_referral").lines().contains(
                "hemomancy.vicar.fungal_survey.visited"));
        DialogueTree hub = DialogueHubFactory.decorate(visited, "vicar",
                (com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.DialogueKnowledge) null);
        assertTrue(hub.presentation().topics(DialogueCategory.QUESTS).stream()
                .anyMatch(topic -> "overworld_fungal_survey_referral".equals(topic.targetNodeId())));
    }

    @Test
    void alchemistReportNeedsVisitAndBothSpecimens() {
        DialogueTree tree = HarbingerAlchemistDialogueTrees.votary(42);
        assertFalse(hasReport(OverworldFungalSurveyDialogue.withAlchemistReport(tree, 2, false, 2, false)));
        assertFalse(hasReport(OverworldFungalSurveyDialogue.withAlchemistReport(tree, 2, true, 1, false)));
        assertTrue(hasReport(OverworldFungalSurveyDialogue.withAlchemistReport(tree, 2, true, 2, false)));
        assertFalse(hasOption(OverworldFungalSurveyDialogue.withAlchemistReport(tree, 2, true, 2, true),
                "overworld_fungal_survey_report"));
    }

    private static boolean hasReport(DialogueTree tree) {
        DialogueNode node = tree.getNode("overworld_fungal_survey_report");
        return node != null && node.options().stream().anyMatch(option ->
                OverworldFungalSurveyDialogue.REPORT.equals(option.eventId()));
    }

    private static boolean hasOption(DialogueTree tree, String node) {
        return tree.getStartNode().options().stream().anyMatch(option -> node.equals(option.nextNodeId()));
    }
}
