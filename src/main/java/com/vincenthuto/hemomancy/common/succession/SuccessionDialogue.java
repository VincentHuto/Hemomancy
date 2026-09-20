package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class SuccessionDialogue {
    public static final String BEQUEST = "succession_bequest", DONATE = "succession_donate";
    private SuccessionDialogue() {}
    public static boolean eligible(ServerPlayer player) {
        var line = SuccessionResidents.line(player);
        return line != null && HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 5
                && HemoCapabilityAccess.getBloodVolume(player).map(v -> v.isActive()).orElse(false)
                && java.util.stream.StreamSupport.stream(player.server.getAllLevels().spliterator(), false)
                .anyMatch(level -> FoundingFaneSavedData.get(level).hasFane(line.getLeaderUUID()));
    }
    public static DialogueTree decorate(ServerPlayer player, Entity entity, DialogueTree tree) {
        if (!(entity instanceof ProfessionalHarbingerEntity npc)) return tree;
        var nodes = new LinkedHashMap<String, DialogueNode>();
        for (var node : tree.nodes().values()) {
            var options = new ArrayList<>(node.options());
            if (npc.isSuccessor()) options.removeIf(o -> "recruit_harbinger".equals(o.eventId())
                    || "expel_harbinger".equals(o.eventId()) || "recruit_offer".equals(o.nextNodeId())
                    || BEQUEST.equals(o.eventId()) || DONATE.equals(o.eventId()));
            var nodeLines = npc.isSuccessor() && "identity".equals(node.id())
                    ? List.of("hemomancy.succession.personal_identity") : node.lines();
            nodes.put(node.id(), new DialogueNode(node.id(), nodeLines, options));
        }
        var root = nodes.get(tree.startNodeId());
        if (root == null) return tree;
        var options = new ArrayList<>(root.options());
        var lines = new ArrayList<>(root.lines());
        String speaker = tree.speakerName();
        if (npc.isSuccessor()) {
            var r = SuccessionSavedData.get(player.serverLevel()).residents.get(npc.getUUID());
            if (r != null) {
                speaker = r.name;
                if ("greeting".equals(root.id())) {
                    lines.clear();
                    lines.add("hemomancy.succession.greeting." + Math.floorMod(r.seed, 3)
                            + (r.encounters.getInt(player.getUUID().toString()) > 1 ? ".return" : ".first"));
                }
            }
        } else if (eligible(player)) {
            String role = SuccessionProfessions.profession(npc);
            var line = SuccessionResidents.line(player);
            boolean consent = SuccessionSavedData.get(player.serverLevel()).hasBequest(npc.getUUID(), line.getBloodlineUUID(), role);
            options.add(new DialogueOption("hemomancy.succession.explain", "succession_explain", null));
            options.add(new DialogueOption(consent ? "hemomancy.succession.donate" : "hemomancy.succession.ask_bequest",
                    consent ? null : "succession_consent", consent ? DONATE : null));
            nodes.put("succession_consent", new DialogueNode("succession_consent", List.of("hemomancy.succession.consent"),
                    List.of(new DialogueOption("hemomancy.succession.accept_bequest", null, BEQUEST))));
            nodes.put("succession_explain", new DialogueNode("succession_explain", List.of("hemomancy.succession.teaching." + role),
                    List.of(new DialogueOption("hemomancy.succession.back", tree.startNodeId(), null))));
        }
        nodes.put(root.id(), new DialogueNode(root.id(), lines, options));
        return new DialogueTree(speaker, tree.speakerIcon(), tree.startNodeId(), nodes, tree.entityId(), tree.theme(), tree.presentation());
    }
    public static boolean handle(ServerPlayer player, Entity entity, String event) {
        if (!event.startsWith("succession_")) return false;
        if (!(entity instanceof ProfessionalHarbingerEntity npc) || npc.isSuccessor() || npc.isMisbegotten() || !npc.isAlive()
                || player.distanceToSqr(npc) > 64 || !eligible(player)) return true;
        var line = SuccessionResidents.line(player); String profession = SuccessionProfessions.profession(npc);
        var data = SuccessionSavedData.get(player.serverLevel());
        if (BEQUEST.equals(event)) {
            data.bequeath(npc.getUUID(), line.getBloodlineUUID(), profession, npc.getName().getString());
            player.displayClientMessage(Component.translatable("hemomancy.succession.bequest_granted", npc.getName()), false);
        } else if (DONATE.equals(event) && data.hasBequest(npc.getUUID(), line.getBloodlineUUID(), profession)) {
            ItemStack vial = player.getMainHandItem();
            if (vial.getItem() instanceof com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSyringeItem syringe) {
                if (!syringe.collectDonation(player, npc, line.getBloodlineUUID(), profession).consumesAction()) return true;
            } else {
                if (BloodProfileData.profile(npc.getType(), false).requiresLivingSyringe()) {
                    player.displayClientMessage(Component.translatable(BloodSamplingResult.REQUIRES_LIVING_SYRINGE.translationKey()), true);
                    return true;
                }
                if (!(vial.getItem() instanceof BloodVialItem) || BloodSampleData.isFilled(vial)) {
                    player.displayClientMessage(Component.translatable("hemomancy.succession.empty_vial"), true); return true;
                }
                SuccessionSamples.fill(vial, npc, line.getBloodlineUUID(), profession);
            }
            player.getInventory().setChanged(); player.containerMenu.broadcastChanges();
            player.displayClientMessage(Component.translatable("hemomancy.succession.donated", npc.getName()), false);
        }
        return true;
    }
    public static void idle(ServerPlayer player, ProfessionalHarbingerEntity npc) {
        var tree = DialogueTree.builder(npc.getName().getString(), Hemomancy.rloc("textures/item/mnemonic_ambergris.png"), npc.getId())
                .addNode(new DialogueNode("idle", List.of("hemomancy.succession.unavailable"), List.of())).build();
        PacketHandler.sendToPlayer(player, new OpenDialoguePacket(tree));
    }
}
