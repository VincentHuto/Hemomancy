package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.StationUpgradeRules;
import com.vincenthuto.hemomancy.common.station.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Claim options for station upgrade items, shared by every teaching NPC. */
public final class StationUpgradeDialogue {
    public static final String CLAIM_PREFIX = "station_upgrade_claim/";

    private StationUpgradeDialogue() {}

    public static String claimEvent(StationUpgradeTier tier) {
        return CLAIM_PREFIX + tier.station().serializedName() + "/" + tier.tier();
    }

    public static Optional<StationUpgradeTier> parseClaim(String eventId) {
        if (eventId == null || !eventId.startsWith(CLAIM_PREFIX)) return Optional.empty();
        String[] parts = eventId.substring(CLAIM_PREFIX.length()).split("/");
        if (parts.length != 2) return Optional.empty();
        UpgradeStation station = UpgradeStation.byName(parts[0]);
        if (station == null || !(parts[1].equals("1") || parts[1].equals("2"))) return Optional.empty();
        return Optional.of(StationUpgradeCatalog.get(station, Integer.parseInt(parts[1])));
    }

    /** Armature claims keep their bespoke Artificer and Monolith scenes, which call the same progress. */
    public static boolean allowedSpeaker(UpgradeStation station, Entity entity) {
        return switch (station) {
            case ALEMBIC, CENTRIFUGE -> entity instanceof HarbingerAlchemistEntity;
            case RESONANT_FORGE -> entity instanceof HarbingerArtificerEntity;
            case SCRIPTORIUM -> entity instanceof HarbingerMnemonistEntity;
            case ARMATURE -> false;
        };
    }

    public static DialogueTree append(DialogueTree tree, ServerPlayer player, UpgradeStation station, String leaveKey) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        var progress = HemoCapabilityAccess.stationUpgrades(player);
        var nodes = new LinkedHashMap<>(tree.nodes());
        var root = tree.getStartNode();
        var options = new ArrayList<>(root.options());
        for (int number = 2; number >= 1; number--) {
            StationUpgradeTier tier = StationUpgradeCatalog.get(station, number);
            if (degree < tier.requiredDegree()) continue;
            var state = progress.claimState(player, tier);
            boolean ready = state == StationUpgradeRules.ClaimState.READY;
            String key = tier.dialogueKey();
            String nodeId = "station_upgrade_" + station.serializedName() + "_" + number;
            options.add(0, new DialogueOption(key + ".title", nodeId, ready ? claimEvent(tier) : null,
                    ready ? DialogueOptionPresentation.attention(DialogueAttention.NOTICE, key + ".prompt")
                            : DialogueOptionPresentation.prompt(key + ".prompt")));
            String line = state == StationUpgradeRules.ClaimState.CLAIMED ? key + ".claimed"
                    : ready ? key + ".ready" : key + ".requirements";
            nodes.put(nodeId, new DialogueNode(nodeId, List.of(line),
                    List.of(new DialogueOption(leaveKey, null, null))));
        }
        nodes.put(root.id(), new DialogueNode(root.id(), root.lines(), options));
        return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
                tree.entityId(), tree.theme(), tree.presentation());
    }
}
