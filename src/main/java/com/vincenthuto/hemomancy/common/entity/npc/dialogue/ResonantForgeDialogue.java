package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class ResonantForgeDialogue {
    public static final String TEACH = "artificer_resonant_forge_teach";
    public static final String PRECISION_CLAIM = "artificer_precision_governor_claim";
    public static final String MASTER_CLAIM = "artificer_master_cam_claim";

    private ResonantForgeDialogue() {}

    public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        if (degree < 3) return tree;
        var progress = HemoCapabilityAccess.resonantForge(player);
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());

        options.add(0, option("hemomancy.artificer.resonant_forge.title", "resonant_forge",
                progress.taught() ? null : TEACH, !progress.taught()));
        nodes.put("resonant_forge", node("resonant_forge", progress.taught()
                ? "hemomancy.artificer.resonant_forge.taught" : "hemomancy.artificer.resonant_forge.lesson"));
        if (degree >= 5) {
            boolean ready = progress.canClaimPrecision(player);
            options.add(0, option("hemomancy.artificer.resonant_forge.precision.title",
                    "resonant_forge_precision", ready ? PRECISION_CLAIM : null, ready));
            nodes.put("resonant_forge_precision", node("resonant_forge_precision",
                    progress.precisionClaimed() ? "hemomancy.artificer.resonant_forge.precision.claimed"
                            : ready ? "hemomancy.artificer.resonant_forge.precision.ready"
                            : "hemomancy.artificer.resonant_forge.precision.requirements"));
        }
        if (degree >= 7) {
            boolean ready = progress.canClaimMaster(player);
            options.add(0, option("hemomancy.artificer.resonant_forge.master.title",
                    "resonant_forge_master", ready ? MASTER_CLAIM : null, ready));
            nodes.put("resonant_forge_master", node("resonant_forge_master",
                    progress.masterClaimed() ? "hemomancy.artificer.resonant_forge.master.claimed"
                            : ready ? "hemomancy.artificer.resonant_forge.master.ready"
                            : "hemomancy.artificer.resonant_forge.master.requirements"));
        }
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }

    private static DialogueOption option(String label, String next, String event, boolean attention) {
        return new DialogueOption(label, next, event, attention
                ? DialogueOptionPresentation.attention(DialogueAttention.NOTICE)
                : DialogueOptionPresentation.normal());
    }

    private static DialogueNode node(String id, String line) {
        return new DialogueNode(id, List.of(line),
                List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
    }
}
