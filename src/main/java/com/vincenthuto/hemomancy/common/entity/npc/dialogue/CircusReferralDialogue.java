package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class CircusReferralDialogue {
    private CircusReferralDialogue() {
    }

    public static DialogueTree withVicarReferral(DialogueTree tree, int degree, boolean discovered) {
        if (degree < CircusIntroductionRules.MINIMUM_DEGREE) return tree;

        var nodes = new LinkedHashMap<>(tree.nodes());
        DialogueNode start = tree.getStartNode();
        var options = new ArrayList<>(start.options());
        options.add(Math.max(0, options.size() - 1), new DialogueOption(
                "hemomancy.dialogue.vicar.option.circus_referral", "circus_referral", null));
        nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), options));
        nodes.put("circus_referral", new DialogueNode("circus_referral", List.of(
                discovered ? "hemomancy.vicar.circus_referral.discovered"
                        : "hemomancy.vicar.circus_referral.lead",
                "hemomancy.vicar.circus_referral.mnemonist"), List.of(
                new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null))));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
