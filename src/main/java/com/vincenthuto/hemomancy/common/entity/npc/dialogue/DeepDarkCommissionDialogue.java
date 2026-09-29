package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class DeepDarkCommissionDialogue {
    public static final String REPORT = "alchemist_deep_dark_commission_report";

    private DeepDarkCommissionDialogue() {
    }

    public static DialogueTree withAlchemistCommission(DialogueTree tree, int degree, boolean proof,
            boolean reported) {
        if (degree < 5 || reported) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        DialogueNode start = tree.getStartNode();
        var options = new ArrayList<>(start.options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(
                "hemomancy.dialogue.alchemist.option.deep_dark_commission", "deep_dark_commission", null));
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
        var reportOptions = new ArrayList<DialogueOption>();
        if (proof) reportOptions.add(new DialogueOption(
                "hemomancy.dialogue.alchemist.option.report_deep_dark_commission", null, REPORT,
                DialogueOptionPresentation.prompt("hemomancy.alchemist.deep_dark_commission.report.prompt")));
        reportOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
        nodes.put("deep_dark_commission", new DialogueNode("deep_dark_commission", List.of(
                proof ? "hemomancy.alchemist.deep_dark_commission.ready"
                        : "hemomancy.alchemist.deep_dark_commission.lead",
                "hemomancy.alchemist.deep_dark_commission.task"), reportOptions));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
