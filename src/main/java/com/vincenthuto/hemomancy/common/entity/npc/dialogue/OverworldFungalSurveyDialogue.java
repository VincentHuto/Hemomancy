package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class OverworldFungalSurveyDialogue {
    public static final String REPORT = "alchemist_overworld_fungal_survey_report";

    private OverworldFungalSurveyDialogue() {}

    public static DialogueTree withVicarReferral(DialogueTree tree, int degree, boolean visited,
            boolean reported) {
        if (degree < 2 || reported) return tree;
        return withOption(tree, "hemomancy.dialogue.vicar.option.fungal_survey",
                "overworld_fungal_survey_referral", new DialogueNode("overworld_fungal_survey_referral",
                        List.of(visited ? "hemomancy.vicar.fungal_survey.visited"
                                        : "hemomancy.vicar.fungal_survey.lead",
                                "hemomancy.vicar.fungal_survey.specimens"),
                        List.of(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null))));
    }

    public static DialogueTree withAlchemistReport(DialogueTree tree, int degree, boolean visited,
            int specimens, boolean reported) {
        if (degree < 2 || reported) return tree;
        List<DialogueOption> options = new ArrayList<>();
        if (visited && specimens >= 2) options.add(new DialogueOption(
                "hemomancy.dialogue.alchemist.option.report_fungal_survey", null, REPORT,
                DialogueOptionPresentation.prompt("hemomancy.alchemist.fungal_survey.report.prompt")));
        options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
        return withOption(tree, "hemomancy.dialogue.alchemist.option.fungal_survey",
                "overworld_fungal_survey_report", new DialogueNode("overworld_fungal_survey_report",
                        List.of(!visited ? "hemomancy.alchemist.fungal_survey.visit"
                                : specimens < 2 ? "hemomancy.alchemist.fungal_survey.specimens"
                                : "hemomancy.alchemist.fungal_survey.ready"), options));
    }

    private static DialogueTree withOption(DialogueTree tree, String label, String nodeId, DialogueNode node) {
        DialogueNode start = tree.getStartNode();
        var nodes = new LinkedHashMap<>(tree.nodes());
        nodes.put(nodeId, node);
        var options = new ArrayList<>(start.options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(label, nodeId, null));
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
