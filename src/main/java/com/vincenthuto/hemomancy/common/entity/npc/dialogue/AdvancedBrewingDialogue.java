package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class AdvancedBrewingDialogue {
    private AdvancedBrewingDialogue() {}

    public static DialogueTree append(DialogueTree tree, ServerPlayer player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        if (degree < 3) return tree;
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());
        options.add(new DialogueOption("hemomancy.alchemist.preparations.title",
                "thelemic_preparations", null));
        String preparationState = HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_COMPLETE)
                ? "hemomancy.alchemist.preparations.complete"
                : HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_BRIEFED)
                        ? "hemomancy.alchemist.preparations.briefed"
                        : "hemomancy.alchemist.preparations.unbriefed";
        nodes.put("thelemic_preparations", new DialogueNode("thelemic_preparations", List.of(
                preparationState, "hemomancy.alchemist.preparations.alembic",
                "hemomancy.alchemist.preparations.capacity"),
                List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        DialogueTree withPreparations = new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(),
                nodes, tree.entityId(), tree.theme(), tree.presentation());
        var withAlembic = StationUpgradeDialogue.append(withPreparations, player, UpgradeStation.ALEMBIC,
                "hemomancy.dialogue.alchemist.option.leave");
        return StationUpgradeDialogue.append(withAlembic, player, UpgradeStation.CENTRIFUGE,
                "hemomancy.dialogue.alchemist.option.leave");
    }
}
