package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class MorphlingHandlingDialogue {
    public static final String INSPECT = "alchemist_morphling_handling_inspect";

    private MorphlingHandlingDialogue() {}

    public static DialogueTree withAlchemistInspection(DialogueTree tree, int degree, boolean proof,
            boolean inspected) {
        if (degree < 4 || inspected) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        var start = tree.getStartNode();
        var options = new ArrayList<>(start.options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(
                "hemomancy.dialogue.alchemist.option.morphling_handling", "morphling_handling", null));
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
        var reportOptions = new ArrayList<DialogueOption>();
        if (proof) reportOptions.add(new DialogueOption(
                "hemomancy.dialogue.alchemist.option.inspect_morphling_handling", null, INSPECT,
                DialogueOptionPresentation.prompt("hemomancy.alchemist.morphling_handling.inspect.prompt")));
        reportOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
        nodes.put("morphling_handling", new DialogueNode("morphling_handling", List.of(
                proof ? "hemomancy.alchemist.morphling_handling.ready"
                        : "hemomancy.alchemist.morphling_handling.lead",
                "hemomancy.alchemist.morphling_handling.task"), reportOptions));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
