package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class AdvancedBrewingDialogue {
    public static final String CONDENSER_CLAIM = "alchemist_condenser_kit_claim";
    public static final String ATHANOR_CLAIM = "alchemist_athanor_kit_claim";

    private AdvancedBrewingDialogue() {}

    public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        if (degree < 3) return tree;
        var progress = HemoCapabilityAccess.advancedBrewing(player);
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());
        boolean condenserReady = progress.canClaimCondenser(player);
        options.add(0, new DialogueOption("hemomancy.alchemist.brewing.condenser.title",
                "advanced_condenser", condenserReady ? CONDENSER_CLAIM : null,
                condenserReady ? DialogueOptionPresentation.attention(DialogueAttention.NOTICE)
                        : DialogueOptionPresentation.normal()));
        nodes.put("advanced_condenser", new DialogueNode("advanced_condenser",
                List.of(progress.condenserClaimed()
                        ? "hemomancy.alchemist.brewing.condenser.claimed"
                        : condenserReady ? "hemomancy.alchemist.brewing.condenser.ready"
                        : "hemomancy.alchemist.brewing.condenser.requirements"),
                List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
        if (degree >= 5) {
            boolean athanorReady = progress.canClaimAthanor(player);
            options.add(0, new DialogueOption("hemomancy.alchemist.brewing.athanor.title",
                    "advanced_athanor", athanorReady ? ATHANOR_CLAIM : null,
                    athanorReady ? DialogueOptionPresentation.attention(DialogueAttention.NOTICE)
                            : DialogueOptionPresentation.normal()));
            nodes.put("advanced_athanor", new DialogueNode("advanced_athanor",
                    List.of(progress.athanorClaimed()
                            ? "hemomancy.alchemist.brewing.athanor.claimed"
                            : athanorReady ? "hemomancy.alchemist.brewing.athanor.ready"
                            : "hemomancy.alchemist.brewing.athanor.requirements"),
                    List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
        }
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
