package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class PhlegethonticCommissionDialogue {
    public static final String REPORT = "alchemist_phlegethontic_commission_report";
    public static final String BEARING = "alchemist_phlegethontic_commission_bearing";

    private PhlegethonticCommissionDialogue() {
    }

    public static DialogueTree withAlchemistCommission(DialogueTree tree, int degree,
            boolean sampleProof, int scyphusCount, boolean reported) {
        if (degree < 5 || reported) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        DialogueNode start = tree.getStartNode();
        var options = new ArrayList<>(start.options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(
                "hemomancy.dialogue.alchemist.option.phlegethontic_commission",
                "phlegethontic_commission", null));
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));

        var reportOptions = new ArrayList<DialogueOption>();
        reportOptions.add(new DialogueOption(
                "hemomancy.dialogue.alchemist.option.phlegethontic_bearing", null, BEARING));
        if (sampleProof && scyphusCount >= 5) reportOptions.add(new DialogueOption(
                "hemomancy.dialogue.alchemist.option.report_phlegethontic_commission", null, REPORT,
                DialogueOptionPresentation.prompt("hemomancy.alchemist.phlegethontic_commission.report.prompt")));
        reportOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
        nodes.put("phlegethontic_commission", new DialogueNode("phlegethontic_commission", List.of(
                sampleProof ? "hemomancy.alchemist.phlegethontic_commission.sample_ready"
                        : "hemomancy.alchemist.phlegethontic_commission.lead",
                "hemomancy.alchemist.phlegethontic_commission.task"), reportOptions));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
