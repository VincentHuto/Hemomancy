package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class VagrantMindInquiryDialogue {
    public static final String MEMORY_REPORT = "mnemonist_vagrant_memory_report";
    public static final String BIOLOGY_REPORT = "alchemist_vagrant_biology_report";
    public static final String BEARING = "mnemonist_vagrant_mind_bearing";

    private VagrantMindInquiryDialogue() {}

    public static DialogueTree memory(DialogueTree tree, VagrantMindInquiryProgress progress) {
        if (progress.memoryReported()) return tree;
        var options = new ArrayList<DialogueOption>();
        options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.vagrant_bearing", null, BEARING));
        if (progress.memoryReady()) options.add(new DialogueOption(
                "hemomancy.dialogue.mnemonist.option.vagrant_memory_report", null, MEMORY_REPORT,
                DialogueOptionPresentation.prompt("hemomancy.vagrant_mind_inquiry.memory.prompt")));
        options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null));
        return append(tree, "vagrant_memory_inquiry", "hemomancy.dialogue.mnemonist.option.vagrant_inquiry",
                progress.mindVisited() ? "hemomancy.vagrant_mind_inquiry.memory_found"
                        : "hemomancy.vagrant_mind_inquiry.memory_lead", options);
    }

    public static DialogueTree biology(DialogueTree tree, VagrantMindInquiryProgress progress) {
        if (progress.biologyReported()) return tree;
        var options = new ArrayList<DialogueOption>();
        if (progress.biologyReady()) options.add(new DialogueOption(
                "hemomancy.dialogue.alchemist.option.vagrant_biology_report", null, BIOLOGY_REPORT,
                DialogueOptionPresentation.prompt("hemomancy.vagrant_mind_inquiry.biology.prompt")));
        options.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
        return append(tree, "vagrant_biology_inquiry", "hemomancy.dialogue.alchemist.option.vagrant_inquiry",
                progress.biologyReady() ? "hemomancy.vagrant_mind_inquiry.biology_ready"
                        : "hemomancy.vagrant_mind_inquiry.biology_lead", options);
    }

    private static DialogueTree append(DialogueTree tree, String nodeId, String optionKey,
            String leadKey, List<DialogueOption> reportOptions) {
        var nodes = new LinkedHashMap<>(tree.nodes());
        DialogueNode start = tree.getStartNode();
        var options = new ArrayList<>(start.options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(optionKey, nodeId, null));
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
        nodes.put(nodeId, new DialogueNode(nodeId, List.of(leadKey), reportOptions));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
