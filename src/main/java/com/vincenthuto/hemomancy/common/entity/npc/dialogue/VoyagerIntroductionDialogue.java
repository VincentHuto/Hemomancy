package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class VoyagerIntroductionDialogue {
    public static final String OBSERVE = "voyager_introduction_observe";
    public static final String REPORT = "vicar_voyager_introduction_report";
    public static final String BEARING = "vicar_voyager_introduction_bearing";

    private VoyagerIntroductionDialogue() {}

    public static DialogueTree withFieldObservation(DialogueTree tree, boolean available) {
        if (!available) return tree;
        DialogueNode work = tree.getNode("work");
        if (work == null) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        var options = new ArrayList<>(work.options());
        options.addFirst(new DialogueOption("hemomancy.dialogue.voyager.option.record_observation", null,
                OBSERVE, DialogueOptionPresentation.prompt("hemomancy.voyager.introduction.prompt")));
        nodes.put(work.id(), new DialogueNode(work.id(), work.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }

    public static DialogueTree withVicarReport(DialogueTree tree, int degree, boolean observed,
            boolean reported) {
        if (degree < 3 || reported) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        var options = new ArrayList<>(tree.getStartNode().options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(
                "hemomancy.dialogue.vicar.option.voyager_introduction", "voyager_introduction", null));
        DialogueNode start = tree.getStartNode();
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
        var reportOptions = new ArrayList<DialogueOption>();
        if (!observed) reportOptions.add(new DialogueOption(
                "hemomancy.dialogue.vicar.option.voyager_bearing", null, BEARING));
        if (observed) reportOptions.add(new DialogueOption(
                "hemomancy.dialogue.vicar.option.report_voyager_observation", null, REPORT,
                DialogueOptionPresentation.prompt("hemomancy.vicar.voyager_introduction.report.prompt")));
        reportOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
        nodes.put("voyager_introduction", new DialogueNode("voyager_introduction", List.of(
                observed ? "hemomancy.vicar.voyager_introduction.observed"
                        : "hemomancy.vicar.voyager_introduction.lead",
                "hemomancy.vicar.voyager_introduction.task"), reportOptions));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
