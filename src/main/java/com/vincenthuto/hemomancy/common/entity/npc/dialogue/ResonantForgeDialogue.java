package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class ResonantForgeDialogue {
    private ResonantForgeDialogue() {}

    public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        if (degree < 3) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());

        options.add(0, new DialogueOption("hemomancy.artificer.resonant_forge.title", "resonant_forge", null,
                DialogueOptionPresentation.prompt("hemomancy.artificer.resonant_forge.prompt")));
        nodes.put("resonant_forge", forgeNode(degree));
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        DialogueTree withLesson = new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
        return StationUpgradeDialogue.append(withLesson, player, UpgradeStation.RESONANT_FORGE,
                "hemomancy.dialogue.artificer.option.leave");
    }

    private static DialogueNode forgeNode(int degree) {
        List<String> lines = new ArrayList<>();
        if (degree == 3) {
            lines.add("hemomancy.artificer.resonant_forge.early");
        } else {
            lines.add("hemomancy.artificer.resonant_forge.lesson");
            lines.add("hemomancy.artificer.resonant_forge.taught");
            if (degree >= 6) lines.add("hemomancy.artificer.resonant_forge.practice");
        }
        return new DialogueNode("resonant_forge", lines,
                List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
    }
}
