package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

import net.minecraft.network.chat.Component;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitMode;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationChannelManager;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastingAnimationManager;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.UseManipKeyPacket;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.block.harbinger.EscharianScyphusBlock;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.ClinicalBloodDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEventHandler;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MnemonistStarterMemoryChoice;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PhlegethonticCommissionDialogue;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.harbinger.*;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSicklePruning;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.*;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVesperFightScene;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteCancellationHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyStage;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneySnapshot;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyChecks;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyController;
import com.vincenthuto.hemomancy.gametest.journey.JourneyRoute;
import com.vincenthuto.hemomancy.gametest.journey.HarbingerJourneyAutomation;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.PhlebotomistsCabinetBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.*;

import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress.Lesson.*;

@GameTestHolder("clinical_validation")
@PrefixGameTestTemplate(false)
public final class ClinicalBloodProgressionGameTests {
    private static final net.minecraft.server.level.TicketType<UUID> ROOTED_VEIN_TICKET =
            net.minecraft.server.level.TicketType.create("hemomancy_rooted_vein_test", UUID::compareTo);
    private static final net.minecraft.server.level.TicketType<UUID> CHAMBER_ITEM_FIXTURE_TICKET =
            net.minecraft.server.level.TicketType.create("hemomancy_chamber_item_fixture", UUID::compareTo);
    private static ServerPlayer player(GameTestHelper h, int degree) {
        return player(h, degree, h.getLevel());
    }
    private static ServerPlayer player(GameTestHelper h, int degree, net.minecraft.server.level.ServerLevel level) {
        return player(h, degree, level, packet -> {});
    }
    private static ServerPlayer player(GameTestHelper h, int degree, net.minecraft.server.level.ServerLevel level,
            java.util.function.Consumer<net.minecraft.network.protocol.Packet<?>> received) {
        return player(h, degree, level, received, new GameProfile(UUID.randomUUID(), "clinical-test"));
    }
    private static ServerPlayer player(GameTestHelper h, int degree, net.minecraft.server.level.ServerLevel level,
            java.util.function.Consumer<net.minecraft.network.protocol.Packet<?>> received, GameProfile profile) {
        var cookie = CommonListenerCookie.createInitial(profile, false);
        var p = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(p.server, connection, p, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) { received.accept(packet); }
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,
                    net.minecraft.network.PacketSendListener listener) { received.accept(packet); }
        };
        p.setGameMode(GameType.SURVIVAL);
        p.setPos(level == h.getLevel() ? h.absolutePos(new BlockPos(0, 3, 0)).getCenter()
                : new Vec3(0.5, 80, 0.5));
        p.setNoGravity(true);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(degree);
        return p;
    }

    private static void captureVesperScene(net.minecraft.network.protocol.Packet<?> packet,
            List<PacketSyncVesperFightScene> scenes) {
        if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                && custom.payload() instanceof PacketSyncVesperFightScene scene) scenes.add(scene);
        if (packet instanceof net.minecraft.network.protocol.game.ClientboundBundlePacket bundle)
            bundle.subPackets().forEach(child -> captureVesperScene(child, scenes));
    }

    @GameTest(template = "empty") public static void distributorSaveRequiresLearnedThelemicMemory(GameTestHelper h) {
        var p = player(h, 5);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.dendritic_distributor.get().defaultBlockState());
        p.setPos(pos.getCenter());
        var known = HemoCapabilityAccess.getKnownManipulations(p).orElseThrow();
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        p.giveExperiencePoints(100);
        var memory = MuscleMemory.SANGUINE_FISTS;
        String key = MemorySlotRef.muscleMemory(memory).storageKey();
        known.setEquippedManipNames(List.of(key));
        known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(memory));
        try {
            distributorAction(p, pos, 0,
                    com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.SAVE_OR_OVERWRITE);
            h.assertTrue(known.getLoadout(0).isEmpty() && volume.getBloodVolume() == 1000
                            && p.totalExperience == 100,
                    "An unlearned equipped Thelemic key must not save or spend blood/XP");
            p.getData(HemoAttachmentTypes.MUSCLE_MEMORY).learnAndAddReserve(memory, 200);
            distributorAction(p, pos, 0,
                    com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.SAVE_OR_OVERWRITE);
            h.assertTrue(known.getLoadout(0).manipNames().equals(List.of(key))
                            && known.getLoadout(0).selectedManipName().equals(key)
                            && volume.getBloodVolume() == 900 && p.totalExperience == 75,
                    "Learning the same Thelemic memory must allow normal paid Save");
            h.assertTrue(p.getData(HemoAttachmentTypes.MUSCLE_MEMORY).reserveTicks(memory) == 200,
                    "Saving a pattern must not consume its Thelemic reserve");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(template = "empty") public static void distributorRejectsRemoteActionsWithoutMutation(GameTestHelper h) {
        var p = player(h, 5);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.dendritic_distributor.get().defaultBlockState());
        p.setPos(pos.getCenter().add(9, 0, 0));
        var known = HemoCapabilityAccess.getKnownManipulations(p).orElseThrow();
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        p.giveExperiencePoints(100);
        var memory = MuscleMemory.SANGUINE_FISTS;
        p.getData(HemoAttachmentTypes.MUSCLE_MEMORY).learnAndAddReserve(memory, 200);
        String key = MemorySlotRef.muscleMemory(memory).storageKey();
        var saved = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout.of(
                "Retained pattern", key, List.of(key), 0);
        known.setLoadout(0, saved);
        p.getData(HemoAttachmentTypes.MUSCLE_MEMORY).learnAndAddReserve(MuscleMemory.LABORING_ARMS, 200);
        known.setEquippedMemoryRefs(List.of(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS)));
        known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
        var equipped = List.copyOf(known.getEquippedManipNames());
        var selected = known.getSelectedMemoryRef();
        try {
            for (var action : com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.values()) {
                distributorAction(p, pos, 0, action);
                h.assertTrue(known.getLoadout(0).equals(saved)
                                && known.getEquippedManipNames().equals(equipped)
                                && known.getSelectedMemoryRef().equals(selected)
                                && volume.getBloodVolume() == 1000 && p.totalExperience == 100,
                        "Remote " + action + " must retain pattern, equipment, selection and costs");
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(template = "empty") public static void distributorApplyRejectsUnknownAndOverCapacityPatterns(GameTestHelper h) {
        var p = player(h, 5);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.dendritic_distributor.get().defaultBlockState());
        p.setPos(pos.getCenter());
        var known = HemoCapabilityAccess.getKnownManipulations(p).orElseThrow();
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        p.giveExperiencePoints(100);
        var muscle = p.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        muscle.learnAndAddReserve(MuscleMemory.LABORING_ARMS, 200);
        muscle.activate(MuscleMemory.LABORING_ARMS);
        known.setEquippedMemoryRefs(List.of(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS)));
        known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
        var equipped = List.copyOf(known.getEquippedManipNames());
        var selected = known.getSelectedMemoryRef();
        try {
            var unknownThelemic = List.of(MemorySlotRef.muscleMemory(MuscleMemory.SANGUINE_FISTS).storageKey());
            var unknownNoetic = List.of("missing_distributor_test_memory");
            var allThelemic = java.util.Arrays.stream(MuscleMemory.values())
                    .map(memory -> MemorySlotRef.muscleMemory(memory).storageKey()).toList();
            for (var names : List.of(unknownThelemic, unknownNoetic, allThelemic)) {
                if (names == allThelemic) {
                    for (var memory : MuscleMemory.values()) muscle.learnAndAddReserve(memory, 200);
                    h.assertTrue(names.size() > com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper.getMaxSlots(p),
                            "The capacity fixture must exceed this player's actual shared limit");
                }
                var saved = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout.of(
                        "Rejected pattern", names.getFirst(), names, 0);
                known.setLoadout(0, saved);
                int reserve = muscle.reserveTicks(MuscleMemory.LABORING_ARMS);
                distributorAction(p, pos, 0,
                        com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.APPLY);
                h.assertTrue(known.getLoadout(0).equals(saved) && known.getEquippedManipNames().equals(equipped)
                                && known.getSelectedMemoryRef().equals(selected) && muscle.isEnabled(MuscleMemory.LABORING_ARMS)
                                && muscle.reserveTicks(MuscleMemory.LABORING_ARMS) == reserve
                                && volume.getBloodVolume() == 1000 && p.totalExperience == 100,
                        "Rejected Apply must retain saved pattern, current memories, activation, reserve and costs: " + names);
            }
            h.succeed();
        } finally { p.discard(); }
    }

    private static void distributorAction(ServerPlayer player, BlockPos pos, int slot,
            com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action action) {
        var context = (net.neoforged.neoforge.network.handling.IPayloadContext) java.lang.reflect.Proxy.newProxyInstance(
                ClinicalBloodProgressionGameTests.class.getClassLoader(),
                new Class<?>[]{net.neoforged.neoforge.network.handling.IPayloadContext.class}, (proxy, method, args) -> {
                    if (method.getName().equals("player")) return player;
                    if (method.getName().equals("enqueueWork")) {
                        ((Runnable) args[0]).run();
                        return java.util.concurrent.CompletableFuture.completedFuture(null);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.handle(
                new com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket(
                        action, pos, slot, "Changed pattern"), context);
    }

    @GameTest(template = "empty") public static void distributorApplySynchronizesOmittedThelemicMemory(GameTestHelper h) {
        var states = new ArrayList<com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMuscleMemory>();
        var p = player(h, 5, h.getLevel(), packet -> captureDistributorMuscleState(packet, states));
        h.getLevel().addNewPlayer(p);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.dendritic_distributor.get().defaultBlockState());
        p.setPos(pos.getCenter());
        var known = HemoCapabilityAccess.getKnownManipulations(p).orElseThrow();
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        var muscle = p.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        muscle.learnAndAddReserve(MuscleMemory.LABORING_ARMS, 200);
        muscle.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 300);
        muscle.activate(MuscleMemory.LABORING_ARMS);
        known.setEquippedMemoryRefs(List.of(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS)));
        known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
        String key = MemorySlotRef.muscleMemory(MuscleMemory.SANGUINE_FISTS).storageKey();
        known.setLoadout(0, com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout.of(
                "Fists", key, List.of(key), 0));
        states.clear();
        try {
            distributorAction(p, pos, 0,
                    com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.APPLY);
            h.assertTrue(!muscle.hasEnabledMemories() && muscle.reserveTicks(MuscleMemory.LABORING_ARMS) == 200
                            && muscle.reserveTicks(MuscleMemory.SANGUINE_FISTS) == 300
                            && known.getSelectedMemoryRef().storageKey().equals(key)
                            && volume.getBloodVolume() == 1000 && p.totalExperience == 0,
                    "Apply must stop the omitted memory, preserve reserves, select the saved memory and remain free");
            h.assertTrue(states.size() == 1 && states.getFirst().playerId() == p.getId(),
                    "Stopping the final active Thelemic memory must synchronize it immediately to its player; packets=" + states.size());
            var clientState = new com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryState();
            clientState.deserializeNBT(null, states.getFirst().state());
            h.assertTrue(!clientState.hasEnabledMemories() && clientState.knows(MuscleMemory.LABORING_ARMS)
                            && clientState.reserveTicks(MuscleMemory.LABORING_ARMS) == 200
                            && clientState.reserveTicks(MuscleMemory.SANGUINE_FISTS) == 300,
                    "The sent state must retain knowledge/reserves and clear the omitted activation");
            distributorAction(p, pos, 0,
                    com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.APPLY);
            h.assertTrue(states.size() == 1, "Repeating unchanged Apply must not emit another muscle-state update");
            h.succeed();
        } finally { p.discard(); }
    }

    private static void captureDistributorMuscleState(net.minecraft.network.protocol.Packet<?> packet,
            List<com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMuscleMemory> states) {
        if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom
                && custom.payload() instanceof com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncMuscleMemory state)
            states.add(state);
        if (packet instanceof net.minecraft.network.protocol.game.ClientboundBundlePacket bundle)
            bundle.subPackets().forEach(child -> captureDistributorMuscleState(child, states));
    }

    @GameTest(template = "empty") public static void distributorPatternsRetainMixedAndLegacySelectionsAcrossNbt(GameTestHelper h) {
        var p = player(h, 5);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.dendritic_distributor.get().defaultBlockState());
        p.setPos(pos.getCenter());
        var known = HemoCapabilityAccess.getKnownManipulations(p).orElseThrow();
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper.learnAndEquipIfPossible(
                known, ManipulationInit.getByName("blood_shot"), 5);
        var muscle = p.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
        muscle.learnAndAddReserve(MuscleMemory.SANGUINE_FISTS, 300);
        String key = MemorySlotRef.muscleMemory(MuscleMemory.SANGUINE_FISTS).storageKey();
        var legacy = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout.of(
                "Old shot", "blood_shot", List.of("blood_shot"), 0);
        var mixed = com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout.of(
                "Mixed field", key, List.of("blood_shot", key), 2);
        known.setLoadout(0, legacy);
        known.setLoadout(2, mixed);
        known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.SANGUINE_FISTS));
        try {
            var source = p.getData(HemoAttachmentTypes.KNOWN_MANIPULATIONS);
            var restored = new com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations();
            restored.deserializeNBT(h.getLevel().registryAccess(), source.serializeNBT(h.getLevel().registryAccess()).copy());
            h.assertTrue(restored.getLoadouts().size() == 3 && restored.getLoadout(0).equals(legacy)
                            && restored.getLoadout(1).isEmpty() && restored.getLoadout(2).equals(mixed)
                            && restored.getSelectedMemoryRef().storageKey().equals(key),
                    "NBT must retain legacy and mixed names/order/preferred selection and their empty intermediate slot");
            known.setLoadouts(List.of());
            known.setCapa(restored);
            for (int slot : new int[]{0, 2}) {
                var pattern = known.getLoadout(slot);
                distributorAction(p, pos, slot,
                        com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.SynapticLoadoutActionPacket.Action.APPLY);
                h.assertTrue(known.getEquippedManipNames().containsAll(pattern.manipNames())
                                && com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper
                                        .countNormalEquippedNames(known.getEquippedManipNames()) == pattern.manipNames().size()
                                && known.getSelectedMemoryRef().storageKey().equals(pattern.selectedManipName())
                                && known.getLoadout(0).equals(legacy) && known.getLoadout(2).equals(mixed)
                                && volume.getBloodVolume() == 1000 && p.totalExperience == 0
                                && muscle.reserveTicks(MuscleMemory.SANGUINE_FISTS) == 300,
                        "Restored pattern must Apply without record mutation, costs or lost reserve: " + slot);
            }
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(template = "empty") public static void scarTemplateCommitCannotReplacePersonalEffigyPractice(GameTestHelper h) {
        scarCommitWithoutPractice(h, true);
    }

    @GameTest(template = "empty") public static void giftedScarLoadoutCannotReplacePersonalEffigyPractice(GameTestHelper h) {
        scarCommitWithoutPractice(h, false);
    }

    @GameTest(template = "empty") public static void existingScarMainProofSurvivesWithoutTheIntermediateEffigyMilestone(GameTestHelper h) {
        var p = player(h, 4);
        var id = Hemomancy.rloc("scar_heart");
        var scars = HemoCapabilityAccess.requireScarState(p);
        scars.addKnownCerebralScar(id);
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        HarbingerAdvancementGranter.grantIfNotDone(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
        var pattern = com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern.createTemplatePattern(id);
        try {
            com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.burn(h.getLevel(), p.blockPosition(), p, pattern,
                    com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
            h.assertTrue(pattern.isEmpty() && scars.getActiveCerebralScars().contains(id)
                    && volume.getBloodVolume() == 950, "Existing practitioners must retain normal loadout use");
            h.assertTrue(HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(p)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(p),
                    "Existing Main proof survives without fabricating intermediate practice");
            h.assertTrue(HarbingerAdvancementGranter.isVeinMasonContinuationReady(p),
                    "Existing earned Main proof must retain continuation eligibility");
            h.succeed();
        } finally { p.discard(); }
    }

    private static void scarCommitWithoutPractice(GameTestHelper h, boolean template) {
        var p = player(h, 4);
        var id = Hemomancy.rloc("scar_heart");
        var scars = HemoCapabilityAccess.requireScarState(p);
        scars.addKnownCerebralScar(id);
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setActive(true);
        volume.setBloodVolume(1000);
        var pattern = template
                ? com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern.createTemplatePattern(id)
                : com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern.createPreparedPattern(List.of(id));
        try {
            com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.burn(h.getLevel(), p.blockPosition(), p, pattern,
                    com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
            h.assertTrue(pattern.isEmpty() && scars.getActiveCerebralScars().contains(id)
                    && volume.getBloodVolume() == 950, "Known loadouts remain usable and pay the ordinary commit cost");
            h.assertTrue(!HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(p),
                    "Committing a template or gift without personal Effigy preparation must not grant the D4 Main proof");
            h.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(p,
                    HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY),
                    "Unearned Main proof must not unlock the continuation reward");
            h.succeed();
        } finally { p.discard(); }
    }

    @GameTest(template = "empty") public static void personalEffigyPreparationThenCommitEarnsOnlyTheActorsMainProof(GameTestHelper h) {
        var actor = player(h, 4);
        var guest = player(h, 4);
        var id = Hemomancy.rloc("scar_heart");
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.mason_effigy.get().defaultBlockState());
        var effigy = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity)
                h.getLevel().getBlockEntity(pos);
        try {
            for (var p : List.of(actor, guest)) {
                HemoCapabilityAccess.requireScarState(p).addKnownCerebralScar(id);
                var volume = HemoCapabilityAccess.requireBloodVolume(p);
                volume.setActive(true);
                volume.setBloodVolume(1000);
            }
            effigy.setSelectedScarIds(List.of(id));
            h.assertTrue(effigy.beginMotifRitual(actor, new ItemStack(ItemInit.runic_motif_paper.get())),
                    "Selected known scar must accept motif paper");
            effigy.receiveProjectedBlood(actor, 500, false);
            h.assertTrue(HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(actor),
                    "Actual preparation records only the intermediate milestone");
            var outputs = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2),
                    item -> item.getItem().is(ItemInit.scar_pattern.get()));
            h.assertTrue(outputs.size() == 1, "Actual preparation must eject one pattern");
            var gifted = outputs.getFirst().getItem().copy();
            com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.burn(h.getLevel(), pos, guest, gifted,
                    com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
            h.assertTrue(!HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(guest),
                    "Another practitioner's real Effigy output cannot supply personal practice");
            var ownPattern = outputs.getFirst().getItem();
            com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.burn(h.getLevel(), pos, actor, ownPattern,
                    com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
            h.assertTrue(ownPattern.isEmpty() && HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(actor),
                    "Actual preparation followed by commit must earn the actor's Main proof");
            h.assertTrue(HarbingerAdvancementGranter.hasAdvancement(actor,
                    HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY), "Completed cycle must unlock continuation");
            h.succeed();
        } finally { actor.discard(); guest.discard(); }
    }

    @GameTest(template = "empty") public static void scarLessonUnlocksRegisteredLiberPracticePage(GameTestHelper h) {
        var actor = player(h, 4);
        var entry = Hemomancy.rloc("libersanguinium/the_hematic_order/pages/scar_practice");
        try {
            h.assertTrue(!LiberKnowledgeHelper.hasEntry(actor, entry), "D4 alone must not supply the scar lesson page");
            HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
            h.assertTrue(LiberKnowledgeHelper.hasEntry(actor, entry)
                    && com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions
                            .get(entry).isPresent(),
                    "Accepted first scar lesson must unlock a registered, visible Liber page");
            h.assertTrue(!HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(actor),
                    "Reading access must not substitute for scar preparation or commit");
            h.succeed();
        } finally { actor.discard(); }
    }

    @GameTest(template = "empty") public static void earnedScarLessonBackfillsPracticePageWithoutNewRewards(GameTestHelper h) {
        var actor = player(h, 4);
        var newcomer = player(h, 4);
        var entry = Hemomancy.rloc("libersanguinium/the_hematic_order/pages/scar_practice");
        try {
            HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
            var knowledge = actor.getData(HemoAttachmentTypes.LIBER_KNOWLEDGE);
            var legacy = new com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledge();
            var other = Hemomancy.rloc("libersanguinium/intro/pages/hemomancy");
            legacy.unlockEntry(other, com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource.OTHER);
            knowledge.setFrom(legacy);
            h.assertTrue(!knowledge.hasEntry(entry), "Legacy fixture must begin with the missing page");
            com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents.playerLoggedIn(
                    new PlayerEvent.PlayerLoggedInEvent(actor));
            com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents.playerLoggedIn(
                    new PlayerEvent.PlayerLoggedInEvent(newcomer));
            h.assertTrue(knowledge.hasEntry(entry) && knowledge.hasEntry(other)
                    && !LiberKnowledgeHelper.hasEntry(newcomer, entry),
                    "Login must restore only the earned scar lesson page, preserving existing knowledge");
            int count = knowledge.getUnlockedEntries().size();
            com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents.playerLoggedIn(
                    new PlayerEvent.PlayerLoggedInEvent(actor));
            h.assertTrue(knowledge.getUnlockedEntries().size() == count && actor.getInventory().isEmpty()
                    && HemoCapabilityAccess.getPlayerDegreeNumber(actor) == 4
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(actor),
                    "Repeat backfill must not supply items, preparation, Main proof or promotion");
            h.succeed();
        } finally { actor.discard(); newcomer.discard(); }
    }

    @GameTest(template = "empty") public static void effigyProjectionRejectsUnknownScarsWithoutLosingPaidWork(GameTestHelper h) {
        effigyRejectsIneligibleContributor(h, false);
    }

    @GameTest(template = "empty") public static void effigyProjectionRejectsInsufficientCapacityWithoutLosingPaidWork(GameTestHelper h) {
        effigyRejectsIneligibleContributor(h, true);
    }

    private static void effigyRejectsIneligibleContributor(GameTestHelper h, boolean insufficientCapacity) {
        var actor = player(h, insufficientCapacity ? 5 : 4);
        var guest = player(h, 4);
        var heart = Hemomancy.rloc("scar_heart");
        var selected = insufficientCapacity ? List.of(heart, Hemomancy.rloc("scar_marrow")) : List.of(heart);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        var bounds = new AABB(pos).inflate(2);
        h.getLevel().setBlockAndUpdate(pos, BlockInit.mason_effigy.get().defaultBlockState());
        var effigy = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity)
                h.getLevel().getBlockEntity(pos);
        try {
            selected.forEach(HemoCapabilityAccess.requireScarState(actor)::addKnownCerebralScar);
            if (insufficientCapacity) {
                selected.forEach(HemoCapabilityAccess.requireScarState(guest)::addKnownCerebralScar);
            }
            for (var p : List.of(actor, guest)) {
                var volume = HemoCapabilityAccess.requireBloodVolume(p);
                volume.setActive(true);
                volume.setBloodVolume(1000);
            }
            effigy.setSelectedScarIds(selected);
            var paper = new ItemStack(ItemInit.runic_motif_paper.get());
            h.assertTrue(effigy.beginMotifRitual(actor, paper) && paper.isEmpty(),
                    "Eligible practitioner must insert a real motif");
            h.assertTrue(effigy.receiveProjectedBlood(actor, 200, false) == 200,
                    "Eligible practitioner must establish paid partial work");
            h.assertTrue(effigy.receiveProjectedBlood(guest, 1000, false) == 0,
                    insufficientCapacity ? "Charging must enforce the contributor's scar capacity"
                            : "Charging must require the contributor to know each selected scar");
            h.assertTrue(effigy.hasPendingMotif() && effigy.getPendingBlood() == 200
                    && effigy.getSelectedScarIds().equals(selected)
                    && HemoCapabilityAccess.requireBloodVolume(guest).getBloodVolume() == 1000
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(guest)
                    && h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                            item -> item.getItem().is(ItemInit.scar_pattern.get())).isEmpty(),
                    "Rejected projection must retain escrow, guest blood and unearned proof");
            if (insufficientCapacity) {
                HemoCapabilityAccess.requireInitiatoryDegree(guest).setDegreeNumber(5);
            } else {
                selected.forEach(HemoCapabilityAccess.requireScarState(guest)::addKnownCerebralScar);
            }
            double remaining = selected.size() * 500 - 200;
            h.assertTrue(effigy.receiveProjectedBlood(guest, 1000, false) == remaining
                    && HemoCapabilityAccess.requireBloodVolume(guest).getBloodVolume() == 1000 - remaining
                    && HemoCapabilityAccess.requireBloodVolume(actor).getBloodVolume() == 800,
                    "Eligible second practitioner may resume shared work without an ownership lock");
            var outputs = h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.scar_pattern.get()));
            h.assertTrue(outputs.size() == 1
                    && com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern
                            .getScarIds(outputs.getFirst().getItem()).equals(selected)
                    && HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(guest)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(guest),
                    "Eligible completion emits one original pattern and only its practitioner's preparation proof");
            h.succeed();
        } finally {
            h.getLevel().removeBlock(pos, false);
            h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.scar_pattern.get())
                            || item.getItem().is(ItemInit.runic_motif_paper.get())).forEach(ItemEntity::discard);
            actor.discard();
            guest.discard();
        }
    }

    @GameTest(template = "empty") public static void breakingChargedEffigyReturnsItsExactMotifOnce(GameTestHelper h) {
        var actor = player(h, 4);
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        var bounds = new AABB(pos).inflate(2);
        h.getLevel().setBlockAndUpdate(pos, BlockInit.mason_effigy.get().defaultBlockState());
        var effigy = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity)
                h.getLevel().getBlockEntity(pos);
        try {
            var heart = Hemomancy.rloc("scar_heart");
            HemoCapabilityAccess.requireScarState(actor).addKnownCerebralScar(heart);
            var volume = HemoCapabilityAccess.requireBloodVolume(actor);
            volume.setActive(true);
            volume.setBloodVolume(1000);
            effigy.setSelectedScarIds(List.of(heart));
            var paper = new ItemStack(ItemInit.runic_motif_paper.get(), 2);
            paper.set(DataComponents.CUSTOM_NAME, Component.literal("Interrupted Heart Motif"));
            var expected = paper.copyWithCount(1);
            h.assertTrue(effigy.beginMotifRitual(actor, paper) && paper.getCount() == 1,
                    "Preparation must consume only one motif from the supplied stack");
            effigy.receiveProjectedBlood(actor, 200, false);
            var state = h.getLevel().getBlockState(pos);
            h.getLevel().setBlockAndUpdate(pos, state.setValue(
                    com.vincenthuto.hemomancy.common.block.harbinger.functional.MasonsEffigyBlock.FACING,
                    state.getValue(com.vincenthuto.hemomancy.common.block.harbinger.functional.MasonsEffigyBlock.FACING)
                            .getOpposite()));
            h.assertTrue(h.getLevel().getBlockEntity(pos) == effigy && effigy.hasPendingMotif()
                    && effigy.getPendingBlood() == 200
                    && h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                            item -> item.getItem().is(ItemInit.runic_motif_paper.get())).isEmpty(),
                    "Same-block state changes must retain pending work without dropping it");
            h.assertTrue(h.getLevel().destroyBlock(pos, true), "The real block destruction path must run");
            var drops = h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.runic_motif_paper.get()));
            h.assertTrue(drops.size() == 1 && drops.getFirst().getItem().getCount() == 1
                    && ItemStack.isSameItemSameComponents(drops.getFirst().getItem(), expected),
                    "Breaking interrupted work must return exactly its original motif and components");
            h.assertTrue(!effigy.hasPendingMotif() && effigy.getPendingBlood() == 0
                    && volume.getBloodVolume() == 800
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(actor),
                    "Removal must clear escrow without refunding blood or awarding scar proofs");
            h.getLevel().destroyBlock(pos, true);
            effigy.dropPendingMotif();
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.runic_motif_paper.get())).size() == 1,
                    "Repeated removal must not duplicate the returned motif");
            h.succeed();
        } finally {
            h.getLevel().removeBlock(pos, false);
            h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.runic_motif_paper.get())
                            || item.getItem().is(BlockInit.mason_effigy.get().asItem())).forEach(ItemEntity::discard);
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void chargedEffigySelectionSurvivesInterruptionAndReload(GameTestHelper h) {
        var actor = player(h, 4);
        var heart = Hemomancy.rloc("scar_heart");
        var marrow = Hemomancy.rloc("scar_marrow");
        var pos = h.absolutePos(new BlockPos(1, 2, 1));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.mason_effigy.get().defaultBlockState());
        var effigy = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity)
                h.getLevel().getBlockEntity(pos);
        var bounds = new AABB(pos).inflate(2);
        try {
            var scars = HemoCapabilityAccess.requireScarState(actor);
            scars.addKnownCerebralScar(heart);
            scars.addKnownCerebralScar(marrow);
            var volume = HemoCapabilityAccess.requireBloodVolume(actor);
            volume.setActive(true);
            volume.setBloodVolume(1000);
            effigy.setSelectedScarIds(List.of(heart));
            var paper = new ItemStack(ItemInit.runic_motif_paper.get(), 2);
            h.assertTrue(effigy.beginMotifRitual(actor, paper) && paper.getCount() == 1,
                    "Starting preparation must consume exactly one motif");
            h.assertTrue(effigy.receiveProjectedBlood(actor, 200, false) == 200,
                    "Partial projection must retain its paid charge");
            effigy.setSelectedScarIds(List.of());
            h.assertTrue(effigy.getSelectedScarIds().equals(List.of(heart)),
                    "Clearing selection must not strand a paid motif");
            effigy.setSelectedScarIds(List.of(marrow));
            h.assertTrue(effigy.getSelectedScarIds().equals(List.of(heart)),
                    "A pending motif must retain the scars it was started with");
            var saved = effigy.saveWithFullMetadata(h.getLevel().registryAccess());
            h.getLevel().removeBlockEntity(pos);
            effigy = new com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity(
                    pos, h.getLevel().getBlockState(pos));
            h.getLevel().setBlockEntity(effigy);
            effigy.loadWithComponents(saved, h.getLevel().registryAccess());
            h.assertTrue(effigy.hasPendingMotif() && effigy.getPendingBlood() == 200
                    && effigy.getSelectedScarIds().equals(List.of(heart))
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor),
                    "Reload must preserve unfinished work without awarding preparation proof");
            effigy.setSelectedScarIds(List.of(marrow));
            h.assertTrue(effigy.getSelectedScarIds().equals(List.of(heart)),
                    "Reload must not release the pending selection lock");
            h.assertTrue(!effigy.beginMotifRitual(actor, paper) && paper.getCount() == 1,
                    "Restarting pending work must retain the spare motif");
            h.assertTrue(effigy.receiveProjectedBlood(actor, 500, false) == 300
                    && volume.getBloodVolume() == 500,
                    "Resuming must spend only the remaining charge, exactly 500 total");
            var outputs = h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.scar_pattern.get()));
            h.assertTrue(outputs.size() == 1
                    && com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern
                            .getScarIds(outputs.getFirst().getItem()).equals(List.of(heart))
                    && !effigy.hasPendingMotif() && effigy.getPendingBlood() == 0,
                    "Resumption must eject exactly the original loadout and clear pending work");
            h.assertTrue(HarbingerAdvancementGranter.isVeinMasonFirstEffigyPattern(actor)
                    && !HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(actor),
                    "Completion earns preparation only, not the Main commit proof");
            h.assertTrue(effigy.receiveProjectedBlood(actor, 500, false) == 0
                    && volume.getBloodVolume() == 500,
                    "Completed preparation must not spend blood or eject a duplicate");
            effigy.setSelectedScarIds(List.of(marrow));
            h.assertTrue(effigy.getSelectedScarIds().equals(List.of(marrow)),
                    "Selection becomes editable after completion");
            h.succeed();
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, bounds,
                    item -> item.getItem().is(ItemInit.scar_pattern.get())).forEach(ItemEntity::discard);
            h.getLevel().removeBlock(pos, false);
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void personalScarCycleOpensIlluminatusWithoutPromotingEarly(GameTestHelper h) {
        var actor = player(h, 4);
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var effigyPos = h.absolutePos(new BlockPos(1, 2, 1));
        var saved = CardinalRiteSavedData.get(h.getLevel());
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, "illuminatus_rite");
            var blood = HemoCapabilityAccess.requireBloodVolume(actor);
            blood.setActive(true);
            blood.setBloodVolume(2000);
            var staff = actor.getMainHandItem();
            var id = Hemomancy.rloc("scar_heart");
            HemoCapabilityAccess.requireScarState(actor).addKnownCerebralScar(id);
            var gift = com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern.createPreparedPattern(List.of(id));
            com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.burn(h.getLevel(), effigyPos, actor, gift,
                    com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
            var trigger = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE;
            var rejected = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, origin.above(), trigger);
            h.assertTrue(rejected != com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                    && saved.getRite(actor.getUUID()) == null && actor.getMainHandItem() == staff,
                    "A gifted loadout must leave Illuminatus sealed and its Staff unescrowed");
            h.getLevel().setBlockAndUpdate(effigyPos, BlockInit.mason_effigy.get().defaultBlockState());
            var effigy = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity)
                    h.getLevel().getBlockEntity(effigyPos);
            effigy.setSelectedScarIds(List.of(id));
            h.assertTrue(effigy.beginMotifRitual(actor, new ItemStack(ItemInit.runic_motif_paper.get())),
                    "Personal Effigy practice must accept the known Heart");
            effigy.receiveProjectedBlood(actor, 500, false);
            var outputs = h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(effigyPos).inflate(2),
                    item -> item.getItem().is(ItemInit.scar_pattern.get()));
            h.assertTrue(outputs.size() == 1, "Personal practice must produce one actual pattern");
            com.vincenthuto.hemomancy.common.rite.ScarBrazierRite.burn(h.getLevel(), effigyPos, actor,
                    outputs.getFirst().getItem(), com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
            var started = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, origin.above(), trigger);
            var rite = saved.getRite(actor.getUUID());
            h.assertTrue(started == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                    && rite != null && rite.getRecipeId().equals(Hemomancy.rloc("cardinal_rite/illuminatus_rite")),
                    "Personal preparation and commit must open the authored Illuminatus rite");
            h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(actor) == 4,
                    "Starting Illuminatus must not award Degree 5 before its ceremony finishes");
            h.succeed();
        } finally {
            var rite = saved.getRite(actor.getUUID());
            if (rite != null) com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, rite);
            saved.removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            h.getLevel().removeBlock(effigyPos, false);
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void chamberRiteAttunesWithoutGuidedLessonChairOrRecruitedMnemonist(GameTestHelper h) {
        var actor = player(h, 5);
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var saved = CardinalRiteSavedData.get(h.getLevel());
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, "chamber_of_will");
            HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
            var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                    h.getLevel(), Hemomancy.rloc("cardinal_rite/chamber_of_will"));
            h.assertTrue(recipe.getCeremony().requiredHelpers() == 0,
                    "Independent Chamber attunement must not require recruited helpers");
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(actor)
                    && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isChairBound(actor)
                    && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(actor),
                    "Independent fixture must begin without earlier Chamber access");
            var staff = actor.getMainHandItem();
            var tooEarly = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, origin.above(),
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(tooEarly != com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                    && saved.getRite(actor.getUUID()) == null && actor.getMainHandItem() == staff
                    && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(actor),
                    "D5 must not start the independent D6 rite, lose its Staff, or gain attunement");
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(6);
            var started = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, origin.above(),
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            var rite = saved.getRite(actor.getUUID());
            h.assertTrue(started == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                    && rite != null && rite.getAllyRoles().isEmpty(), "Unrecruited D6 must start its own Chamber rite");
            // Supply the completed ceremony boundary; this server has no Chamber destination.
            var complete = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class
                    .getDeclaredMethod("completeRite", net.minecraft.server.level.ServerLevel.class,
                            ServerPlayer.class, ActiveCardinalRite.class);
            complete.setAccessible(true);
            h.assertTrue((boolean) complete.invoke(null, h.getLevel(), actor, rite),
                    "Independent Chamber rite completion must succeed without a recruited Mnemonist");
            h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(actor)
                    && HarbingerAdvancementGranter.hasAdvancement(actor, HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED),
                    "Earned rite completion must grant permanent attunement even when transport is unavailable");
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.hasCompletedGuidedVisit(actor)
                    && !HarbingerAdvancementGranter.hasAdvancement(actor, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED)
                    && HemoCapabilityAccess.getPlayerDegreeNumber(actor) == 6,
                    "Attunement must not fabricate a guided lesson, return, or higher degree");
            h.succeed();
        } catch (ReflectiveOperationException exception) {
            h.fail("Independent Chamber completion could not run: " + exception);
        } finally {
            var rite = saved.getRite(actor.getUUID());
            if (rite != null) com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, rite);
            saved.removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
    }

    private static void prepareSharedSelection(ServerPlayer player, BloodManipulation noetic) {
        var known = HemoCapabilityAccess.requireKnownManipulations(player);
        known.getKnownManips().put(ManipulationInit.blood_shot.get(), new ManipLevel(4, 185));
        known.getKnownManips().put(noetic, new ManipLevel(4, 185));
        known.setEquippedManipNames(List.of(noetic.getName(),
                MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS).storageKey()));
        known.setSelectedManip(noetic);
        player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).learnAndAddReserve(MuscleMemory.LABORING_ARMS, 6000);
        var blood = HemoCapabilityAccess.requireBloodVolume(player);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        HemoCapabilityAccess.requireBloodTendency(player).setTendencyAlignment(noetic.getTend(), 100);
    }

    private static void cleanupSharedSelection(ServerPlayer player) {
        ManipulationChannelManager.stop(player, false);
        CastingAnimationManager.logout(new PlayerEvent.PlayerLoggedOutEvent(player));
        player.discard();
    }

    @GameTest(template = "empty") public static void staffAbsorptionFollowsSharedMemorySelection(GameTestHelper h) {
        verifyStaffUtilitySelection(h, ManipulationInit.blood_absorption.get());
    }

    @GameTest(template = "empty") public static void staffProjectionFollowsSharedMemorySelection(GameTestHelper h) {
        verifyStaffUtilitySelection(h, ManipulationInit.blood_projection.get());
    }

    @GameTest(template = "empty") public static void inactiveStaffCannotStartRootedVeinOrLoseItsEscrow(GameTestHelper h) {
        var actor = player(h, 2);
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var focusPos = origin.above();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(h.getLevel());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, "rooted_vein");
            actor.setPos(focusPos.getCenter().add(0, 1, 2));
            var staff = actor.getMainHandItem();
            var blood = HemoCapabilityAccess.requireBloodVolume(actor);
            blood.setBloodVolume(1000);
            blood.setActive(false);
            var result = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(result != com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && saved.getRite(actor.getUUID()) == null,
                    "Inactive blood started Rooted Vein with an early Staff");
            h.assertTrue(actor.getMainHandItem() == staff && blood.getBloodVolume() == 1000,
                    "Rejected inactive start escrowed the Staff or spent blood");
            blood.setActive(true);
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(1);
            var tooEarly = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(tooEarly != com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && saved.getRite(actor.getUUID()) == null && actor.getMainHandItem() == staff,
                    "D1 Staff ownership bypassed Rooted Vein's D2 gate");
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(2);
            var retry = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(retry == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && saved.getRite(actor.getUUID()) != null,
                    "Active D2 retry did not start the same Rooted Vein fixture");
            h.succeed();
        } finally {
            var rite = saved.getRite(actor.getUUID());
            if (rite != null) com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, rite);
            saved.removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
    }

    @net.minecraft.gametest.framework.GameTestGenerator
    public static java.util.Collection<net.minecraft.gametest.framework.TestFunction> loadedChamberEntryTests() {
        if (!Boolean.getBoolean("hemomancy.chamberLoadedValidation")) return List.of();
        return List.of(
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_active_visit",
                        "clinical_validation:empty", 40, 0, true, h -> {
                    requireLoadedChamber(h);
                    rejectedChairVisitPreservesAnActiveVisit(h);
                }),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_fungal_projection",
                        "clinical_validation:empty", 40, 0, true, h -> {
                    requireLoadedChamber(h);
                    rejectedChairVisitPreservesFungalProjection(h);
                }),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_dream_snapshot_return",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.DREAM, false)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_dream_menu_recovery",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.DREAM, true)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_guided_snapshot_return",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.GUIDED, false)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_guided_menu_recovery",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.GUIDED, true)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_dream_pending_return",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.DREAM, false, true)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_dream_pending_recovery",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.DREAM, true, true)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_guided_pending_return",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.GUIDED, false, true)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_guided_pending_recovery",
                        "clinical_validation:empty", 200, 0, true, h -> loadedObservationalSnapshotRecovery(h, ChamberVisitMode.GUIDED, true, true)),
                new net.minecraft.gametest.framework.TestFunction("chamber_loaded_entry", "loaded_chamber_already_inside",
                        "clinical_validation:empty", 40, 0, true, h -> {
                    var chamber = requireLoadedChamber(h);
                    var actor = player(h, 3, chamber);
                    try {
                        var before = actor.getPersistentData().copy();
                        h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginChairVisit(actor),
                                "Chair entry accepted a player already inside the Chamber");
                        h.assertTrue(before.equals(actor.getPersistentData()),
                                "Rejected in-Chamber entry changed binding or visit state");
                        h.succeed();
                    } finally { actor.discard(); }
                }));
    }

    private static net.minecraft.server.level.ServerLevel requireLoadedChamber(GameTestHelper h) {
        var chamber = h.getLevel().getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL);
        h.assertTrue(chamber != null, "Loaded-entry acceptance requires the real Chamber destination");
        return chamber;
    }

    private static void loadedObservationalSnapshotRecovery(GameTestHelper h, ChamberVisitMode mode, boolean outsideRecovery) {
        loadedObservationalSnapshotRecovery(h, mode, outsideRecovery, false);
    }

    private static void loadedObservationalSnapshotRecovery(GameTestHelper h, ChamberVisitMode mode,
            boolean outsideRecovery, boolean pendingGifts) {
        var chamber = requireLoadedChamber(h);
        int degree = mode == ChamberVisitMode.GUIDED ? (pendingGifts ? 7 : 3) : 1;
        var actor = player(h, degree);
        var originArea = actor.getBoundingBox().inflate(8);
        var manager = ChamberOfWillManager.get(actor.server);
        var cell = manager.cellPos(manager.idFor(actor.getUUID()));
        var chamberArea = new AABB(cell).inflate(16);
        var originChunks = itemQueryChunks(originArea);
        var chamberChunks = itemQueryChunks(chamberArea);
        var originTicket = new net.minecraft.world.level.ChunkPos(actor.blockPosition());
        var chamberTicket = new net.minecraft.world.level.ChunkPos(cell);
        boolean chamberWasForced = chamber.getForcedChunks().contains(chamberTicket.toLong());
        var probes = new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
        var baselines = new java.util.ArrayList<java.util.Set<UUID>>();
        Runnable cleanup = () -> {
            probes.forEach(net.minecraft.world.entity.Entity::discard);
            actor.containerMenu.setCarried(ItemStack.EMPTY);
            actor.inventoryMenu.setCarried(ItemStack.EMPTY);
            actor.inventoryMenu.clearCraftingContent();
            actor.getInventory().clearContent();
            if (actor.containerMenu != actor.inventoryMenu) actor.closeContainer();
            actor.discard();
            h.getLevel().getChunkSource().removeRegionTicket(CHAMBER_ITEM_FIXTURE_TICKET, originTicket, 2, actor.getUUID(), true);
            chamber.getChunkSource().removeRegionTicket(CHAMBER_ITEM_FIXTURE_TICKET, chamberTicket, 2, actor.getUUID(), true);
            if (!chamberWasForced) chamber.setChunkForced(chamberTicket.x, chamberTicket.z, false);
        };
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo test) { }
            public void testPassed(GameTestInfo test, net.minecraft.gametest.framework.GameTestRunner runner) { cleanup.run(); }
            public void testFailed(GameTestInfo test, net.minecraft.gametest.framework.GameTestRunner runner) { cleanup.run(); }
            public void testAddedForRerun(GameTestInfo oldTest, GameTestInfo newTest, net.minecraft.gametest.framework.GameTestRunner runner) { }
        });
        h.getLevel().getChunkSource().addRegionTicket(CHAMBER_ITEM_FIXTURE_TICKET, originTicket, 2, actor.getUUID(), true);
        chamber.getChunkSource().addRegionTicket(CHAMBER_ITEM_FIXTURE_TICKET, chamberTicket, 2, actor.getUUID(), true);
        // Transient tickets load chunks but do not keep an empty dimension's entity loop running.
        chamber.setChunkForced(chamberTicket.x, chamberTicket.z, true);
        h.startSequence().thenWaitUntil(() -> h.assertTrue(
                originChunks.stream().allMatch(chunk -> h.getLevel().areEntitiesLoaded(chunk.toLong()))
                        && chamberChunks.stream().allMatch(chunk -> chamber.areEntitiesLoaded(chunk.toLong())),
                "Every observational item-query chunk must become entity-loaded"))
                .thenExecute(() -> {
            probes.add(itemQueryProbe(h, h.getLevel(), actor.position().add(2, 1, 0)));
            probes.add(itemQueryProbe(h, chamber, Vec3.atCenterOf(cell.offset(2, 1, 2))));
            var originDrops = itemEntityIds(h.getLevel(), originArea);
            var chamberDrops = itemEntityIds(chamber, chamberArea);
            h.assertTrue(originDrops.contains(probes.get(0).getUUID()) && chamberDrops.contains(probes.get(1).getUUID()),
                    "Item-query positive controls must be visible before recording baselines");
            baselines.add(originDrops);
            baselines.add(chamberDrops);
            for (int slot = 0; slot < 36; slot++) actor.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
            actor.getInventory().setItem(39, new ItemStack(Items.DIAMOND_HELMET));
            actor.getInventory().setItem(40, new ItemStack(Items.SHIELD));
            if (pendingGifts) prepareLoadedPendingGifts(h, actor, mode);
            actor.inventoryMenu.getSlot(1).set(new ItemStack(Items.OAK_PLANKS, 17));
            actor.inventoryMenu.getSlot(4).set(new ItemStack(Items.DIAMOND, 3));
            var cursor = mode == ChamberVisitMode.GUIDED ? ItemStack.EMPTY : new ItemStack(Items.EMERALD, 2);
            if (!cursor.isEmpty()) cursor.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Dream entry cursor"));
            actor.inventoryMenu.setCarried(cursor.copy());
            var inventory = actor.getInventory().save(new net.minecraft.nbt.ListTag());
            var crafting = new net.minecraft.nbt.ListTag();
            for (int slot = 1; slot <= 4; slot++)
                crafting.add(actor.inventoryMenu.getSlot(slot).getItem().saveOptional(actor.registryAccess()));
            var origin = actor.position();
            var equipment = (com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentContainer)
                    HemoCapabilityAccess.requireEquipment(actor);
            var gourd = mode == ChamberVisitMode.GUIDED ? new ItemStack(ItemInit.blood_gourd_red.get()) : ItemStack.EMPTY;
            if (mode == ChamberVisitMode.GUIDED) {
                gourd.set(DataComponents.CUSTOM_NAME, Component.literal("Guided entry reserve"));
                HemoCapabilityAccess.getBloodVolume(gourd).orElseThrow().setBloodVolume(123);
                equipment.setStackInSlot(6, gourd.copy());
            }
            var equipmentSnapshot = equipment.serializeNBT(actor.registryAccess());
            boolean equipmentEventsBlocked = equipment.isEventBlocked();

            if (mode == ChamberVisitMode.GUIDED) {
                HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
                h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginGuidedVisit(actor),
                        "Eligible guided fixture did not begin its visit");
            } else {
                actor.getPersistentData().putInt("hemomancy:chamber_visit_dream_attempts", 2);
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.onCompletedSleep(actor, true);
            }
            h.assertTrue(actor.serverLevel().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)
                            && com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(actor),
                    "Observational fixture did not enter the actual Chamber for " + mode);
            h.assertTrue(inventory.equals(actor.getPersistentData().getList("hemomancy:chamber_visit_dream_inventory", 10))
                            && crafting.equals(actor.getPersistentData().getList("hemomancy:chamber_visit_dream_crafting", 10))
                            && ItemStack.matches(cursor, ItemStack.parseOptional(actor.registryAccess(),
                                    actor.getPersistentData().getCompound("hemomancy:chamber_visit_dream_carried"))),
                    "Real observational entry did not capture inventory, crafting inputs, and cursor for " + mode);
            h.assertTrue(originDrops.equals(itemEntityIds(h.getLevel(), originArea))
                            && chamberDrops.equals(itemEntityIds(chamber, chamberArea)),
                    "Observational entry leaked item entities for " + mode);
            if (mode == ChamberVisitMode.GUIDED) {
                h.assertTrue(equipmentSnapshot.equals(actor.getPersistentData().getCompound("hemomancy:chamber_visit_guided_equipment")),
                        "Real guided entry did not capture its equipment");
                equipment.setStackInSlot(6, ItemStack.EMPTY);
            }

            actor.inventoryMenu.getSlot(1).set(ItemStack.EMPTY);
            actor.inventoryMenu.getSlot(2).set(new ItemStack(Items.STONE, 64));
            actor.inventoryMenu.setCarried(ItemStack.EMPTY);
            if (pendingGifts) {
                actor.getInventory().setItem(0, ItemStack.EMPTY);
                retryPendingChamberGifts(actor);
                h.assertTrue(actor.getInventory().getItem(0).isEmpty()
                                && actor.getInventory().countItem(ItemInit.bloody_vial.get()) == 0
                                && actor.getInventory().countItem(ItemInit.living_syringe.get()) == 0
                                && actor.getInventory().countItem(ItemInit.vial_rack.get()) == 0
                                && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 0
                                && actor.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 0
                                && actor.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 0,
                        "Actual observational entry must defer all pending gifts despite available space");
                if (mode == ChamberVisitMode.GUIDED) {
                    var persisted = actor.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                    h.assertTrue(!actor.getPersistentData().contains("hemomancy:vesper_memory_pending")
                                    && persisted.getBoolean("hemomancy:vesper_memory_pending"),
                            "Observational deferral must still migrate the explicit legacy Memory claim");
                }
            }
            if (outsideRecovery) {
                actor.changeDimension(new net.minecraft.world.level.portal.DimensionTransition(h.getLevel(), origin,
                        Vec3.ZERO, 0, 0, net.minecraft.world.level.portal.DimensionTransition.DO_NOTHING));
                actor.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (id, inv, p) -> net.minecraft.world.inventory.ChestMenu.threeRows(id, inv),
                        Component.literal("Recovery fixture chest")));
                h.assertTrue(actor.containerMenu != actor.inventoryMenu, "Recovery fixture did not open another menu");
                actor.containerMenu.setCarried(new ItemStack(Items.DIAMOND_HELMET));
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.recoverOutsideChamber(actor);
            } else {
                actor.inventoryMenu.setCarried(new ItemStack(Items.DIAMOND_HELMET));
                actor.getPersistentData().putInt("hemomancy:chamber_visit_remaining", 1);
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.tick(actor);
            }
            h.assertTrue(actor.serverLevel() == h.getLevel(),
                    "Observational recovery remained in " + actor.serverLevel().dimension());
            h.assertTrue(actor.containerMenu == actor.inventoryMenu, "Observational recovery retained another menu");
            h.assertTrue(inventory.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                    "Observational recovery changed inventory: " + actor.getInventory().save(new net.minecraft.nbt.ListTag()));
            h.assertTrue(ItemStack.matches(cursor, actor.inventoryMenu.getCarried()),
                    "Observational recovery changed its cursor: " + actor.inventoryMenu.getCarried().saveOptional(actor.registryAccess()));
            for (int slot = 1; slot <= 4; slot++)
                h.assertTrue(ItemStack.matches(actor.inventoryMenu.getSlot(slot).getItem(),
                                ItemStack.parseOptional(actor.registryAccess(), crafting.getCompound(slot - 1))),
                        "Observational recovery changed crafting slot " + slot);
            if (mode == ChamberVisitMode.GUIDED)
                h.assertTrue(ItemStack.matches(gourd, equipment.getStackInSlot(6))
                                && equipment.isEventBlocked() == equipmentEventsBlocked,
                        "Guided recovery changed its equipped reserve or event-block state");
            h.assertTrue(originDrops.equals(itemEntityIds(h.getLevel(), originArea))
                            && chamberDrops.equals(itemEntityIds(chamber, chamberArea)),
                    "Observational recovery leaked item entities for " + mode);
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(actor)
                            && !actor.getPersistentData().contains("hemomancy:chamber_visit_dream_crafting")
                            && actor.getPersistentData().getBoolean("hemomancy:chamber_visit_guided_complete")
                                    == (mode == ChamberVisitMode.GUIDED && !outsideRecovery)
                            && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isAttuned(actor),
                    "Observational recovery retained visit state or miscredited guided completion/attunement");
            if (pendingGifts) {
                actor.getInventory().clearContent();
                retryPendingChamberGifts(actor);
                int vials = mode == ChamberVisitMode.GUIDED ? 7 : 5;
                h.assertTrue(actor.getInventory().countItem(ItemInit.bloody_vial.get()) == vials,
                        "Real Chamber return/recovery lost its pending vial claims");
                if (mode == ChamberVisitMode.GUIDED) {
                    for (var item : List.of(ItemInit.living_syringe.get(), ItemInit.vial_rack.get(),
                            ItemInit.fungal_spine.get(), ItemInit.memory_of_vesper.get(), ItemInit.mycophant_tendril.get()))
                        h.assertTrue(actor.getInventory().countItem(item) == 1,
                                "Real guided return/recovery lost its pending " + item);
                    var rack = actor.getInventory().items.stream().filter(s -> s.is(ItemInit.vial_rack.get())).findFirst().orElseThrow();
                    h.assertTrue(rack.getComponents().equals(FirstSeparationAssignment.rewardStacks().get(1).getComponents()),
                            "Actual return/recovery changed the initialized pending rack");
                }
                var delivered = actor.getInventory().save(new net.minecraft.nbt.ListTag());
                retryPendingChamberGifts(actor);
                h.assertTrue(delivered.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag()))
                                && HemoCapabilityAccess.getPlayerDegreeNumber(actor) == degree,
                        "Actual return/recovery duplicated pending gifts or changed degree");
            }
        }).thenWaitUntil(() -> h.assertTrue(probes.stream().allMatch(probe -> probe.isAlive() && probe.tickCount >= 5),
                "Both item-query probes must receive five natural ticks; " + probes.stream().map(probe ->
                        probe.level().dimension() + ": ticks=" + probe.tickCount + ", alive=" + probe.isAlive()
                                + ", indexed=" + (probe.level() instanceof net.minecraft.server.level.ServerLevel level
                                        && level.getEntity(probe.getUUID()) == probe)
                                + ", pos=" + probe.position()).toList()))
                .thenExecute(() -> {
            h.assertTrue(originChunks.stream().allMatch(chunk -> h.getLevel().areEntitiesLoaded(chunk.toLong()))
                            && chamberChunks.stream().allMatch(chunk -> chamber.areEntitiesLoaded(chunk.toLong())),
                    "Observational item-query chunks stopped being entity-loaded");
            h.assertTrue(baselines.get(0).equals(itemEntityIds(h.getLevel(), originArea))
                            && baselines.get(1).equals(itemEntityIds(chamber, chamberArea)),
                    "Observational recovery leaked item entities after natural ticks for " + mode);
        }).thenSucceed();
    }

    private static void prepareLoadedPendingGifts(GameTestHelper h, ServerPlayer actor, ChamberVisitMode mode) {
        HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(actor.position());
        try {
            h.assertTrue(FirstDrawsAssignment.brief(actor, alchemist), "Loaded fixture must record pending Draws supplies");
            if (mode != ChamberVisitMode.GUIDED) return;
            FirstSeparationAssignment.markBriefed(actor);
            FirstSeparationAssignment.giveBriefingSupplies(actor);
            HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
            HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
            h.assertTrue(FirstSeparationAssignment.claimRewards(actor), "Loaded fixture must record its supplied tool claim");
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setQliphothCommunionDone(true);
            actor.getPersistentData().putBoolean("hemomancy:vesper_memory_pending", true);
            var persisted = actor.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            persisted.putBoolean("hemomancy:mycophant_tendril_pending", true);
            actor.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        } finally { alchemist.discard(); }
    }

    private static java.util.List<net.minecraft.world.level.ChunkPos> itemQueryChunks(AABB area) {
        return net.minecraft.world.level.ChunkPos.rangeClosed(
                new net.minecraft.world.level.ChunkPos(BlockPos.containing(area.minX, 0, area.minZ)),
                new net.minecraft.world.level.ChunkPos(BlockPos.containing(area.maxX, 0, area.maxZ))).toList();
    }

    private static net.minecraft.world.entity.item.ItemEntity itemQueryProbe(GameTestHelper h,
            net.minecraft.server.level.ServerLevel level, Vec3 position) {
        var probe = new net.minecraft.world.entity.item.ItemEntity(level, position.x, position.y, position.z,
                new ItemStack(Items.STONE));
        probe.setNoGravity(true);
        probe.setInvulnerable(true);
        probe.setPickUpDelay(32767);
        probe.setDeltaMovement(Vec3.ZERO);
        h.assertTrue(level.addFreshEntity(probe), "Could not spawn item-query positive control");
        return probe;
    }

    private static java.util.Set<UUID> itemEntityIds(net.minecraft.server.level.ServerLevel level, AABB area) {
        return level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).stream()
                .map(net.minecraft.world.entity.Entity::getUUID).collect(java.util.stream.Collectors.toSet());
    }

    @GameTest(template = "empty") public static void rejectedChairVisitPreservesAnActiveVisit(GameTestHelper h) {
        var actor = player(h, 3);
        try {
            actor.getPersistentData().putBoolean("hemomancy:chamber_visit_active", true);
            actor.getPersistentData().putString("hemomancy:chamber_visit_mode", "GUIDED");
            actor.getPersistentData().putInt("hemomancy:chamber_visit_remaining", 1200);
            var before = actor.getPersistentData().copy();
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginChairVisit(actor),
                    "Chair entry overlapped an active guided visit");
            h.assertTrue(before.equals(actor.getPersistentData()),
                    "Rejected overlapping chair entry granted binding or changed the original visit");
            h.succeed();
        } finally {
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void rejectedChairVisitPreservesFungalProjection(GameTestHelper h) {
        var actor = player(h, 7);
        try {
            actor.getPersistentData().putBoolean("hemomancy:fungal_projection_active", true);
            actor.getPersistentData().putInt("hemomancy:fungal_projection_remaining", 1200);
            var before = actor.getPersistentData().copy();
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginChairVisit(actor),
                    "Chair entry overlapped a Fungal projection");
            h.assertTrue(before.equals(actor.getPersistentData()),
                    "Rejected projecting chair entry granted binding or changed the projection");
            h.succeed();
        } finally {
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void unavailableChairDestinationDoesNotGrantBinding(GameTestHelper h) {
        var actor = player(h, 3);
        try {
            h.assertTrue(actor.server.getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) == null,
                    "This failed-start fixture requires an unloaded Chamber destination");
            var before = actor.getPersistentData().copy();
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginChairVisit(actor),
                    "Chair entry succeeded without its destination");
            h.assertTrue(before.equals(actor.getPersistentData()),
                    "Missing Chamber destination still granted chair binding");
            h.succeed();
        } finally {
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void observationalSingleAndStackDropsPreserveFullInventoryImmediately(GameTestHelper h) {
        for (String mode : List.of("DREAM", "GUIDED")) {
            var actor = player(h, 3);
            try {
                fillObservationalDropInventory(actor, new ItemStack(Items.STONE, 64), mode);
                var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
                h.assertTrue(!actor.drop(false), "Observational single-item drop entered the world");
                h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                        "Cancelled " + mode + " drop removed an item until return");
                h.assertTrue(!actor.drop(true), "Observational whole-stack drop entered the world");
                h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                        "Cancelled " + mode + " stack drop changed inventory or components");
            } finally {
                actor.getPersistentData().remove("hemomancy:chamber_visit_active");
                actor.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void observationalInventoryScreenDropPreservesVesselComponentsImmediately(GameTestHelper h) {
        var actor = player(h, 3);
        var vessel = new ItemStack(ItemInit.blood_gourd_white.get());
        HemoCapabilityAccess.getBloodVolume(vessel).orElseThrow().setBloodVolume(212);
        try {
            fillObservationalDropInventory(actor, vessel, "GUIDED");
            var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
            var expected = vessel.copy();
            actor.inventoryMenu.clicked(36, 0, ClickType.PICKUP, actor);
            h.assertTrue(actor.inventoryMenu.getCarried().is(ItemInit.blood_gourd_white.get()), "Fixture did not pick up the vessel");
            actor.inventoryMenu.clicked(-999, 0, ClickType.PICKUP, actor);
            h.assertTrue(ItemStack.matches(actor.inventoryMenu.getCarried(), expected),
                    "Rejected inventory-screen drop did not retain the vessel on the cursor");
            actor.inventoryMenu.clicked(36, 0, ClickType.PICKUP, actor);
            h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                    "Returning the rejected cursor drop changed vessel components or blood");
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, actor.getBoundingBox().inflate(4),
                            item -> item.getItem().is(ItemInit.blood_gourd_white.get())).isEmpty(),
                    "Observational inventory-screen drop spawned a vessel");
        } finally {
            actor.getPersistentData().remove("hemomancy:chamber_visit_active");
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void observationalEquipmentDropsKeepFullInventoryAndCursor(GameTestHelper h) {
        for (String mode : List.of("DREAM", "GUIDED")) {
            var actor = player(h, 3);
            try {
                fillObservationalDropInventory(actor, new ItemStack(Items.STONE, 64), mode);
                actor.getInventory().setItem(39, new ItemStack(Items.DIAMOND_HELMET));
                actor.getInventory().setItem(40, new ItemStack(Items.SHIELD));
                var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
                for (int slot : List.of(5, 45)) {
                    actor.inventoryMenu.clicked(slot, 1, ClickType.THROW, actor);
                    h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                            "Rejected " + mode + " equipment throw changed a full inventory");
                    actor.inventoryMenu.clicked(slot, 0, ClickType.PICKUP, actor);
                    var carried = actor.inventoryMenu.getCarried().copy();
                    for (int button : List.of(0, 1)) {
                        actor.inventoryMenu.clicked(-999, button, ClickType.PICKUP, actor);
                        h.assertTrue(ItemStack.matches(carried, actor.inventoryMenu.getCarried()),
                                "Rejected equipment cursor drop lost its item");
                        actor.inventoryMenu.clicked(-999, button, ClickType.QUICK_MOVE, actor);
                        h.assertTrue(ItemStack.matches(carried, actor.inventoryMenu.getCarried()),
                                "Outside quick-move bypassed observational equipment protection");
                    }
                    actor.inventoryMenu.clicked(slot, 0, ClickType.PICKUP, actor);
                    h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                            "Returning equipment from the cursor changed its components");
                }
            } finally {
                actor.inventoryMenu.setCarried(ItemStack.EMPTY);
                actor.getPersistentData().remove("hemomancy:chamber_visit_active");
                actor.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void observationalFormationKeyDoesNotSpendBlood(GameTestHelper h) {
        var actor = player(h, 3);
        try {
            fillObservationalDropInventory(actor, new ItemStack(Items.IRON_SWORD), "GUIDED");
            var blood = HemoCapabilityAccess.requireBloodVolume(actor);
            blood.setActive(true);
            blood.setBloodVolume(1000);
            var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
            var context = new net.neoforged.neoforge.network.handling.ServerPayloadContext(actor.connection,
                    com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodFormationKeyPressPacket.TYPE.id());
            com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodFormationKeyPressPacket.handle(
                    new com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodFormationKeyPressPacket(), context);
            h.assertTrue(blood.getBloodVolume() == 1000
                            && before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                    "Observational formation key spent blood or changed inventory");
        } finally {
            actor.getPersistentData().remove("hemomancy:chamber_visit_active");
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void observationalRecoveryClearsCursorBeforeRestoringInventory(GameTestHelper h) {
        for (String mode : List.of("DREAM", "GUIDED")) {
            var actor = player(h, 3);
            try {
                fillObservationalDropInventory(actor, new ItemStack(Items.STONE, 64), mode);
                actor.getInventory().setItem(39, new ItemStack(Items.DIAMOND_HELMET));
                var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
                actor.getPersistentData().put("hemomancy:chamber_visit_dream_inventory", before.copy());
                actor.inventoryMenu.clicked(5, 0, ClickType.PICKUP, actor);
                h.assertTrue(actor.inventoryMenu.getCarried().is(Items.DIAMOND_HELMET), "Recovery fixture did not hold its helmet");
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.recoverOutsideChamber(actor);
                h.assertTrue(actor.inventoryMenu.getCarried().isEmpty()
                                && before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                        "Observational " + mode + " recovery duplicated its restored helmet on the cursor");
                h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(actor)
                                && !actor.getPersistentData().getBoolean("hemomancy:chamber_visit_guided_complete"),
                        "Outside recovery completed an invalid guided return");
            } finally {
                actor.inventoryMenu.setCarried(ItemStack.EMPTY);
                actor.getPersistentData().remove("hemomancy:chamber_visit_active");
                actor.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void observationalRecoveryRestoresCraftingInputsWithoutDuplicates(GameTestHelper h) {
        for (String mode : List.of("DREAM", "GUIDED")) {
            var actor = player(h, 3);
            try {
                fillObservationalDropInventory(actor, new ItemStack(Items.STONE, 64), mode);
                actor.inventoryMenu.getSlot(1).set(new ItemStack(Items.DIAMOND));
                var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
                var crafting = new net.minecraft.nbt.ListTag();
                for (int slot = 1; slot <= 4; slot++)
                    crafting.add(actor.inventoryMenu.getSlot(slot).getItem().saveOptional(actor.registryAccess()));
                actor.getPersistentData().put("hemomancy:chamber_visit_dream_inventory", before.copy());
                actor.getPersistentData().put("hemomancy:chamber_visit_dream_crafting", crafting);
                var entryCursor = mode.equals("DREAM") ? new ItemStack(Items.EMERALD, 2) : ItemStack.EMPTY;
                actor.getPersistentData().put("hemomancy:chamber_visit_dream_carried", entryCursor.saveOptional(actor.registryAccess()));
                actor.inventoryMenu.clicked(36, 0, ClickType.PICKUP, actor);
                actor.inventoryMenu.clicked(2, 0, ClickType.PICKUP, actor);
                h.assertTrue(actor.inventoryMenu.getSlot(2).getItem().is(Items.STONE), "Crafting fixture did not move its stack");
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.recoverOutsideChamber(actor);
                h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag()))
                                && actor.inventoryMenu.getSlot(1).getItem().is(Items.DIAMOND)
                                && actor.inventoryMenu.getSlot(1).getItem().getCount() == 1
                                && actor.inventoryMenu.getSlot(2).getItem().isEmpty(),
                        "Observational recovery lost entry crafting inputs or duplicated its moved stack");
                h.assertTrue(!actor.getPersistentData().contains("hemomancy:chamber_visit_dream_crafting"),
                        "Completed recovery retained its transient crafting snapshot");
                h.assertTrue(ItemStack.matches(entryCursor, actor.inventoryMenu.getCarried()),
                        "Recovery lost or duplicated the original dream cursor state");
            } finally {
                actor.inventoryMenu.setCarried(ItemStack.EMPTY);
                actor.inventoryMenu.clearCraftingContent();
                actor.getPersistentData().remove("hemomancy:chamber_visit_active");
                actor.discard();
            }
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void timedChairStillAllowsOrdinaryDrops(GameTestHelper h) {
        var actor = player(h, 3);
        var source = new ItemStack(Items.STONE, 64);
        var helmet = new ItemStack(Items.DIAMOND_HELMET);
        helmet.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Chair helmet " + actor.getUUID()));
        var shield = new ItemStack(Items.SHIELD);
        shield.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Chair shield " + actor.getUUID()));
        try {
            fillObservationalDropInventory(actor, source, "TIMED_CHAIR");
            h.assertTrue(actor.drop(false) && actor.getInventory().getItem(0).getCount() == 63,
                    "A non-observational chair visit stopped ordinary dropping");
            var items = h.getLevel().getEntitiesOfClass(ItemEntity.class, actor.getBoundingBox().inflate(4),
                    item -> ItemStack.isSameItemSameComponents(item.getItem(), source));
            h.assertTrue(items.size() == 1 && items.getFirst().getItem().getCount() == 1,
                    "Chair drop did not produce exactly the removed item");
            actor.getInventory().setItem(39, helmet.copy());
            actor.inventoryMenu.clicked(5, 1, ClickType.THROW, actor);
            h.assertTrue(actor.getInventory().getItem(39).isEmpty()
                            && !h.getLevel().getEntitiesOfClass(ItemEntity.class, actor.getBoundingBox().inflate(4),
                                    item -> ItemStack.isSameItemSameComponents(item.getItem(), helmet)).isEmpty(),
                    "Timed chair inventory throw was incorrectly protected");
            actor.getInventory().setItem(40, shield.copy());
            actor.inventoryMenu.clicked(45, 0, ClickType.PICKUP, actor);
            actor.inventoryMenu.clicked(-999, 0, ClickType.QUICK_MOVE, actor);
            h.assertTrue(actor.inventoryMenu.getCarried().isEmpty()
                            && !h.getLevel().getEntitiesOfClass(ItemEntity.class, actor.getBoundingBox().inflate(4),
                                    item -> ItemStack.isSameItemSameComponents(item.getItem(), shield)).isEmpty(),
                    "Timed chair outside-cursor drop was incorrectly protected");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, actor.getBoundingBox().inflate(4),
                    item -> ItemStack.isSameItemSameComponents(item.getItem(), source)
                            || ItemStack.isSameItemSameComponents(item.getItem(), helmet)
                            || ItemStack.isSameItemSameComponents(item.getItem(), shield)).forEach(ItemEntity::discard);
            actor.getPersistentData().remove("hemomancy:chamber_visit_active");
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void observationalDropKeepsTransformedStaffAndPairedOffhand(GameTestHelper h) {
        var actor = player(h, 3);
        try {
            fillObservationalDropInventory(actor, new ItemStack(ItemInit.living_staff.get()), "GUIDED");
            HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
            HemoCapabilityAccess.requireBloodVolume(actor).setBloodVolume(1000);
            h.assertTrue(LivingStaffWeaponFormHelper.applySelection(actor, ManipulationInit.conjure_claws.get())
                            && actor.getOffhandItem().is(ItemInit.living_baghnakh.get()),
                    "Fixture did not form paired claws from its Staff");
            var before = actor.getInventory().save(new net.minecraft.nbt.ListTag());
            h.assertTrue(!actor.drop(false), "Observational weapon-form drop entered the world");
            h.assertTrue(before.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag())),
                    "Cancelled drop restored the Staff form or removed its paired offhand");
        } finally {
            actor.getPersistentData().remove("hemomancy:chamber_visit_active");
            actor.discard();
        }
        h.succeed();
    }

    private static void fillObservationalDropInventory(ServerPlayer actor, ItemStack selected, String mode) {
        for (int slot = 0; slot < actor.getInventory().items.size(); slot++)
            actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        selected.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Guided visit reserve " + actor.getUUID()));
        actor.getInventory().setItem(0, selected);
        actor.getInventory().selected = 0;
        actor.getPersistentData().putBoolean("hemomancy:chamber_visit_active", true);
        actor.getPersistentData().putString("hemomancy:chamber_visit_mode", mode);
    }

    @GameTest(template = "empty") public static void pallidVesselRequiresThirdDegree(GameTestHelper h) {
        verifyVesselDegreeGate(h, "pallid_vessel_rite", 3);
    }

    @GameTest(template = "empty") public static void crimsonVesselRequiresFourthDegree(GameTestHelper h) {
        verifyVesselDegreeGate(h, "crimson_vessel_rite", 4);
    }

    @GameTest(template = "empty") public static void ashenVesselRequiresFifthDegree(GameTestHelper h) {
        verifyVesselDegreeGate(h, "ashen_vessel_rite", 5);
    }

    @GameTest(template = "empty") public static void curvedHornRequiresSixthDegree(GameTestHelper h) {
        verifyVesselDegreeGate(h, "horn_of_culmination_rite", 6);
    }

    private static void verifyVesselDegreeGate(GameTestHelper h, String recipePath, int requiredDegree) {
        var actor = player(h, requiredDegree - 1);
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var center = origin.above();
        var saved = CardinalRiteSavedData.get(h.getLevel());
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, recipePath);
            actor.setPos(center.getCenter().add(0, 0.5, 2));
            var blood = HemoCapabilityAccess.requireBloodVolume(actor);
            blood.setActive(true);
            blood.setBloodVolume(1000);
            var staff = actor.getMainHandItem();
            var trigger = com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE;
            var attempt = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, center, trigger);
            h.assertTrue(attempt != com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && saved.getRite(actor.getUUID()) == null && actor.getMainHandItem() == staff
                            && blood.getBloodVolume() == 1000,
                    "An earlier-degree Staff owner started " + recipePath + " or lost blood/escrow");
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setDegreeNumber(requiredDegree);
            attempt = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, center, trigger);
            h.assertTrue(attempt == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && saved.getRite(actor.getUUID()) != null
                            && saved.getRite(actor.getUUID()).getRecipeId().equals(Hemomancy.rloc("cardinal_rite/" + recipePath)),
                    "The authored vessel gate rejected its eligible degree: " + recipePath);
        } finally {
            if (saved.getRite(actor.getUUID()) != null)
                com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, saved.getRite(actor.getUUID()));
            saved.removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void allVesselsFillCorkTransferEquipAndSerialize(GameTestHelper h) {
        var actor = player(h, 6);
        var pos = h.absolutePos(new BlockPos(4, 3, 4));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.ghastly_alembic.get().defaultBlockState());
        var alembic = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity) h.getLevel().getBlockEntity(pos);
        var playerBlood = HemoCapabilityAccess.requireBloodVolume(actor);
        var equipment = HemoCapabilityAccess.requireEquipment(actor);
        try {
            for (Item item : List.of(ItemInit.blood_gourd_white.get(), ItemInit.blood_gourd_red.get(),
                    ItemInit.blood_gourd_black.get(), ItemInit.curved_horn.get())) {
                var gourd = new ItemStack(item);
                gourd.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Saved reserve"));
                var gourdItem = (com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem) item;
                var stored = HemoCapabilityAccess.getBloodVolume(gourd).orElseThrow();
                alembic.getBloodCapability().setBloodVolume(500);
                alembic.setItem(4, gourd);
                com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity
                        .serverTick(h.getLevel(), pos, alembic.getBlockState(), alembic);
                h.assertTrue(stored.getBloodVolume() == 100 && alembic.getBloodCapability().getBloodVolume() == 400,
                        "Alembic did not conserve its 100 mL gourd transfer");
                alembic.setItem(4, ItemStack.EMPTY);
                playerBlood.setActive(true);
                playerBlood.setBloodVolume(1000);
                actor.setItemInHand(InteractionHand.MAIN_HAND, gourd);
                item.inventoryTick(gourd, h.getLevel(), actor, 0, true);
                h.assertTrue(playerBlood.getBloodVolume() == 1000 && stored.getBloodVolume() == 100,
                        "A corked gourd transferred blood");
                item.use(h.getLevel(), actor, InteractionHand.MAIN_HAND);
                item.inventoryTick(gourd, h.getLevel(), actor, 0, true);
                double rate = gourdItem.getTransferRate();
                h.assertTrue(playerBlood.getBloodVolume() == 1000 + rate && stored.getBloodVolume() == 100 - rate,
                        "Open gourd ignored its authored withdrawal rate");
                playerBlood.setActive(false);
                item.inventoryTick(gourd, h.getLevel(), actor, 0, true);
                h.assertTrue(stored.getBloodVolume() == 100 - rate, "Dormant blood drained its gourd");
                playerBlood.setActive(true);
                playerBlood.setBloodVolume(playerBlood.getMaxBloodVolume() - 0.25);
                item.inventoryTick(gourd, h.getLevel(), actor, 0, true);
                h.assertTrue(playerBlood.isFull() && stored.getBloodVolume() == 99.75 - rate,
                        "Gourd overflow lost fractional blood");
                item.use(h.getLevel(), actor, InteractionHand.MAIN_HAND);
                var menu = new com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu(
                        1, h.getLevel(), pos, actor.getInventory(), actor, true);
                menu.quickMoveStack(actor, 35);
                h.assertTrue(equipment.getStackInSlot(6).is(item) && actor.getMainHandItem().isEmpty(),
                        "Scarlet Vanity shift-click lost or duplicated its gourd");
                playerBlood.setBloodVolume(1000);
                var context = new net.neoforged.neoforge.network.handling.ServerPayloadContext(actor.connection,
                        com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleGourdKeyPacket.TYPE.id());
                com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleGourdKeyPacket.handle(
                        new com.vincenthuto.hemomancy.common.network.capa.harbinger.ToggleGourdKeyPacket(), context);
                gourdItem.onWornTick(actor);
                h.assertTrue(playerBlood.getBloodVolume() == 1000 + rate, "Equipped toggle did not open the gourd");
                var restored = new com.vincenthuto.hemomancy.common.capability.player.harbinger.equipment.HarbingerEquipmentContainer();
                restored.deserializeNBT(actor.registryAccess(), actor.getData(HemoAttachmentTypes.HARBINGER_EQUIPMENT)
                        .serializeNBT(actor.registryAccess()));
                h.assertTrue(ItemStack.isSameItemSameComponents(restored.getStackInSlot(6), equipment.getStackInSlot(6)),
                        "Equipment serialization changed gourd reserve, cork state, or name");
                equipment.setStackInSlot(6, ItemStack.EMPTY);
            }
        } finally {
            equipment.setStackInSlot(6, ItemStack.EMPTY);
            alembic.clearContent();
            h.getLevel().removeBlock(pos.above(), false);
            h.getLevel().removeBlock(pos, false);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void allVesselFormationRefillsUseTheFillSerializer(GameTestHelper h) {
        for (String recipe : List.of("blood_gourd_white_fill", "blood_gourd_red_fill", "blood_gourd_black_fill", "curved_horn_fill")) {
            h.assertTrue(h.getLevel().getRecipeManager().byKey(Hemomancy.rloc(recipe)).orElseThrow().value()
                            instanceof com.vincenthuto.hemomancy.common.recipe.FillBloodGourdRecipe,
                    "A gourd refill is wired to a non-filling serializer: " + recipe);
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void freshVesselFormationRefillActuallyAddsBlood(GameTestHelper h) {
        List<Item> vessels = List.of(ItemInit.blood_gourd_white.get(), ItemInit.blood_gourd_red.get(),
                ItemInit.blood_gourd_black.get(), ItemInit.curved_horn.get());
        List<String> recipes = List.of("blood_gourd_white_fill", "blood_gourd_red_fill", "blood_gourd_black_fill", "curved_horn_fill");
        for (int tier = 0; tier < vessels.size(); tier++) {
            var source = new ItemStack(vessels.get(tier));
            var original = source.copy();
            var input = gourdRefillInput(source);
            var recipe = (CraftingRecipe) h.getLevel().getRecipeManager().byKey(Hemomancy.rloc(recipes.get(tier))).orElseThrow().value();
            h.assertTrue(recipe.matches(input, h.getLevel()), "Refill fixture does not match " + recipes.get(tier));
            var output = recipe.assemble(input, h.getLevel().registryAccess());
            h.assertTrue(HemoCapabilityAccess.getBloodVolume(output).orElseThrow().getBloodVolume() == 200,
                    "A fresh vessel consumed its formations without gaining blood: " + recipes.get(tier));
            h.assertTrue(ItemStack.isSameItemSameComponents(source, original), "Refill preview mutated its fresh input");
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void vesselFormationRefillPreservesItsComponentsAndClampsCapacity(GameTestHelper h) {
        var inputGourd = new ItemStack(ItemInit.blood_gourd_red.get());
        inputGourd.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("My reserve"));
        HemoCapabilityAccess.getBloodVolume(inputGourd).orElseThrow().setBloodVolume(1750);
        var data = inputGourd.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putBoolean("state", true);
        inputGourd.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        var input = gourdRefillInput(inputGourd);
        var recipe = (CraftingRecipe) h.getLevel().getRecipeManager().byKey(Hemomancy.rloc("blood_gourd_red_fill")).orElseThrow().value();
        var output = recipe.assemble(input, h.getLevel().registryAccess());
        h.assertTrue(java.util.Objects.equals(output.get(DataComponents.CUSTOM_NAME), inputGourd.get(DataComponents.CUSTOM_NAME))
                        && output.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("state"),
                "Refilling erased the original vessel name or open state");
        h.assertTrue(HemoCapabilityAccess.getBloodVolume(output).orElseThrow().getBloodVolume() == 1800
                        && HemoCapabilityAccess.getBloodVolume(inputGourd).orElseThrow().getBloodVolume() == 1750,
                "Refill overflowed capacity or changed its crafting input");
        h.succeed();
    }

    private static CraftingInput gourdRefillInput(ItemStack gourd) {
        var formation = new ItemStack(ItemInit.sanguine_formation.get());
        return CraftingInput.of(3, 3, List.of(ItemStack.EMPTY, formation.copy(), ItemStack.EMPTY,
                formation.copy(), gourd, formation.copy(), ItemStack.EMPTY, formation.copy(), ItemStack.EMPTY));
    }

    @GameTest(template = "empty") public static void occupiedRootedVeinSiteRejectsBeforeStaffEscrow(GameTestHelper h) {
        var actor = player(h, 2);
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var center = origin.above();
        var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(h.getLevel());
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, "rooted_vein");
            actor.setPos(center.getCenter().add(0, 1, 2));
            HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
            var staff = actor.getMainHandItem();
            h.getLevel().setBlock(center.above(), Blocks.CHEST.defaultBlockState(), 3);
            var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) h.getLevel().getBlockEntity(center.above());
            chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
            var result = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, center,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(result != com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && saved.getRite(actor.getUUID()) == null,
                    "An occupied Rooted Vein site started its ceremony");
            h.assertTrue(actor.getMainHandItem() == staff && chest.getItem(0).getCount() == 3,
                    "A rejected Rooted Vein start changed the Staff or occupied inventory");
            h.succeed();
        } finally {
            var rite = saved.getRite(actor.getUUID());
            if (rite != null) com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, rite);
            saved.removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
    }

    @GameTest(template = "empty") public static void rootedVeinCompletionRechecksOccupiedSite(GameTestHelper h) {
        var actor = player(h, 2);
        var center = h.absolutePos(new BlockPos(6, 3, 6));
        h.getLevel().setBlock(center, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
        h.getLevel().setBlock(center.above(), Blocks.CHEST.defaultBlockState(), 3);
        var chest = (net.minecraft.world.level.block.entity.ChestBlockEntity) h.getLevel().getBlockEntity(center.above());
        chest.setItem(0, new ItemStack(Items.DIAMOND, 3));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                actor.getUUID(), center, Hemomancy.rloc("cardinal_rite/rooted_vein"), 400, 2, 2, false, 0, 4);
        try {
            var complete = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class
                    .getDeclaredMethod("completeRite", net.minecraft.server.level.ServerLevel.class,
                            ServerPlayer.class, com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.class);
            complete.setAccessible(true);
            h.assertTrue(!(boolean) complete.invoke(null, h.getLevel(), actor, rite),
                    "Rooted Vein overwrote a site occupied after activation");
            h.assertTrue(h.getLevel().getBlockState(center).is(BlockInit.cardinal_focus.get())
                            && h.getLevel().getBlockEntity(center.above()) == chest
                            && chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 3,
                    "Rejected Rooted Vein completion changed the Focus or occupied inventory");
            h.getLevel().removeBlock(center.above(), false);
            h.assertTrue((boolean) complete.invoke(null, h.getLevel(), actor, rite),
                    "A clear-site Rooted Vein retry did not complete");
            h.assertTrue(h.getLevel().getBlockState(center).is(BlockInit.venous_stone.get())
                            && h.getLevel().getBlockState(center.above()).is(BlockInit.earthen_vein.get()),
                    "Rooted Vein retry did not create its permanent result");
            h.succeed();
        } catch (ReflectiveOperationException exception) {
            h.fail("Rooted Vein completion could not run: " + exception);
        } finally {
            h.getLevel().removeBlock(center.above(), false);
            h.getLevel().removeBlock(center, false);
            actor.discard();
        }
    }

    @GameTest(template = "empty", timeoutTicks = 800)
    public static void illuminatusPaysRequiredBastionAndCompletesItsActualCeremony(GameTestHelper h) {
        var supportWarnings = new ArrayList<net.minecraft.network.protocol.game.ClientboundSystemChatPacket>();
        var actor = player(h, 4, h.getLevel(), packet -> {
            if (packet instanceof net.minecraft.network.protocol.game.ClientboundSystemChatPacket message
                    && message.content().getString().contains("support sigil has not awakened")) supportWarnings.add(message);
        });
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var center = origin.above();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        HemoJourneyFixtures.prepareCardinalRite(actor, origin, "illuminatus_rite");
        // Supplied chapter proof isolates the ceremony; the personal cycle/start is tested separately.
        HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
        actor.setPos(center.getCenter().add(0, 0.5, 2));
        actor.getMainHandItem().set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("Illuminatus ceremony Staff"));
        var staff = actor.getMainHandItem().copy();
        var blood = HemoCapabilityAccess.requireBloodVolume(actor);
        blood.setActive(true);
        blood.setBloodVolume(2000);
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                h.getLevel(), Hemomancy.rloc("cardinal_rite/illuminatus_rite"));
        var saved = CardinalRiteSavedData.get(h.getLevel());
        var chunk = new net.minecraft.world.level.ChunkPos(center);
        h.getLevel().getChunkSource().addRegionTicket(ROOTED_VEIN_TICKET, chunk, 2, actor.getUUID());
        setServerPlayerLookup(actor, true);
        Runnable cleanup = () -> {
            if (actor.isRemoved()) return;
            var rite = saved.getRite(actor.getUUID());
            if (rite != null) {
                com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.clearThreats(h.getLevel(), rite);
                com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, rite);
            }
            saved.removeRite(actor.getUUID());
            for (var daemon : h.getLevel().getEntitiesOfClass(HumanitySpriteEntity.class,
                    new AABB(center).inflate(128), entity -> entity.isBoundToRite(actor.getUUID()))) daemon.discard();
            HemoJourneyFixtures.cleanup(actor, origin);
            h.getLevel().getChunkSource().removeRegionTicket(ROOTED_VEIN_TICKET, chunk, 2, actor.getUUID());
            setServerPlayerLookup(actor, false);
            actor.discard();
        };
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo test) { }
            public void testPassed(GameTestInfo test, GameTestRunner runner) { }
            public void testFailed(GameTestInfo test, GameTestRunner runner) { cleanup.run(); }
            public void testAddedForRerun(GameTestInfo oldTest, GameTestInfo newTest, GameTestRunner runner) { }
        });
        h.startSequence()
                .thenWaitUntil(() -> h.assertTrue(h.getLevel().areEntitiesLoaded(chunk.toLong()),
                        "Illuminatus ceremony chunk is not ready"))
                .thenExecute(() -> {
                    actor.getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(actor,
                            InteractionHand.MAIN_HAND, new BlockHitResult(center.getCenter(), Direction.UP, center, false)));
                    h.assertTrue(saved.getRite(actor.getUUID()) != null, "Staff use did not start Illuminatus");
                })
                .thenIdle(25)
                .thenExecute(() -> {
                    actor.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
                    for (var anchor : recipe.getCeremony().anchors()) {
                        actor.lookAt(EntityAnchorArgument.Anchor.EYES,
                                CardinalRiteTargetGeometry.anchorAimPoint(center, anchor.offset()));
                        var payment = CardinalRiteInteractionHandler.tryProject(h.getLevel(), actor, 50);
                        h.assertTrue(payment.handled() && payment.bloodSpent() == 50,
                                "Illuminatus anchor must accept exactly 50 mL");
                    }
                    h.assertTrue(saved.getRite(actor.getUUID()).getPhase() == CardinalRitePhase.INSCRIPTION
                            && blood.getBloodVolume() == 1800, "Four paid anchors must open inscription without promotion");
                })
                .thenWaitUntil(() -> h.assertTrue(HumanitySpriteEntity.findBoundToRite(h.getLevel(),
                        actor.getUUID(), center) != null, "Illuminatus daemon has not emerged"))
                .thenExecute(() -> {
                    var rite = saved.getRite(actor.getUUID());
                    actor.lookAt(EntityAnchorArgument.Anchor.EYES,
                            HumanitySpriteEntity.findBoundToRite(h.getLevel(), actor.getUUID(), center).position());
                    for (int pulse = 0; pulse < 10; pulse++) CardinalRiteInteractionHandler.tryProject(h.getLevel(), actor, 50);
                    h.assertTrue(rite.getPhase() == CardinalRitePhase.INSCRIPTION && blood.getBloodVolume() == 1800,
                            "Missing Bastion support must reject sealing without spending blood");
                    h.assertTrue(!supportWarnings.isEmpty() && supportWarnings.stream().allMatch(message -> message.overlay()),
                            "Held Projection must show missing support in the action bar without filling chat: warnings="
                                    + supportWarnings.size());
                    var socket = recipe.getCeremony().supportSockets().getFirst();
                    var id = Hemomancy.rloc(socket.suggestedSigil());
                    var sigil = com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry.get(id);
                    var occupied = new java.util.HashSet<BlockPos>();
                    for (var anchor : recipe.getCeremony().anchors()) occupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
                    var placement = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules.resolveSupportPlacement(
                            new BlockPos(socket.x(), 0, socket.z()), sigil.nodes(), occupied);
                    actor.setPos(center.getCenter().add(2, 0.5, 2));
                    for (var node : sigil.nodes()) {
                        var surface = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules.surfaceAirPosition(
                                h.getLevel(), center.offset(0, socket.y(), 0),
                                placement.getX() + (int) Math.round(node.x()), placement.getZ() + (int) Math.round(node.z()));
                        actor.lookAt(EntityAnchorArgument.Anchor.EYES, CardinalRiteTargetGeometry.sigilAimPoint(center,
                                surface, placement.getX(), placement.getZ(), node.x(), node.z()));
                        var payment = CardinalRiteInteractionHandler.tryProject(h.getLevel(), actor, 50);
                        h.assertTrue(payment.handled() && payment.bloodSpent() == 50,
                                "Ordered Bastion node must accept exactly 50 mL: node=" + node
                                        + ", placement=" + placement + ", surface=" + surface
                                        + ", payment=" + payment + ", progress=" + rite.getSigilProgress()
                                        + ", eye=" + actor.getEyePosition());
                    }
                    h.assertTrue(rite.isSigilAwakened(id.toString()) && blood.getBloodVolume() == 1800 - 50 * sigil.nodes().size(),
                            "Fully paid Bastion must awaken without an extra blood debit");
                    actor.lookAt(EntityAnchorArgument.Anchor.EYES,
                            HumanitySpriteEntity.findBoundToRite(h.getLevel(), actor.getUUID(), center).position());
                    var seal = CardinalRiteInteractionHandler.tryProject(h.getLevel(), actor, 50);
                    h.assertTrue(seal.handled() && seal.bloodSpent() == 0 && rite.getPhase() == CardinalRitePhase.OFFERING_PROCESSION,
                            "Paid Bastion must allow the real daemon seal and zero-wave procession");
                    h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(actor) == 4,
                            "Sealing the altar must not promote before culmination");
                })
                .thenWaitUntil(() -> h.assertTrue(saved.getRite(actor.getUUID()) == null, "Illuminatus ceremony has not finished"))
                .thenExecute(() -> {
                    try {
                        h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(actor) == 5
                                && HarbingerAdvancementGranter.hasAdvancement(actor, HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED),
                                "Actual Illuminatus culmination must award D5 and its rite proof");
                        h.assertTrue(actor.getInventory().countItem(ItemInit.living_staff.get()) == 1
                                && actor.getInventory().items.stream().anyMatch(stack -> ItemStack.isSameItemSameComponents(stack, staff)),
                                "Illuminatus must return exactly one original named Staff");
                        h.assertTrue(blood.getBloodVolume() == 1500,
                                "Illuminatus must spend only its four anchors and six Bastion nodes, with no completion debit or refund");
                        var floor = com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElseThrow();
                        for (var socket : floor.brazierSockets().subList(0, 2)) {
                            var brazier = (com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity)
                                    h.getLevel().getBlockEntity(center.offset(socket.getX(), socket.getY(), -socket.getZ()));
                            h.assertTrue(brazier.getOfferingForMatching().isEmpty(), "Illuminatus must consume both authored offerings");
                        }
                    } finally { cleanup.run(); }
                }).thenSucceed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void rootedVeinCompletesThroughPaidAnchorsAndDaemon(GameTestHelper h) {
        var fixture = rootedVeinFixture(h);
        rootedVeinSequence(h, fixture)
                .thenExecute(() -> sealRootedVein(h, fixture))
                .thenWaitUntil(() -> h.assertTrue(fixture.rite().getPhase() == CardinalRitePhase.CULMINATION,
                        "Rooted Vein daemon has not returned from its offering"))
                .thenExecute(() -> h.assertTrue(fixture.brazier().getOfferingForMatching().isEmpty(),
                        "Rooted Vein did not consume its one Blood Rock"))
                .thenWaitUntil(() -> h.assertTrue(fixture.rite() == null, "Rooted Vein did not finish"))
                .thenExecute(() -> {
                    try {
                        assertRootedStaffReturned(h, fixture);
                        h.assertTrue(h.getLevel().getBlockState(fixture.center()).is(BlockInit.venous_stone.get())
                                        && h.getLevel().getBlockState(fixture.center().above()).is(BlockInit.earthen_vein.get()),
                                "Full Rooted Vein ceremony did not create the authored result");
                        var vein = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity)
                                h.getLevel().getBlockEntity(fixture.center().above());
                        h.assertTrue(!vein.isTemporary(), "Rooted Vein created a temporary result");
                        var restored = new com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity(
                                fixture.center().above(), vein.getBlockState());
                        restored.loadWithComponents(vein.saveWithFullMetadata(h.getLevel().registryAccess()), h.getLevel().registryAccess());
                        h.assertTrue(!restored.isTemporary(), "Rooted Vein lost its permanent state on serialization");
                        h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(fixture.actor()) == 2,
                                "Rooted Vein promoted its caster");
                    } finally { fixture.close(); }
                }).thenSucceed();
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void rootedVeinInterruptedAbsorptionReturnsExactStaffAndUntouchedOffering(GameTestHelper h) {
        verifyRootedVeinCancellation(h, false);
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void rootedVeinCancellationDoesNotRefundConsumedOffering(GameTestHelper h) {
        verifyRootedVeinCancellation(h, true);
    }

    private static void verifyRootedVeinCancellation(GameTestHelper h, boolean afterOffering) {
        var fixture = rootedVeinFixture(h);
        var sequence = rootedVeinSequence(h, fixture);
        if (afterOffering) {
            sequence.thenExecute(() -> sealRootedVein(h, fixture))
                    .thenWaitUntil(() -> h.assertTrue(fixture.rite().getPhase() == CardinalRitePhase.CULMINATION,
                            "Rooted Vein daemon has not consumed its offering"));
        }
        sequence.thenExecute(() -> fixture.actor().lookAt(EntityAnchorArgument.Anchor.EYES, fixture.center().getCenter()))
                .thenExecuteFor(10, () -> h.assertTrue(CardinalRiteCancellationHandler.tryChannel(fixture.actor(), 8),
                        "Absorption could not target the planted Staff"))
                .thenIdle(5)
                .thenExecute(() -> h.assertTrue(fixture.rite() != null && fixture.rite().hasEscrowedStaff()
                                && fixture.actor().getInventory().countItem(ItemInit.living_staff.get()) == 0,
                        "Interrupted absorption lost or duplicated the Staff"))
                .thenExecuteFor(80, () -> {
                    if (fixture.rite() != null) h.assertTrue(CardinalRiteCancellationHandler.tryChannel(fixture.actor(), 8),
                            "Resumed absorption could not target the planted Staff");
                })
                .thenWaitUntil(() -> h.assertTrue(fixture.rite() == null, "Resumed absorption did not finish cancellation"))
                .thenExecute(() -> {
                    try {
                        assertRootedStaffReturned(h, fixture);
                        h.assertTrue(h.getLevel().getBlockState(fixture.center()).is(BlockInit.cardinal_focus.get())
                                        && h.getLevel().getBlockState(fixture.center().above()).isAir(),
                                "Cancelled Rooted Vein changed its Focus or created a result");
                        var offering = fixture.brazier().getOfferingForMatching();
                        h.assertTrue(afterOffering ? offering.isEmpty() : offering.is(ItemInit.blood_rock.get()) && offering.getCount() == 1,
                                "Cancellation changed the consumed/untouched offering contract");
                    } finally { fixture.close(); }
                }).thenSucceed();
    }

    private static GameTestSequence rootedVeinSequence(GameTestHelper h, RootedVeinFixture fixture) {
        return h.startSequence()
                .thenWaitUntil(() -> h.assertTrue(h.getLevel().areEntitiesLoaded(
                        net.minecraft.world.level.ChunkPos.asLong(fixture.center())), "Rooted Vein entity chunk is not ready"))
                .thenExecute(() -> {
                    var hit = new BlockHitResult(fixture.center().getCenter(), Direction.UP, fixture.center(), false);
                    fixture.actor().getMainHandItem().useOn(new net.minecraft.world.item.context.UseOnContext(
                            fixture.actor(), InteractionHand.MAIN_HAND, hit));
                    h.assertTrue(fixture.rite() != null && fixture.rite().hasEscrowedStaff(),
                            "D2 Staff use did not start the authored Rooted Vein ceremony");
                })
                .thenIdle(25)
                .thenExecute(() -> {
                    var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
                            h.getLevel(), Hemomancy.rloc("cardinal_rite/rooted_vein"));
                    fixture.actor().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
                    for (var anchor : recipe.getCeremony().anchors()) {
                        fixture.actor().lookAt(EntityAnchorArgument.Anchor.EYES,
                                CardinalRiteTargetGeometry.anchorAimPoint(fixture.center(), anchor.offset()));
                        var projection = CardinalRiteInteractionHandler.tryProject(h.getLevel(), fixture.actor(), 50);
                        h.assertTrue(projection.handled() && projection.bloodSpent() == 50,
                                "Rooted Vein anchor did not receive exactly 50 mL");
                    }
                    h.assertTrue(fixture.rite().getPhase() == CardinalRitePhase.INSCRIPTION
                                    && HemoCapabilityAccess.requireBloodVolume(fixture.actor()).getBloodVolume() == 800,
                            "Four Rooted Vein anchors did not spend exactly 200 mL before inscription");
                })
                .thenWaitUntil(() -> h.assertTrue(HumanitySpriteEntity.findBoundToRite(h.getLevel(),
                        fixture.actor().getUUID(), fixture.center()) != null, "Rooted Vein daemon has not emerged"));
    }

    private static void sealRootedVein(GameTestHelper h, RootedVeinFixture fixture) {
        var daemon = HumanitySpriteEntity.findBoundToRite(h.getLevel(), fixture.actor().getUUID(), fixture.center());
        fixture.actor().lookAt(EntityAnchorArgument.Anchor.EYES, daemon.position());
        var projection = CardinalRiteInteractionHandler.tryProject(h.getLevel(), fixture.actor(), 50);
        h.assertTrue(projection.handled() && projection.bloodSpent() == 0
                        && fixture.rite().getPhase() == CardinalRitePhase.OFFERING_PROCESSION,
                "Projection into the daemon did not seal Rooted Vein without extra blood");
    }

    private static void assertRootedStaffReturned(GameTestHelper h, RootedVeinFixture fixture) {
        h.assertTrue(fixture.actor().getInventory().countItem(ItemInit.living_staff.get()) == 1
                        && fixture.actor().getInventory().items.stream().anyMatch(stack ->
                                ItemStack.isSameItemSameComponents(stack, fixture.staff())),
                "Rooted Vein did not return exactly one original Staff with its components");
        h.assertTrue(HemoCapabilityAccess.requireBloodVolume(fixture.actor()).getBloodVolume() == 800,
                "Rooted Vein refunded anchor blood or charged an extra completion cost");
    }

    private static RootedVeinFixture rootedVeinFixture(GameTestHelper h) {
        var actor = player(h, 2);
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var center = origin.above();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, h.getLevel().dimension().location().toString());
        HemoJourneyFixtures.prepareCardinalRite(actor, origin, "rooted_vein");
        actor.setPos(center.getCenter().add(0, 0.5, 2));
        actor.getMainHandItem().set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Rooted Vein test Staff"));
        var staff = actor.getMainHandItem().copy();
        var blood = HemoCapabilityAccess.requireBloodVolume(actor);
        blood.setActive(true);
        blood.setBloodVolume(1000);
        setServerPlayerLookup(actor, true);
        var chunk = new net.minecraft.world.level.ChunkPos(center);
        h.getLevel().getChunkSource().addRegionTicket(ROOTED_VEIN_TICKET, chunk, 2, actor.getUUID());
        var floor = com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry
                .get(Hemomancy.rloc("working_lesser")).orElseThrow();
        var socket = floor.brazierSockets().getFirst();
        var brazierPos = center.offset(socket.getX(), socket.getY(), -socket.getZ());
        var fixture = new RootedVeinFixture(actor, origin, center, staff, brazierPos, chunk);
        h.testInfo.addListener(new GameTestListener() {
            public void testStructureLoaded(GameTestInfo test) { }
            public void testPassed(GameTestInfo test, GameTestRunner runner) { }
            public void testFailed(GameTestInfo test, GameTestRunner runner) { fixture.close(); }
            public void testAddedForRerun(GameTestInfo oldTest, GameTestInfo newTest, GameTestRunner runner) { }
        });
        return fixture;
    }

    private record RootedVeinFixture(ServerPlayer actor, BlockPos origin, BlockPos center, ItemStack staff,
            BlockPos brazierPos, net.minecraft.world.level.ChunkPos chunk) {
        ActiveCardinalRite rite() { return CardinalRiteSavedData.get(actor.serverLevel()).getRite(actor.getUUID()); }
        com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity brazier() {
            return (com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity)
                    actor.serverLevel().getBlockEntity(brazierPos);
        }
        void close() {
            if (actor.isRemoved()) return;
            if (rite() != null) {
                com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.clearThreats(actor.serverLevel(), rite());
                com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(actor, rite());
            }
            CardinalRiteSavedData.get(actor.serverLevel()).removeRite(actor.getUUID());
            for (var daemon : actor.serverLevel().getEntitiesOfClass(HumanitySpriteEntity.class,
                    new AABB(center).inflate(128), entity -> entity.isBoundToRite(actor.getUUID()))) daemon.discard();
            if (actor.serverLevel().getBlockState(center.above()).is(BlockInit.earthen_vein.get()))
                actor.serverLevel().removeBlock(center.above(), false);
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.serverLevel().getChunkSource().removeRegionTicket(ROOTED_VEIN_TICKET, chunk, 2, actor.getUUID());
            setServerPlayerLookup(actor, false);
            actor.discard();
        }
    }

    private static void verifyStaffUtilitySelection(GameTestHelper h, BloodManipulation utility) {
        var p = player(h, 2);
        try {
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            known.getKnownManips().put(utility, new ManipLevel(0, 0));
            known.setSelectedManip(utility);
            var staff = new ItemStack(ItemInit.living_staff.get());
            p.setItemInHand(InteractionHand.MAIN_HAND, staff);
            staff.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
            h.assertTrue(p.isUsingItem(), "D2 Staff did not begin held use");
            h.assertTrue(com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem
                            .isLivingStaffUtilityUse(p, staff), "Selected Staff utility was unavailable at D2");

            known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
            h.assertTrue(known.getSelectedManip() == utility, "Fixture did not retain the legacy utility selection");
            h.assertTrue(!com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem
                            .isLivingStaffUtilityUse(p, staff), "Thelemic selection retained the hidden Staff utility");

            known.setSelectedMemoryRef(MemorySlotRef.manipulation(utility.getName()));
            h.assertTrue(com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem
                            .isLivingStaffUtilityUse(p, staff), "Returning to the utility did not restore Staff use");
            known.setSelectedMemoryRef(MemorySlotRef.manipulation(ManipulationInit.blood_shot.get().getName()));
            h.assertTrue(!com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem
                            .isLivingStaffUtilityUse(p, staff), "Another Noetic retained the hidden Staff utility");
            h.succeed();
        } finally { p.releaseUsingItem(); p.discard(); }
    }

    @GameTest(template = "empty") public static void thelemicSelectionRejectsStaleContinuousStart(GameTestHelper h) {
        var p = player(h, 5);
        try {
            prepareSharedSelection(p, ManipulationInit.sanguine_ward.get());
            var known = HemoCapabilityAccess.requireKnownManipulations(p);
            known.setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
            ManipulationChannelManager.start(p);
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()),
                    "Thelemic selection started the previously selected Noetic channel");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume() == 2000,
                    "Rejected stale channel spent blood");
            h.succeed();
        } finally { cleanupSharedSelection(p); }
    }

    @GameTest(template = "empty") public static void thelemicSelectionStopsRunningNoeticChannel(GameTestHelper h) {
        var p = player(h, 5);
        try {
            prepareSharedSelection(p, ManipulationInit.sanguine_ward.get());
            ManipulationChannelManager.start(p);
            h.assertTrue(ManipulationChannelManager.isChanneling(p.getUUID()), "Noetic channel fixture did not start");
            double blood = HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume();
            HemoCapabilityAccess.requireKnownManipulations(p)
                    .setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
            ManipulationChannelManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(!ManipulationChannelManager.isChanneling(p.getUUID()),
                    "Switching to a Thelemic memory retained the hidden Noetic channel");
            h.assertTrue(HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume() == blood,
                    "Selection interruption spent another channel pulse");
            h.succeed();
        } finally { cleanupSharedSelection(p); }
    }

    @GameTest(template = "empty") public static void thelemicSelectionRejectsStaleChargedPresentation(GameTestHelper h) {
        var p = player(h, 5);
        try {
            var mortar = ManipulationInit.hematic_mortar.get();
            prepareSharedSelection(p, mortar);
            CastingAnimationManager.charge(p, mortar.getName(), 4);
            h.assertTrue(CastingAnimationManager.current(p) != null, "Charged presentation fixture did not start");
            CastingAnimationManager.charge(p, mortar.getName(), 0);
            HemoCapabilityAccess.requireKnownManipulations(p)
                    .setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
            CastingAnimationManager.charge(p, mortar.getName(), 4);
            h.assertTrue(!CastingAnimationManager.current(p).phase().sustained(),
                    "Thelemic selection reopened the previous Noetic charge pose");
            h.succeed();
        } finally { cleanupSharedSelection(p); }
    }

    @GameTest(template = "empty") public static void thelemicSelectionCancelsAnActiveNoeticCharge(GameTestHelper h) {
        var p = player(h, 5);
        try {
            var mortar = ManipulationInit.hematic_mortar.get();
            prepareSharedSelection(p, mortar);
            CastingAnimationManager.charge(p, mortar.getName(), 4);
            h.assertTrue(CastingAnimationManager.current(p) != null, "Active charge fixture did not start");
            HemoCapabilityAccess.requireKnownManipulations(p)
                    .setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
            CastingAnimationManager.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));
            h.assertTrue(!CastingAnimationManager.current(p).phase().sustained(),
                    "Switching to a Thelemic memory retained an active Noetic charge pose");
            h.succeed();
        } finally { cleanupSharedSelection(p); }
    }

    @GameTest(template = "empty") public static void sharedSelectionPreservesChargePreviewCleanup(GameTestHelper h) {
        ManipulationChargeVisualGameTests.verify(h.getLevel(), h.absolutePos(new BlockPos(0, 3, 0)).getCenter());
        h.succeed();
    }

    @GameTest(template = "empty") public static void thelemicUseTogglesWithoutCastingThePreviousNoetic(GameTestHelper h) {
        for (var noetic : List.of(ManipulationInit.sanguine_ward.get(), ManipulationInit.hematic_mortar.get())) {
            var p = player(h, 5);
            try {
                prepareSharedSelection(p, noetic);
                HemoCapabilityAccess.requireKnownManipulations(p)
                        .setSelectedMemoryRef(MemorySlotRef.muscleMemory(MuscleMemory.LABORING_ARMS));
                var context = new net.neoforged.neoforge.network.handling.ServerPayloadContext(
                        p.connection, UseManipKeyPacket.TYPE.id());
                var state = p.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
                UseManipKeyPacket.handle(new UseManipKeyPacket(), context);
                h.assertTrue(state.isEnabled(MuscleMemory.LABORING_ARMS), "Use did not activate the selected Thelemic memory");
                UseManipKeyPacket.handle(new UseManipKeyPacket(), context);
                h.assertTrue(!state.isEnabled(MuscleMemory.LABORING_ARMS), "Second use did not deactivate the memory");
                h.assertTrue(state.reserveTicks(MuscleMemory.LABORING_ARMS) == 6000,
                        "Selection toggle spent reserve without a running tick");
                h.assertTrue(HemoCapabilityAccess.requireBloodVolume(p).getBloodVolume() == 2000,
                        "Thelemic use cast the previous Noetic");
            } finally { cleanupSharedSelection(p); }
        }
        h.succeed();
    }
    private static ItemStack vial(String source) {
        var stack = new ItemStack(ItemInit.bloody_vial.get());
        var tag = new CompoundTag();
        tag.putString(BloodVialItem.TAG_ENTITY_TYPE, source);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
    private static void examine(GameTestHelper h, ServerPlayer p, ItemStack vial) {
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        p.setItemInHand(InteractionHand.OFF_HAND, vial);
        p.getMainHandItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 40; i++) p.doTick();
        h.assertTrue(BloodSampleData.identified(vial), "Examination did not identify the sample");
        p.releaseUsingItem();
    }

    @GameTest(template = "empty") public static void degreeTwoBriefingAndTeachingValidateOwnerAndEvidence(GameTestHelper h) {
        var p = player(h, 2);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel());
        alchemist.setPos(p.position()); artificer.setPos(p.position());
        h.assertTrue(FirstSeparationAssignment.canBrief(p), "First Separation is still locked behind Degree 2");
        var dialogue = alchemist.progressionDialogue(p);
        h.assertTrue(dialogue.getStartNode().options().stream()
                .anyMatch(o -> "first_separation_offer".equals(o.nextNodeId())),
                "D2 briefing offer is unreachable");
        var offer = dialogue.getNode("first_separation_offer");
        h.assertTrue(offer != null && offer.lines().size() == 2,
                "The First Separation offer must explain the assignment before acceptance");
        h.assertTrue(offer.options().stream().anyMatch(o ->
                        HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF.equals(o.eventId())
                                && "first_separation_briefing".equals(o.nextNodeId())),
                "The explained offer does not retain the briefing action");
        var decorated = DialogueHubFactory.decorate(dialogue, "alchemist", p);
        h.assertTrue(decorated.presentation().topics(DialogueCategory.QUESTS).stream()
                        .anyMatch(topic -> "first_separation_offer".equals(topic.targetNodeId())),
                "The dialogue hub does not open the authored First Separation offer");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Empty-handed microscope lesson accepted");
        var firstVial = new ItemStack(ItemInit.bloody_vial.get());
        p.setItemInHand(InteractionHand.OFF_HAND, firstVial);
        ItemInit.bloody_vial.get().onLeftClickEntity(firstVial, p, net.minecraft.world.entity.EntityType.PIG.create(h.getLevel()));
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).collected, "Successful sampling did not immediately record collection");
        ClinicalBloodKnowledge.met(p, "alchemist");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, artificer, MICROSCOPE), "Wrong teacher unlocked microscopy");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Alchemist did not teach held sample");
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("hematic_microscope")), "Microscope recipe not awarded");
        h.assertTrue(LiberKnowledgeHelper.hasEntry(p, ClinicalBloodKnowledge.entry("microscope")), "Liber lesson missing");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, MICROSCOPE), "Repeat lesson rewarded twice");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, INJECTION), "Collection alone unlocked injection");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyCompletesMissingFirstBloodcraftProofsBeforeVicar(GameTestHelper h) {
        var actor = player(h, 1);
        setServerPlayerLookup(actor, true);
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var proofStage = java.util.Arrays.stream(HemoJourneyStage.values())
                .filter(stage -> "first_bloodcraft_proofs".equals(stage.id())).findFirst();
        try {
            h.assertTrue(proofStage.isPresent()
                            && proofStage.get().ordinal() < HemoJourneyStage.VICAR_REWARD.ordinal(),
                    "The journey skips Blood Absorption and Venous Stone before the Vicar return");
            FirstBloodcraftAssignment.recordFormation(actor);
            FirstBloodcraftAssignment.recordStructure(actor, new ItemStack(ItemInit.liber_sanguinum.get()));
            var blood = HemoCapabilityAccess.requireBloodVolume(actor);
            blood.setActive(true);
            blood.setBloodVolume(4600.0D);
            HemoJourneyFixtures.prepare(actor, proofStage.orElseThrow(), origin);
            HarbingerJourneyAutomation.perform(actor, proofStage.orElseThrow().id(), origin);
            var progress = FirstBloodcraftAssignment.progress(actor);
            h.assertTrue(progress.absorbedMl() >= 500.0D && progress.venousStoneProjected()
                            && progress.readyForVicar(),
                    "The proof checkpoint did not earn absorption and Stone infusion through gameplay paths: " + progress);
            HemoJourneyFixtures.cleanup(actor, origin);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.VICAR_REWARD, origin);
            actor.setPos(origin.getX() + 1.5D, origin.getY() + 1.0D, origin.getZ() + 0.5D);
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.VICAR_REWARD.id(), origin);
            h.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(actor).getDegreeNumber() == 2
                            && FirstBloodcraftAssignment.isClaimed(actor),
                    "The Vicar still refused a player with all four independent proofs: degree="
                            + HemoCapabilityAccess.requireInitiatoryDegree(actor).getDegreeNumber()
                            + ", claimed=" + FirstBloodcraftAssignment.isClaimed(actor)
                            + ", check=" + HemoJourneyChecks.verify(actor, HemoJourneyStage.VICAR_REWARD, origin));
            h.assertTrue(HemoJourneyFixtures.captureExpectedOutputs(actor, HemoJourneyStage.VICAR_REWARD, origin),
                    "The Vicar reward kit was not attributable after the scrap pickup");
            HemoJourneyFixtures.cleanupOwnedOutputs(actor, origin);
            h.assertTrue(actor.getInventory().items.stream().anyMatch(stack ->
                            stack.is(ItemInit.hematic_iron_scrap.get()) && stack.getCount() >= 4),
                    "The Vicar's Hematic Iron Scrap did not survive fixture output cleanup");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            setServerPlayerLookup(actor, false);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void unboundPomesAreFoodButNotCommunionProof(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        try {
            for (int i = 0; i < 9; i++) {
                ItemStack pome = new ItemStack(ItemInit.qliphoth_pome.get());
                pome.getItem().finishUsingItem(pome, h.getLevel(), actor);
            }
            h.assertTrue(actor.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),
                    "Unbound pomes should still provide their ordinary effects");
            h.assertTrue(degree.getTotalPomesConsumed() == 0,
                    "Unbound pomes must not count as nine fruits from one owned Bloom");
            h.assertTrue(!degree.isQliphothCommunionDone() && !degree.hasFungalSpineGranted(),
                    "Unbound pomes must not award Communion or the Fungal Spine");
        } finally {
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ownedBoundPomesStillCompleteCommunion(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        var bloom = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                h.absolutePos(new BlockPos(2, 2, 2)), h.getLevel().dimension().location().toString(), 3,
                h.getLevel().getGameTime());
        blooms.addBloom(bloom);
        try {
            for (int husk = 0; husk < 9; husk++) {
                ItemStack pome = QliphothPomeItem.createPickedPomeStack(bloom, husk);
                pome.getItem().finishUsingItem(pome, h.getLevel(), actor);
            }
            h.assertTrue(degree.getTotalPomesConsumed() == 9 && degree.isQliphothCommunionDone()
                            && degree.hasFungalSpineGranted(),
                    "Nine sequential bound pomes from one Bloom must complete Communion");
        } finally {
            blooms.removeBloomInChunk(bloom.center(), bloom.dimension());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void fullInventoryKeepsFungalSpineClaimPending(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        var bloom = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                h.absolutePos(new BlockPos(2, 2, 2)), h.getLevel().dimension().location().toString(), 3,
                h.getLevel().getGameTime());
        blooms.addBloom(bloom);
        var nearby = new AABB(actor.blockPosition()).inflate(4);
        try {
            for (int husk = 0; husk < 8; husk++) {
                ItemStack pome = QliphothPomeItem.createPickedPomeStack(bloom, husk);
                pome.getItem().finishUsingItem(pome, h.getLevel(), actor);
            }
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++) {
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            }
            ItemStack last = QliphothPomeItem.createPickedPomeStack(bloom, 8);
            last.getItem().finishUsingItem(last, h.getLevel(), actor);
            h.assertTrue(degree.isQliphothCommunionDone() && !degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 0
                            && h.getLevel().getEntitiesOfClass(ItemEntity.class, nearby,
                                    item -> item.getItem().is(ItemInit.fungal_spine.get())).isEmpty(),
                    "A full inventory must keep the Fungal Spine claim pending instead of dropping its only copy");
            var persisted = new com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree();
            persisted.deserializeNBT(actor.registryAccess(), actor.getData(
                    com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.INITIATORY_DEGREE)
                    .serializeNBT(actor.registryAccess()));
            h.assertTrue(persisted.isQliphothCommunionDone() && !persisted.hasFungalSpineGranted(),
                    "The pending Spine claim did not survive degree serialization");
            InitiatoryDegreeEvents.playerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(actor));
            h.assertTrue(!degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "Login must not mark a full-inventory Spine claim as delivered");
            actor.getInventory().setItem(0, ItemStack.EMPTY);
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Making room did not deliver the pending Fungal Spine");
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Spine delivery retried after its one-time claim was recorded");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, nearby,
                    item -> item.getItem().is(ItemInit.fungal_spine.get())).forEach(ItemEntity::discard);
            blooms.removeBloomInChunk(bloom.center(), bloom.dimension());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void qliphothAtlasTeachingFollowsArchonRevelation(GameTestHelper h) {
        var actor = player(h, 5);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var entries = com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialsData.getBloodEntries();
        try {
            for (String id : new String[] {"qliphoth_seed", "qliphoth_bloom", "qliphoth_pome", "fungal_spine", "memory_of_vesper"}) {
                var entry = entries.stream().filter(e -> e.name().equals(id)).findFirst().orElseThrow();
                h.assertTrue(!entry.iconStack().get().isEmpty(),
                        "Qliphoth teaching must have a nonempty process/material icon: " + id);
                var gate = com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialAtlasSpec.entryFor(
                        com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialAtlasPath.HARBINGER, entry).gate();
                degree.setDegreeNumber(5);
                h.assertTrue(!entry.unlockPredicate().isUnlocked(actor)
                                && gate.visibilityFor(com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialAtlasPath.HARBINGER,
                                        5, 0, 0) == com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialVisibility.HIDDEN,
                        "Lower-degree teaching exposed the Archon material " + id);
                h.assertTrue(gate.visibilityFor(com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialAtlasPath.HARBINGER,
                                6, 0, 0) == com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialVisibility.NEXT_PREVIEW,
                        "D6 should retain the atlas's veiled next-degree preview for " + id);
                degree.setDegreeNumber(7);
                h.assertTrue(entry.unlockPredicate().isUnlocked(actor) == !id.equals("memory_of_vesper"),
                        "Undecided D7 teaching must expose Communion, not Vesper reward details: " + id);
            }
            var memory = entries.stream().filter(e -> e.name().equals("memory_of_vesper")).findFirst().orElseThrow();
            var bloom = entries.stream().filter(e -> e.name().equals("qliphoth_bloom")).findFirst().orElseThrow();
            h.assertTrue(bloom.iconStack().get().is(ItemInit.qliphoth_pome.get()) && !bloom.hasRecipe(),
                    "Itemless Bloom process must use its Pome icon without advertising a crafting recipe");
            degree.setFungalRevelationWitnessed(true);
            h.assertTrue(memory.unlockPredicate().isUnlocked(actor),
                    "Returned revelation must make Vesper Memory teaching available at D7");
            degree.setDegreeNumber(8);
            h.assertTrue(memory.unlockPredicate().isUnlocked(actor),
                    "Apotheos must retain knowledge of the other branch without receiving its reward");
        } finally { actor.discard(); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void bloomRiteIconUsesItsSeedWithoutCreatingABlockItem(GameTestHelper h) {
        h.assertTrue(BlockInit.qliphoth_bloom.get().asItem() == Items.AIR,
                "Bloom must remain itemless; its rite is the acquisition route");
        var icon = com.vincenthuto.hemomancy.client.screen.skilltree.shared.HarbingerRecipeMapDefinitions.riteIcon(
                "cardinal_rite/bloom_of_qliphoth", ItemStack.EMPTY);
        h.assertTrue(icon.is(ItemInit.qliphoth_seed.get()),
                "Bloom rite must display its Seed medium, not an empty block item");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeFullInventoryKeepsFungalSpineClaimPending(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        try {
            degree.setQliphothCommunionDone(true);
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++)
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            actor.setGameMode(GameType.CREATIVE);
            InitiatoryDegreeEvents.playerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(actor));
            actor.tickCount = 20;
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(!degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "Creative full-inventory discard must not mark the first Spine delivered");
            actor.getInventory().setItem(0, ItemStack.EMPTY);
            actor.tickCount = 20;
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Actual Creative inventory space must deliver the pending Spine exactly once");
        } finally { actor.discard(); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void deadPlayerKeepsFungalSpineClaimForRespawn(GameTestHelper h) {
        var actor = player(h, 7);
        var restored = player(h, 7, h.getLevel(), packet -> {}, actor.getGameProfile());
        var respawned = player(h, 7, h.getLevel(), packet -> {}, actor.getGameProfile());
        try {
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setQliphothCommunionDone(true);
            restored.load(actor.saveWithoutId(new CompoundTag()));
            restored.setHealth(0);
            restored.tickCount = 20;
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(restored));
            h.assertTrue(!HemoCapabilityAccess.requireInitiatoryDegree(restored).hasFungalSpineGranted()
                            && restored.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "Dead-player retries must not consume the pending first Spine before respawn");
            respawned.restoreFrom(restored, false);
            h.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(respawned).isQliphothCommunionDone()
                            && !HemoCapabilityAccess.requireInitiatoryDegree(respawned).hasFungalSpineGranted(),
                    "Serialization and death cloning must preserve the pending Communion Spine");
            InitiatoryDegreeEvents.playerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(respawned));
            respawned.tickCount = 20;
            QliphothBloomEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(respawned));
            h.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(respawned).hasFungalSpineGranted()
                            && respawned.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Respawn must receive the earned first Spine exactly once");
        } finally { respawned.discard(); restored.discard(); actor.discard(); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void existingFungalSpineFulfillsLegacyClaimWithoutDuplicate(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        try {
            degree.setQliphothCommunionDone(true);
            actor.getInventory().setItem(0, new ItemStack(ItemInit.fungal_spine.get()));
            InitiatoryDegreeEvents.playerLoggedIn(new PlayerEvent.PlayerLoggedInEvent(actor));
            QliphothBloomEvents.deliverPendingFungalSpine(actor);
            h.assertTrue(degree.hasFungalSpineGranted()
                            && actor.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "A legacy Spine in inventory must fulfill the saved claim without another copy");
        } finally {
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyCommunionUsesAndCleansUpOneBloomLifecycle(GameTestHelper h) {
        var actor = player(h, 7);
        var center = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, center);
            var bloom = blooms.getBloomAt(center.above(), dimension);
            h.assertTrue(bloom != null && bloom.center().equals(center.above())
                            && bloom.ownerUUID().equals(actor.getUUID()),
                    "Journey Communion must prepare a real owned Bloom");
            h.assertTrue(h.getLevel().getBlockState(center.above()).is(BlockInit.qliphoth_bloom.get()),
                    "Journey Bloom must exist in the test world");
            h.assertTrue(!QliphothPomeItem.isBoundPomeFromBloom(
                            QliphothPomeItem.createPickedPomeStack(bloom.center().asLong(), 0, actor.getUUID()), bloom),
                    "Coordinate-only legacy fruit must not prove the new journey Bloom identity");
            h.assertTrue(actor.getInventory().countItem(ItemInit.qliphoth_pome.get()) == 0,
                    "Journey Communion must not supply fruit before the Bloom ripens");
            blooms.incrementPomesDropped(bloom);
            blooms.setPendingPome(bloom, 0);
            var hit = new BlockHitResult(Vec3.atCenterOf(bloom.center()), Direction.UP, bloom.center(), false);
            h.getLevel().getBlockState(bloom.center()).useWithoutItem(h.getLevel(), actor, hit);
            h.assertTrue(blooms.isPendingPomeClaimed(bloom)
                            && QliphothPomeItem.isBoundPomeFromBloom(actor.getInventory().getItem(0), bloom),
                    "The interrupted journey must begin with its first pome already picked");
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), center);
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
            h.assertTrue(degree.isQliphothCommunionDone()
                            && degree.getPomesConsumedFromBloom(bloom.bloomId(), bloom.center().asLong()) == 9
                            && blooms.getPomesDropped(bloom) == 9 && !blooms.hasPendingPome(bloom),
                    "Journey automation must pick and consume nine sequential fruit from its Bloom");
            HemoJourneyFixtures.cleanup(actor, center);
            h.assertTrue(blooms.getBloomById(bloom.bloomId()) == null
                            && h.getLevel().getBlockState(center.above()).isAir()
                            && h.getLevel().getBlockState(center.above(8)).isAir(),
                    "Journey fixture cleanup must remove its Bloom and saved lifecycle");
        } finally {
            HemoJourneyFixtures.cleanup(actor, center);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ownerPicksAndConsumesNineRipePomesThroughBloom(GameTestHelper h) {
        var owner = player(h, 7);
        var stranger = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = h.getLevel().dimension().location().toString();
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var root = origin.above();
            var bloom = blooms.getBloomAt(root, dimension);
            h.assertTrue(bloom != null && bloom.center().equals(root), "Ripe-pome test needs a real Bloom");
            var hit = new BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false);
            for (int husk = 0; husk < 9; husk++) {
                blooms.incrementPomesDropped(bloom);
                blooms.setPendingPome(bloom, husk);
                h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), stranger, hit);
                h.assertTrue(stranger.getInventory().countItem(ItemInit.qliphoth_pome.get()) == 0
                                && !blooms.isPendingPomeClaimed(bloom),
                        "A stranger claimed the owner's ripe pome");
                h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), owner, hit);
                ItemStack pome = owner.getInventory().getItem(0);
                h.assertTrue(QliphothPomeItem.isBoundPomeFromBloom(pome, bloom)
                                && blooms.isPendingPomeClaimed(bloom),
                        "The Bloom did not give its owner the current bound pome");
                h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), owner, hit);
                h.assertTrue(owner.getInventory().countItem(ItemInit.qliphoth_pome.get()) == 1,
                        "A claimed husk could be picked twice");
                pome.getItem().finishUsingItem(pome, h.getLevel(), owner);
                h.assertTrue(!blooms.hasPendingPome(bloom)
                                && HemoCapabilityAccess.requireInitiatoryDegree(owner)
                                        .getPomesConsumedFromBloom(bloom.bloomId(), root.asLong()) == husk + 1,
                        "Eating the picked husk did not release the tree for the next fruit");
            }
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
            h.assertTrue(degree.isQliphothCommunionDone() && degree.hasFungalSpineGranted()
                            && owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Nine picked and eaten pomes did not complete Communion");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
            stranger.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void ownerReclaimsLostFungalSpineAtCompletedBloom(GameTestHelper h) {
        var owner = player(h, 7);
        var stranger = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var root = origin.above();
            var bloom = blooms.getBloomAt(root, h.getLevel().dimension().location().toString());
            HarbingerJourneyAutomation.perform(owner, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), origin);
            var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
            h.assertTrue(bloom != null && degree.isQliphothCommunionDone()
                            && degree.hasFungalSpineGranted()
                            && owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "Recovery test needs a completed owner-bound Bloom and earned Spine");
            owner.getInventory().clearContent();
            var hit = new BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false);
            owner.setPos(root.getCenter());
            stranger.setPos(root.getCenter());
            stranger.setShiftKeyDown(true);
            stranger.gameMode.useItemOn(stranger, h.getLevel(), stranger.getMainHandItem(),
                    InteractionHand.MAIN_HAND, hit);
            h.assertTrue(stranger.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "A stranger reclaimed the owner's Fungal Spine");
            owner.setShiftKeyDown(true);
            owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "Holding an unrelated item should not trigger empty-hand Spine recovery");
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "The Communion owner could not reclaim a lost Spine at the living Bloom");
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 1,
                    "The Bloom issued a duplicate Spine while the owner carried one");
            owner.getInventory().clearContent();
            blooms.removeBloomInChunk(root, bloom.dimension());
            var replacement = new QliphothBloomSavedData.BloomEntry(owner.getUUID(), root,
                    bloom.dimension(), 3, h.getLevel().getGameTime());
            blooms.addBloom(replacement);
            for (int i = 0; i < 9; i++) blooms.incrementPomesDropped(replacement);
            owner.gameMode.useItemOn(owner, h.getLevel(), owner.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            h.assertTrue(owner.getInventory().countItem(ItemInit.fungal_spine.get()) == 0,
                    "A new Bloom at the old coordinates inherited the spent tree's Spine claim");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
            stranger.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 80)
    public static void apotheosJourneyRunsInFocusedServer(GameTestHelper h) {
        HarbingerJourneyFixtureGameTests.apotheosJourneyUsesRealCommunionChoiceAndRite(h);
    }

    @GameTest(template = "empty")
    public static void journeyRestoresNumericFungalProjectionState(GameTestHelper h) {
        var actor = player(h, 7);
        var persistent = actor.getPersistentData();
        persistent.putBoolean(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_ACTIVE, true);
        persistent.putInt(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_REMAINING, 77);
        persistent.putDouble(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_X, 123.5D);
        persistent.putFloat(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_YROT, 91.0F);
        try {
            h.assertTrue(HemoJourneySnapshot.capture(actor).passed(), "Fungal projection snapshot capture failed");
            h.assertTrue(HemoJourneySnapshot.resetForJourney(actor).passed(),
                    "Journey reset rejected numeric Fungal projection fields");
            h.assertTrue(!persistent.contains(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_ACTIVE)
                            && !persistent.contains(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_X),
                    "Journey reset left the prior Fungal projection active");
            var restored = HemoJourneySnapshot.restore(actor);
            h.assertTrue(restored.passed(), "Fungal projection snapshot restore failed: " + restored.message());
            h.assertTrue(persistent.getBoolean(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_ACTIVE)
                            && persistent.getInt(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.PROJECTION_REMAINING) == 77
                            && persistent.getDouble(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_X) == 123.5D
                            && persistent.getFloat(com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.RETURN_YROT) == 91.0F,
                    "Journey restore changed the preexisting Fungal projection");
        } finally {
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyAutomationUsesSpineBeforeApotheosChoice(GameTestHelper h) {
        var actor = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                h.getLevel().dimension().location().toString());
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), origin);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin);
            h.assertTrue(!actor.getPersistentData().getBoolean(
                            com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.REVELATION_CHOICE_PENDING),
                    "Automation fixture offered the ending before projection");
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.APOTHEOS_CHOICE.id(), origin);
            h.assertTrue(HemoJourneyChecks.verify(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin).passed(),
                    "Journey automation did not use the Spine, return, and choose Apotheos");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void journeyAcceptsSilentChoiceAfterRealProjection(GameTestHelper h) {
        var actor = player(h, 7);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            h.assertTrue(HemoJourneySnapshot.capture(actor).passed(), "Silent journey snapshot capture failed");
            actor.getPersistentData().putLong(HemoJourneyFixtures.ORIGIN_KEY, origin.asLong());
            actor.getPersistentData().putString(JourneyRoute.KEY, JourneyRoute.HARBINGER);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var bloom = blooms.getBloomAt(origin.above(), dimension);
            HarbingerJourneyAutomation.perform(actor, HemoJourneyStage.QLIPHOTH_COMMUNION.id(), origin);
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin);
            actor.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY, HemoJourneyStage.APOTHEOS_CHOICE.id());
            actor.getMainHandItem().use(h.getLevel(), actor, InteractionHand.MAIN_HAND);
            com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.performForcedProjectionReturn(actor);
            DialogueEventHandler.onDialogueOption(new DialogueEvent(actor, "archon_choice_silence", 0));
            h.assertTrue(HemoJourneyChecks.verify(actor, HemoJourneyStage.APOTHEOS_CHOICE, origin).passed()
                            && blooms.getBloomById(bloom.bloomId()) != null,
                    "A real Spine return and Silent choice must pass without losing the Communion Bloom");
            for (int slot = 1; slot < actor.getInventory().getContainerSize(); slot++) {
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            }
            var failedTransition = HemoJourneyController.next(actor);
            h.assertTrue(!failedTransition.passed() && blooms.getBloomById(bloom.bloomId()) != null
                            && h.getLevel().getBlockState(bloom.center()).is(BlockInit.qliphoth_bloom.get()),
                    "A full-inventory transition must leave the verified choice and Bloom retryable");
            actor.getInventory().setItem(1, ItemStack.EMPTY);
            var next = HemoJourneyController.next(actor);
            h.assertTrue(next.passed() && next.stage() == HemoJourneyStage.SILENT_REFUSAL
                            && blooms.getBloomById(bloom.bloomId()) != null
                            && h.getLevel().getBlockState(bloom.center()).is(BlockInit.qliphoth_bloom.get()),
                    "The Silent branch must retain its owned Bloom for the refusal ordeal: " + next.message());
            var known = HemoCapabilityAccess.requireKnownManipulations(actor);
            h.assertTrue(known.isManipulationAvailable(ManipulationInit.conjure_blade.get())
                            && known.getEquippedManipNames().contains(ManipulationInit.conjure_blade.get().getName()),
                    "The refusal fixture must leave a selectable Living Arsenal weapon form");
            h.assertTrue(!HemoJourneyChecks.verify(actor, HemoJourneyStage.SILENT_REFUSAL, origin).passed(),
                    "The open refusal stage must not pass before Vesper victory");
            blooms.severBloom(bloom);
            blooms.sealBloom(bloom);
            HemoCapabilityAccess.requireInitiatoryDegree(actor).setArchonPath(
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_ARCHON);
            HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_VESPER_DEFEATED);
            actor.getPersistentData().putBoolean("hemomancy:vesper_memory_pending", true);
            h.assertTrue(HemoJourneyChecks.verify(actor, HemoJourneyStage.SILENT_REFUSAL, origin).passed(),
                    "The sealed owned Bloom and verified reward state must finish the Silent checkpoint");
            h.assertTrue(HemoJourneyController.next(actor).stage() == HemoJourneyStage.COMPLETE,
                    "The Silent checkpoint did not reach journey completion");
            h.assertTrue(HemoJourneyController.next(actor).passed()
                            && !HarbingerAdvancementGranter.hasAdvancement(actor,
                                    HarbingerAdvancementGranter.ADV_VESPER_DEFEATED)
                            && !actor.getPersistentData().contains("hemomancy:vesper_memory_pending")
                            && HemoCapabilityAccess.requireInitiatoryDegree(actor).getArchonPath()
                                    == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.NONE
                            && blooms.getBloomById(bloom.bloomId()) == null,
                    "Journey restore kept a temporary Silent victory or its fixture Bloom");
        } finally {
            HemoJourneyController.clear(actor);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void oldSickleCannotSeverReplacementBloom(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        degree.setQliphothCommunionDone(true);
        degree.syncTotalPomesConsumed(9);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var original = blooms.getBloomAt(origin.above(), dimension);
            h.assertTrue(original != null, "First Bloom was not registered");
            actor.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            h.assertTrue(LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND)
                            && LivingSicklePruning.isTemporarySickle(actor.getMainHandItem()),
                    "The owned Bloom did not shape a temporary Sickle");
            ItemStack oldSickle = actor.getMainHandItem().copy();

            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var replacement = blooms.getBloomAt(origin.above(), dimension);
            h.assertTrue(replacement != null && !replacement.bloomId().equals(original.bloomId()),
                    "The replacement Bloom did not start a new lifecycle");
            actor.setItemInHand(InteractionHand.MAIN_HAND, oldSickle);
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            h.assertTrue(blooms.getState(replacement) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.LIVING
                            && LivingSicklePruning.isTemporarySickle(actor.getMainHandItem()),
                    "A Sickle from the removed Bloom severed its replacement");

            actor.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            h.assertTrue(blooms.getState(replacement) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN,
                    "A new Sickle could not open its own Bloom for the refusal ordeal");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void legacySickleCanSeverMigratedBloom(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        degree.setQliphothCommunionDone(true);
        degree.syncTotalPomesConsumed(9);
        var origin = HemoJourneyFixtures.findClearOrigin(actor);
        var dimension = h.getLevel().dimension().location().toString();
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            HemoJourneyFixtures.prepare(actor, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            blooms.removeBloomInChunk(origin.above(), dimension);
            var migrated = new QliphothBloomSavedData.BloomEntry(actor.getUUID(), origin.above(),
                    dimension, 3, h.getLevel().getGameTime(), UUID.randomUUID(), true);
            blooms.addBloom(migrated);
            actor.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            ItemStack oldSickle = actor.getMainHandItem();
            h.assertTrue(LivingSicklePruning.isTemporarySickle(oldSickle), "Legacy test could not shape a Sickle");
            CompoundTag tag = oldSickle.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            tag.remove(LivingSicklePruning.BLOOM_ID_KEY);
            oldSickle.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            LivingSicklePruning.interact(h.getLevel(), origin.above(), actor, InteractionHand.MAIN_HAND);
            h.assertTrue(blooms.getState(migrated) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN,
                    "A pre-ID Sickle could not finish cutting its migrated Bloom");
        } finally {
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void refusalPortalRemainsRetryableWithoutChamber(GameTestHelper h) {
        var owner = player(h, 7);
        var stranger = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        degree.setQliphothCommunionDone(true);
        degree.syncTotalPomesConsumed(9);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = h.getLevel().dimension().location().toString();
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var root = origin.above();
            var bloom = blooms.getBloomAt(root, dimension);
            h.assertTrue(bloom != null, "Refusal test needs a registered Bloom");
            var hit = new BlockHitResult(Vec3.atCenterOf(root), Direction.UP, root, false);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            h.getLevel().getBlockState(root).useItemOn(stranger.getMainHandItem(), h.getLevel(),
                    stranger, InteractionHand.MAIN_HAND, hit);
            h.assertTrue(!LivingSicklePruning.isTemporarySickle(stranger.getMainHandItem())
                            && blooms.getState(bloom) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.LIVING,
                    "A stranger shaped a Sickle or opened the owner's Bloom");

            owner.setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
            h.getLevel().getBlockState(root).useItemOn(owner.getMainHandItem(), h.getLevel(),
                    owner, InteractionHand.MAIN_HAND, hit);
            h.getLevel().getBlockState(root).useItemOn(owner.getMainHandItem(), h.getLevel(),
                    owner, InteractionHand.MAIN_HAND, hit);
            h.assertTrue(blooms.getState(bloom) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN
                            && degree.getArchonPath() == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING,
                    "Opening the refusal portal prematurely awarded Silent Archon");
			ItemStack restoredWeapon = owner.getMainHandItem().copy();
			h.assertTrue(!LivingSicklePruning.interact(h.getLevel(), root, owner, InteractionHand.MAIN_HAND),
					"Pruning must yield an open wound to portal entry while the restored weapon is held");
			h.assertTrue(ItemStack.matches(restoredWeapon, owner.getMainHandItem()),
					"Using an open wound must not reshape or lose the restored weapon");
            h.getLevel().getBlockState(root).useItemOn(owner.getMainHandItem(), h.getLevel(),
                    owner, InteractionHand.MAIN_HAND, hit);
            h.assertTrue(h.getLevel().getServer().getLevel(
                            com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager.CHAMBER_OF_WILL) == null,
                    "This focused test expects the GameTest server's absent Chamber dimension");
            owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), stranger, hit);
            h.getLevel().getBlockState(root).useWithoutItem(h.getLevel(), owner, hit);
            h.assertTrue(blooms.getState(bloom) == com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState.OPEN
                            && degree.getArchonPath() == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING
                            && !VesperOrdealManager.isActive(owner),
                    "A denied arena transfer consumed the refusal portal or granted its victory");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
            stranger.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void bloomTickRipensOneHuskAndWaitsForConsumption(GameTestHelper h) {
        var owner = player(h, 7);
        var level = h.getLevel();
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = level.dimension().location().toString();
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        int delay = 40 - (int) (level.getGameTime() % 40);
        h.runAfterDelay(delay, () -> {
            try {
                HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
                var blooms = QliphothBloomSavedData.get(level.getServer().overworld());
                var bloom = blooms.getBloomAt(origin.above(), dimension);
                h.assertTrue(bloom != null && level.getGameTime() % 40 == 0,
                        "Ripening test did not reach a Bloom effect tick");
                long seed = 0L;
                while (seed < 10000L) {
                    level.getRandom().setSeed(seed);
                    if (level.getRandom().nextInt(80) == 0) break;
                    seed++;
                }
                h.assertTrue(seed < 10000L, "No deterministic ripe-pome roll was found");
                level.getRandom().setSeed(seed);
                var tick = new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(() -> true, level);
                QliphothBloomEvents.onLevelTick(tick);
                h.assertTrue(blooms.getPomesDropped(bloom) == 1
                                && blooms.getPendingPomeHuskIndex(bloom) == 0,
                        "Bloom world tick did not ripen its first husk");
                level.getRandom().setSeed(seed);
                QliphothBloomEvents.onLevelTick(tick);
                h.assertTrue(blooms.getPomesDropped(bloom) == 1,
                        "Bloom ripened another husk while the first was pending");
                blooms.clearPendingPome(bloom);
                level.getRandom().setSeed(seed);
                QliphothBloomEvents.onLevelTick(tick);
                h.assertTrue(blooms.getPomesDropped(bloom) == 2
                                && blooms.getPendingPomeHuskIndex(bloom) == 1,
                        "Bloom did not resume with the second husk after the pending fruit cleared");
                h.succeed();
            } finally {
                HemoJourneyFixtures.cleanup(owner, origin);
                owner.discard();
            }
        });
    }

    private static ItemStack livingBlade() {
        ItemStack blade = new ItemStack(ItemInit.living_blade.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(LivingStaffWeaponFormHelper.FORM_KEY, LivingStaffWeaponFormRules.CONJURE_BLADE);
        blade.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return blade;
    }

    @GameTest(template = "empty")
    public static void replacementBloomDoesNotInheritPickedPomeProgress(GameTestHelper h) {
        var actor = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(actor);
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        var original = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                h.absolutePos(new BlockPos(2, 2, 2)), h.getLevel().dimension().location().toString(), 3,
                h.getLevel().getGameTime());
        blooms.addBloom(original);
        ItemStack oldPome = QliphothPomeItem.createPickedPomeStack(original, 0);
        blooms.removeBloomInChunk(original.center(), original.dimension());
        var replacement = new QliphothBloomSavedData.BloomEntry(actor.getUUID(),
                original.center(), original.dimension(), 3, h.getLevel().getGameTime());
        blooms.addBloom(replacement);
        blooms.setPendingPome(replacement, 0);
        try {
            oldPome.getItem().finishUsingItem(oldPome, h.getLevel(), actor);
            h.assertTrue(blooms.hasPendingPome(replacement),
                    "Eating an old picked pome cleared the replacement Bloom's pending fruit");
            ItemStack newPome = QliphothPomeItem.createPickedPomeStack(replacement, 0);
            newPome.getItem().finishUsingItem(newPome, h.getLevel(), actor);
            h.assertTrue(degree.getPomesConsumedFromBloom(original.bloomId(), original.center().asLong()) == 1
                            && degree.getPomesConsumedFromBloom(replacement.bloomId(), replacement.center().asLong()) == 1,
                    "Pomes from separate lifecycles shared Communion sequence progress");
            h.assertTrue(!blooms.hasPendingPome(replacement),
                    "Eating the new pome did not clear its own pending fruit");
        } finally {
            blooms.removeBloomInChunk(replacement.center(), replacement.dimension());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void vesperOrdealRetainsBloomIdentityAcrossPhasesAndReload(GameTestHelper h) {
        UUID owner = UUID.randomUUID();
        UUID bloomId = UUID.randomUUID();
        long origin = h.absolutePos(new BlockPos(2, 2, 2)).asLong();
        VesperTheCrownedRefusalEntity first = EntityInit.vesper_crowned_refusal.get().create(h.getLevel());
        first.setOrdeal(owner, origin, bloomId);
        VesperTheCrownedRefusalEntity restoredFirst = EntityInit.vesper_crowned_refusal.get().create(h.getLevel());
        restoredFirst.load(first.saveWithoutId(new CompoundTag()));
        h.assertTrue(bloomId.equals(restoredFirst.getBloomId()), "The first phase lost its Bloom ID on reload");

        VesperTheEveningStarEntity second = EntityInit.vesper_evening_star.get().create(h.getLevel());
        VesperOrdealManager.copyOrdeal(restoredFirst, second);
        VesperTheEveningStarEntity restoredSecond = EntityInit.vesper_evening_star.get().create(h.getLevel());
        restoredSecond.load(second.saveWithoutId(new CompoundTag()));
        h.assertTrue(owner.equals(restoredSecond.getOrdealOwner()) && origin == restoredSecond.getBloomOrigin()
                        && bloomId.equals(restoredSecond.getBloomId()),
                "Vesper's second phase did not retain the exact Bloom lifecycle");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void vesperMemoryWaitsForInventorySpace(GameTestHelper h) {
        var owner = player(h, 7);
        var restored = player(h, 7, h.getLevel(), packet -> {}, owner.getGameProfile());
        var respawned = player(h, 7, h.getLevel(), packet -> {}, owner.getGameProfile());
        String pending = "hemomancy:vesper_memory_pending";
        try {
            for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++) {
                owner.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            }
            owner.getPersistentData().putBoolean("hemomancy:vesper_memory_pending", true);
            VesperOrdealManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(owner));
            h.assertTrue(owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && !owner.getPersistentData().contains(pending)
                            && h.getLevel().getEntitiesOfClass(ItemEntity.class,
                                    new AABB(owner.blockPosition()).inflate(4),
                                    item -> item.getItem().is(ItemInit.memory_of_vesper.get())).isEmpty(),
                    "A full inventory must migrate the earned Memory claim into death-persistent data");

            restored.load(owner.saveWithoutId(new CompoundTag()));
            owner.getInventory().setItem(0, ItemStack.EMPTY);
            owner.tickCount = 20;
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(!owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).contains(pending)
                            && owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1,
                    "The pending memory was not delivered after inventory space opened");
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1,
                    "Retrying delivery duplicated the unique Vesper memory");
            restored.getInventory().clearContent();
            restored.setHealth(0);
            restored.tickCount = 20;
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(restored));
            respawned.restoreFrom(restored, false);
            h.assertTrue(respawned.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending),
                    "Save/load and death cloning lost the pending Vesper Memory");
            VesperOrdealManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(respawned));
            VesperOrdealManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(respawned));
            h.assertTrue(!respawned.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).contains(pending)
                            && respawned.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1,
                    "Respawn login must deliver exactly one pending Memory");
        } finally {
            respawned.discard();
            restored.discard();
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void vesperMemoryCreativeFullInventoryRetainsClaim(GameTestHelper h) {
        var owner = player(h, 7);
        String pending = "hemomancy:vesper_memory_pending";
        try {
            for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++)
                owner.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            owner.getPersistentData().putBoolean(pending, true);
            owner.setGameMode(GameType.CREATIVE);
            VesperOrdealManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(owner));
            h.assertTrue((owner.getPersistentData().getBoolean(pending)
                            || owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending))
                            && owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 0,
                    "Creative full-inventory discard erased the earned Vesper Memory claim");
            owner.getInventory().setItem(0, ItemStack.EMPTY);
            owner.tickCount = 20;
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1
                            && !owner.getPersistentData().contains(pending)
                            && !owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).contains(pending),
                    "A Creative player with actual space must receive and clear the earned claim");
        } finally { owner.discard(); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void vesperMemoryLegacyClaimSurvivesDeathBeforeRetry(GameTestHelper h) {
        var owner = player(h, 7);
        var respawned = player(h, 7, h.getLevel(), packet -> {}, owner.getGameProfile());
        String pending = "hemomancy:vesper_memory_pending";
        try {
            owner.getPersistentData().putBoolean(pending, true);
            owner.setHealth(0);
            VesperOrdealManager.onPlayerDeath(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(
                    owner, owner.damageSources().generic()));
            owner.tickCount = 20;
            VesperOrdealManager.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(owner.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 0
                            && owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending),
                    "Death before the first retry must preserve the legacy claim without dead-player delivery");
            respawned.restoreFrom(owner, false);
            VesperOrdealManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(respawned));
            h.assertTrue(respawned.getInventory().countItem(ItemInit.memory_of_vesper.get()) == 1
                            && !respawned.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).contains(pending),
                    "The migrated pre-retry death claim must deliver once after respawn");
        } finally { respawned.discard(); owner.discard(); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void circusObservationRequiresLivingNonSpectator(GameTestHelper h) {
        var actor = player(h, 0);
        var level = h.getLevel();
        var circus = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                .get(com.vincenthuto.hemomancy.Hemomancy.rloc("circus_pavilion"));
        var piece = new net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces.StartPiece(
                net.minecraft.util.RandomSource.create(42), actor.blockPosition().getX(), actor.blockPosition().getZ());
        var center = piece.getBoundingBox().getCenter();
        actor.setPos(center.getX() + .5, center.getY(), center.getZ() + .5);
        var chunk = level.getChunkAt(actor.blockPosition());
        var oldStarts = new java.util.HashMap<>(chunk.getAllStarts());
        var oldReferences = new java.util.HashMap<>(chunk.getAllReferences());
        oldReferences.replaceAll((structure, references) -> new it.unimi.dsi.fastutil.longs.LongOpenHashSet(references));
        try {
            var start = new net.minecraft.world.level.levelgen.structure.StructureStart(circus, chunk.getPos(), 0,
                    new net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer(java.util.List.of(piece)));
            chunk.setStartForStructure(circus, start);
            chunk.addReferenceForStructure(circus, chunk.getPos().toLong());
            h.assertTrue(level.structureManager().getStructureWithPieceAt(actor.blockPosition(),
                    holder -> holder.is(com.vincenthuto.hemomancy.Hemomancy.rloc("circus_pavilion"))).isValid(),
                    "Supplied Circus structure reference must be visible to real observation");
            HemoCapabilityAccess.getBloodVolume(actor).orElseThrow().setActive(false);
            actor.tickCount = com.vincenthuto.hemomancy.common.circus.CircusProgressRules.PASSIVE_POINT_TICKS;
            actor.setGameMode(GameType.SPECTATOR);
            com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.hasDiscovered(actor)
                            && com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress.acclimation(actor) == 0,
                    "Spectator inspection must not earn Circus discovery or passive acclimation");
            actor.setGameMode(GameType.SURVIVAL);
            actor.setHealth(0);
            com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(!com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.hasDiscovered(actor)
                            && com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress.acclimation(actor) == 0,
                    "Dead-player ticks must not earn Circus discovery or passive acclimation");
            actor.setHealth(20);
            for (int interval = 0; interval < 4; interval++) {
                actor.tickCount += 20;
                com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.onPlayerTick(
                        new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            }
            h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.hasDiscovered(actor)
                            && com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress.acclimation(actor) == 1
                            && HemoCapabilityAccess.requireInitiatoryDegree(actor).getDegreeNumber() == 0,
                    "Living D0 inactive-blood discovery must remain valid without rank credit");
            actor.setGameMode(GameType.SPECTATOR);
            com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress.hasDiscovered(actor)
                            && com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress.acclimation(actor) == 1,
                    "Spectator inspection must retain earned discovery without gaining acclimation");
        } finally {
            chunk.setAllStarts(oldStarts);
            chunk.setAllReferences(oldReferences);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void mycophantTendrilWaitsForInventorySpaceAcrossDeath(GameTestHelper h) {
        var owner = player(h, 8);
        var restored = player(h, 8, h.getLevel(), packet -> {}, owner.getGameProfile());
        var respawned = player(h, 8, h.getLevel(), packet -> {}, owner.getGameProfile());
        var manager = ChamberOfWillManager.get(owner.server);
        var boss = EntityInit.mycophant.get().create(h.getLevel());
        var box = new AABB(owner.blockPosition()).inflate(4);
        var oldDrops = h.getLevel().getEntitiesOfClass(ItemEntity.class, box,
                item -> item.getItem().is(ItemInit.mycophant_tendril.get())).stream()
                .map(ItemEntity::getUUID).collect(java.util.stream.Collectors.toSet());
        String pending = "hemomancy:mycophant_tendril_pending";
        setServerPlayerLookup(owner, true);
        manager.rememberReturnPoint(owner);
        try {
            HemoCapabilityAccess.requireInitiatoryDegree(owner).setArchonPath(
                    com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS);
            for (int slot = 0; slot < owner.getInventory().getContainerSize(); slot++)
                owner.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE));
            owner.getPersistentData().putBoolean("hemomancy:mycophant_active", true);
            boss.setEncounterOwner(owner.getUUID());
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.completeVictory(boss);
            h.assertTrue(HemoCapabilityAccess.requireInitiatoryDegree(owner).isMycophantDefeated()
                            && !com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.isActive(owner),
                    "The first victory must finish even while its unique reward cannot fit");
            h.assertTrue(owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && owner.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 0
                            && h.getLevel().getEntitiesOfClass(ItemEntity.class, box,
                                    item -> item.getItem().is(ItemInit.mycophant_tendril.get())
                                            && !oldDrops.contains(item.getUUID())).isEmpty(),
                    "A full inventory must retain the Tendril claim, not drop its only copy");

            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onLogin(
                    new PlayerEvent.PlayerLoggedInEvent(owner));
            owner.tickCount = 20;
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && owner.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 0,
                    "Blocked login/tick retries must retain the claim without awarding a copy");
            owner.setGameMode(GameType.CREATIVE);
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onLogin(
                    new PlayerEvent.PlayerLoggedInEvent(owner));
            h.assertTrue(owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && owner.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 0,
                    "Creative full-inventory discard must not clear an undelivered Tendril claim");
            owner.setGameMode(GameType.SURVIVAL);
            restored.load(owner.saveWithoutId(new CompoundTag()));
            owner.getInventory().setItem(0, ItemStack.EMPTY);
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            h.assertTrue(!owner.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && owner.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 1,
                    "Opening a slot must deliver the earned Tendril on the next retry tick");
            restored.getInventory().clearContent();
            restored.setHealth(0);
            restored.tickCount = 20;
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(restored));
            h.assertTrue(restored.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && restored.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 0,
                    "A dead player's empty inventory must not consume the pending Tendril before respawn");
            respawned.restoreFrom(restored, false);
            h.assertTrue(respawned.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && HemoCapabilityAccess.requireInitiatoryDegree(respawned).isMycophantDefeated(),
                    "Saved-data reload and death cloning lost the earned Tendril claim");
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onLogin(
                    new PlayerEvent.PlayerLoggedInEvent(respawned));
            h.assertTrue(!respawned.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean(pending)
                            && respawned.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 1,
                    "Respawn inventory space must deliver the earned Tendril on login");
            respawned.tickCount = 20;
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onPlayerTick(
                    new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(respawned));
            com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onLogin(
                    new PlayerEvent.PlayerLoggedInEvent(respawned));
            h.assertTrue(respawned.getInventory().countItem(ItemInit.mycophant_tendril.get()) == 1,
                    "Repeated tick/login delivery duplicated the unique Tendril");
        } finally {
            setServerPlayerLookup(owner, false);
            try {
                var field = ChamberOfWillManager.class.getDeclaredField("returnPoints");
                field.setAccessible(true);
                ((java.util.Map<?, ?>) field.get(manager)).remove(owner.getUUID());
                manager.setDirty();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not clean the reward fixture's return point", e);
            }
            h.getLevel().getEntitiesOfClass(ItemEntity.class, box,
                    item -> item.getItem().is(ItemInit.mycophant_tendril.get())
                            && !oldDrops.contains(item.getUUID())).forEach(ItemEntity::discard);
            boss.discard();
            respawned.discard();
            restored.discard();
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void staleVesperAttemptReleasesReplacedBloom(GameTestHelper h) {
        var owner = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(owner);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        var origin = HemoJourneyFixtures.findClearOrigin(owner);
        var dimension = h.getLevel().dimension().location().toString();
        var blooms = QliphothBloomSavedData.get(h.getLevel().getServer().overworld());
        owner.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY, dimension);
        try {
            HemoJourneyFixtures.prepare(owner, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
            var original = blooms.getBloomAt(origin.above(), dimension);
            blooms.severBloom(original);
            owner.getPersistentData().putLong("hemomancy:vesper_ordeal_bloom", original.center().asLong());
            owner.getPersistentData().putUUID("hemomancy:vesper_ordeal_bloom_id", original.bloomId());
            h.assertTrue(VesperOrdealManager.hasValidActiveBloom(owner),
                    "An open owned Bloom should support its active refusal attempt");
            degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS_PENDING);
            h.assertTrue(!VesperOrdealManager.hasValidActiveBloom(owner),
                    "A changed ending choice must not resume a refusal attempt");
            degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
            blooms.sealBloom(original);
            h.assertTrue(!VesperOrdealManager.hasValidActiveBloom(owner),
                    "A sealed source Bloom must not resume Vesper");

            blooms.removeBloomInChunk(original.center(), dimension);
            var replacement = new QliphothBloomSavedData.BloomEntry(owner.getUUID(), original.center(),
                    dimension, 3, h.getLevel().getGameTime());
            blooms.addBloom(replacement);
            blooms.severBloom(replacement);
            h.assertTrue(!VesperOrdealManager.hasValidActiveBloom(owner),
                    "A new Bloom at the same coordinates cannot validate the old attempt");
            VesperOrdealManager.tickArenaPlayer(owner, h.getLevel());
            h.assertTrue(!VesperOrdealManager.isActive(owner)
                            && degree.getArchonPath() == com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING
                            && blooms.getState(replacement).isPortalOpen(),
                    "Stale Vesper recovery must preserve the replacement portal and pending path");
        } finally {
            HemoJourneyFixtures.cleanup(owner, origin);
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void chamberSceneSyncKeepsActiveVesperFloorAtArena(GameTestHelper h) {
        List<PacketSyncVesperFightScene> scenes = new ArrayList<>();
        var owner = player(h, 7, h.getLevel(), packet -> captureVesperScene(packet, scenes));
        var chamber = ChamberOfWillManager.get(h.getLevel().getServer());
        try {
            chamber.setSkyThemeOverride(owner, ChamberOfWillManager.THEME_VESPER_FIGHT);
            h.assertTrue(!scenes.isEmpty() && scenes.getLast().active(),
                    "The selected Vesper preview theme did not send its floor scene");
            BlockPos previewCenter = scenes.getLast().center();

            owner.getPersistentData().putLong("hemomancy:vesper_ordeal_bloom", h.absolutePos(new BlockPos(2, 2, 2)).asLong());
            scenes.clear();
            chamber.syncTo(owner);
            h.assertTrue(!scenes.isEmpty() && scenes.getLast().active()
                            && scenes.getLast().center().getX() == 4096
                            && !scenes.getLast().center().equals(previewCenter),
                    "An active Vesper sync used the preview cell instead of the real fight arena");
            VesperOrdealManager.abandonAttempt(owner);
            scenes.clear();
            chamber.syncTo(owner);
            h.assertTrue(!scenes.isEmpty() && scenes.getLast().center().equals(previewCenter),
                    "Ending the ordeal did not restore the selected Chamber floor preview");
        } finally {
            VesperOrdealManager.abandonAttempt(owner);
            chamber.clearSkyThemeOverride(owner);
            owner.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedBloomRiteKeepsItsSeedForRetry(GameTestHelper h) {
        var actor = player(h, 7);
        var level = h.getLevel();
        var center = h.absolutePos(new BlockPos(6, 3, 6));
        var obstruction = center.above(5);
        var recipeId = Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth");
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe
                .getRiteByLocation(level, recipeId);
        h.assertTrue(recipe != null, "Bloom rite recipe is missing");
        var bloodlines = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(level.getServer().overworld());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Bloom helper", actor.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        bloodlines.registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(actor).setBloodLine(line);
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                actor.getUUID(), center, recipeId, 1200, 7, 7, false, 0);
        var role = com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR;
        var station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(recipe).get(role));
        level.setBlock(station.below(), Blocks.STONE.defaultBlockState(), 3);
        var helper = EntityInit.harbinger_vicar.get().create(level);
        h.assertTrue(helper != null, "Bloom rite helper could not be created");
        helper.setPos(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
        helper.setNoAi(true);
        h.assertTrue(level.addFreshEntity(helper), "Bloom rite helper could not be spawned");
        bloodlines.addNpcMember(line.getBloodlineUUID(), helper.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(helper.getType()));
        rite.assignAlly(helper.getUUID(), role);
        level.setBlock(center, BlockInit.cardinal_focus.get().defaultBlockState(), 3);
        var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity)
                level.getBlockEntity(center);
        h.assertTrue(focus.insertMedium(actor, new ItemStack(ItemInit.qliphoth_seed.get())),
                "The Bloom Seed could not be seated");
        level.setBlock(obstruction, Blocks.OBSIDIAN.defaultBlockState(), 3);
        try {
            h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                    .hasRequiredHelpers(level, rite), "Bloom helper setup did not satisfy the recipe");
            var complete = com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.class
                    .getDeclaredMethod("completeRite", net.minecraft.server.level.ServerLevel.class,
                            ServerPlayer.class, com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.class);
            complete.setAccessible(true);
            h.assertTrue(!(boolean) complete.invoke(null, level, actor, rite),
                    "An obstructed Bloom column must reject completion");
            h.assertTrue(focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                    "A rejected Bloom must leave its Seed in the Focus");
            var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData
                    .get(level.getServer().overworld());
            var dimension = level.dimension().location().toString();
            h.assertTrue(blooms.getBloomAt(center.above(2), dimension) == null,
                    "A rejected Bloom registered a tree");
            level.removeBlock(obstruction, false);
            var nearby = new QliphothBloomSavedData.BloomEntry(UUID.randomUUID(),
                    center.above(2).east(16), dimension, 3, level.getGameTime());
            blooms.addBloom(nearby);
            try {
                h.assertTrue(!(boolean) complete.invoke(null, level, actor, rite),
                        "An overlapping Bloom must reject completion");
                h.assertTrue(focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                        "An overlap rejection consumed the Seed");
            } finally {
                blooms.removeBloomInChunk(nearby.center(), dimension);
            }
            h.assertTrue((boolean) complete.invoke(null, level, actor, rite),
                    "A clear-site retry did not complete the Bloom rite");
            var bloom = blooms.getBloomAt(center.above(2), dimension);
            h.assertTrue(bloom != null && bloom.ownerUUID().equals(actor.getUUID())
                            && level.getBlockState(center.above(2)).is(BlockInit.qliphoth_bloom.get()),
                    "The retry did not place and register the owner's Bloom");
            h.assertTrue(!focus.hasMedium(), "The successful retry did not consume the Seed");
        } catch (ReflectiveOperationException exception) {
            h.fail("Bloom completion check could not run: " + exception);
        } finally {
            level.removeBlock(obstruction, false);
            level.removeBlock(center.above(2), false);
            level.removeBlock(center, false);
            helper.discard();
            bloodlines.disbandBloodline(line.getBloodlineUUID());
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void blockedBloomSiteCannotStartItsCeremony(GameTestHelper h) {
        var actor = player(h, 7);
        HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
        var level = h.getLevel();
        var origin = h.absolutePos(new BlockPos(14, 3, 14));
        var focusPos = origin.above();
        var obstruction = focusPos.above(5);
        actor.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
                level.dimension().location().toString());
        try {
            HemoJourneyFixtures.prepareCardinalRite(actor, origin, "bloom_of_qliphoth");
            var focus = (com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity)
                    level.getBlockEntity(focusPos);
            h.assertTrue(focus != null && focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                    "Bloom fixture did not seat its Seed");
            for (int height = 3; height <= 9; height++) {
                level.setBlock(focusPos.above(height), Blocks.AIR.defaultBlockState(), 3);
            }
            h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents
                            .canPlaceQliphothBloom(level, actor, focusPos),
                    "Bloom fixture did not provide a clear column");
            level.setBlock(obstruction, Blocks.OBSIDIAN.defaultBlockState(), 3);
            var result = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(result == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.HANDLED
                            && com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level)
                                    .getRite(actor.getUUID()) == null,
                    "A blocked Bloom site started its ceremony");
            h.assertTrue(focus.getMediumForMatching().is(ItemInit.qliphoth_seed.get()),
                    "A rejected Bloom start consumed its Seed");
            level.removeBlock(obstruction, false);
            var root = focusPos.above(2);
            level.setBlock(root, Blocks.OBSIDIAN.defaultBlockState(), 3);
            var blockedRoot = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            h.assertTrue(blockedRoot == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.HANDLED
                            && com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level)
                                    .getRite(actor.getUUID()) == null,
                    "An occupied Bloom root started the ceremony");
            level.removeBlock(root, false);
            var retry = com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket
                    .tryStartCardinalRite(actor, focusPos,
                            com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
            var active = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level)
                    .getRite(actor.getUUID());
            h.assertTrue(retry == com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules.ActivationAttempt.STARTED
                            && active != null,
                    "A clear-site retry did not start the Bloom ceremony: " + retry + ", active=" + (active != null)
                            + ", placement=" + com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents
                                    .canPlaceQliphothBloom(level, actor, focusPos));
        } finally {
            level.removeBlock(obstruction, false);
            com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level).removeRite(actor.getUUID());
            HemoJourneyFixtures.cleanup(actor, origin);
            actor.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonistChoiceAndBlankRecipeFollowDegree(GameTestHelper h) {
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        var d1 = player(h, 1);
        mnemonist.setPos(d1.position());
        h.getLevel().addFreshEntity(mnemonist);
        h.assertTrue(mnemonist.progressionDialogue(d1).getStartNode().options().stream()
                .noneMatch(option -> "starter_choice".equals(option.nextNodeId())),
                "D1 Mnemonist offered the directed starter choice");
        var d2 = player(h, 2);
        h.assertTrue(mnemonist.progressionDialogue(d2).getStartNode().options().stream()
                .anyMatch(option -> "starter_choice".equals(option.nextNodeId())),
                "D2 Mnemonist did not offer the starter choice");
        DialogueEventHandler.onDialogueOption(new DialogueEvent(d2,
                "mnemonist_blank_memory_recipe", mnemonist.getId()));
        h.assertTrue(!d2.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "Forged D2 lesson awarded the D3 blank recipe");
        d2.getPersistentData().putBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY, true);
        h.assertTrue(mnemonist.progressionDialogue(d2).getStartNode().options().stream()
                .noneMatch(option -> "starter_choice".equals(option.nextNodeId())),
                "An earlier starter claim was offered again");

        var d3 = player(h, 3);
        var dialogue = mnemonist.progressionDialogue(d3);
        h.assertTrue(dialogue.getStartNode().options().stream().anyMatch(option ->
                        "mnemonist_blank_memory_recipe".equals(option.eventId())
                                && "woven_vessel".equals(option.nextNodeId())),
                "Woven Vessel did not teach the blank-memory recipe");
        h.assertTrue(!d3.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "Blank recipe was awarded before the D3 lesson");
        DialogueEventHandler.onDialogueOption(new DialogueEvent(d3,
                "mnemonist_blank_memory_recipe", mnemonist.getId()));
        h.assertTrue(d3.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "Mnemonist did not award the blank-memory recipe");
        HarbingerAdvancementGranter.grantIfNotDone(d3,
                HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE);
        h.assertTrue(mnemonist.progressionDialogue(d3).getStartNode().options().stream()
                .anyMatch(option -> "mnemonist_blank_memory_recipe".equals(option.eventId())
                        && "loom".equals(option.nextNodeId())),
                "Returning D3 learner lost the blank-memory lesson");
        h.succeed();
    }

    @GameTest(template = "empty") public static void chickenVialsProvideD3BlankMemoryEnzymeRoute(GameTestHelper h) {
        var p = player(h, 2);
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        var chicken = net.minecraft.world.entity.EntityType.CHICKEN.create(h.getLevel());
        var vial = new ItemStack(ItemInit.bloody_vial.get());
        ItemInit.bloody_vial.get().onLeftClickEntity(vial, p, chicken);
        h.assertTrue("minecraft:chicken".equals(BloodSampleData.rawSource(vial)),
                "D2 player could not collect chicken blood with an ordinary vial");
        var pos = h.absolutePos(new BlockPos(4, 2, 4));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.vial_centrifuge.get().defaultBlockState());
        var centrifuge = (VialCentrifugeBlockEntity) h.getLevel().getBlockEntity(pos);
        boolean neurotic = false;
        for (int i = 0; i < 64; i++) {
            var result = centrifuge.getResultFromVial(net.minecraft.world.entity.EntityType.CHICKEN);
            h.assertTrue(result.is(ItemInit.vivacious_enzyme.get()) || result.is(ItemInit.neurotic_enzyme.get()),
                    "Chicken centrifuging produced an inaccessible or unrelated enzyme");
            neurotic |= result.is(ItemInit.neurotic_enzyme.get());
        }
        h.assertTrue(neurotic, "Chicken blood never produced Neurotic Enzyme");
        h.assertTrue(h.getLevel().getRecipeManager().byKey(Hemomancy.rloc("hematic_memory")).isPresent(),
                "The blank Hematic Memory recipe did not load");
        h.succeed();
    }

    @GameTest(template = "empty") public static void craftedBlankCarriesWovenVesselIntoFirstLoomWeave(GameTestHelper h) {
        var p = player(h, 3);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        mnemonist.setPos(p.position());
        h.getLevel().addFreshEntity(mnemonist);
        DialogueEventHandler.onDialogueOption(new DialogueEvent(p,
                "mnemonist_blank_memory_recipe", mnemonist.getId()));
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("hematic_memory")),
                "D3 player did not learn the blank recipe before crafting it");

        var recipe = h.getLevel().getRecipeManager().byKey(Hemomancy.rloc("hematic_memory"))
                .orElseThrow().value();
        var stoneRecipe = h.getLevel().getRecipeManager()
                .byKey(Hemomancy.rloc("blood_stained_stone_from_venous_stone"))
                .orElseThrow().value();
        var stoneInput = CraftingInput.of(2, 2, java.util.List.of(
                new ItemStack(BlockInit.venous_stone.get()),
                new ItemStack(ItemInit.sanguine_formation.get()),
                new ItemStack(Items.REDSTONE), ItemStack.EMPTY));
        h.assertTrue(stoneRecipe instanceof CraftingRecipe stoneCraft && stoneCraft.matches(stoneInput, h.getLevel()),
                "Main-route bloodcraft materials must make Blood Stained Stone without a Remnant expedition");
        var stone = ((CraftingRecipe) stoneRecipe).assemble(stoneInput, h.getLevel().registryAccess());
        h.assertTrue(stone.is(ItemInit.blood_stained_stone.get()), "The bloodcraft recipe did not yield a stained stone");
        var formation = new ItemStack(ItemInit.sanguine_formation.get());
        var input = CraftingInput.of(3, 3, java.util.List.of(
                formation, stone, formation,
                stone, new ItemStack(ItemInit.neurotic_enzyme.get()), stone,
                formation, stone, formation));
        h.assertTrue(recipe instanceof CraftingRecipe crafting && crafting.matches(input, h.getLevel()),
                "Accessible D3 ingredients do not match the taught blank recipe");
        var blank = ((CraftingRecipe) recipe).assemble(input, h.getLevel().registryAccess());
        h.assertTrue(blank.is(ItemInit.hematic_memory.get()), "The crafted blank is not a Hematic Memory");
        p.getInventory().add(blank);
        p.getInventory().add(new ItemStack(Items.BOOK));
        p.getInventory().add(new ItemStack(Items.INK_SAC));
        p.getInventory().add(new ItemStack(Items.PAPER, 3));
        DialogueEventHandler.onDialogueOption(new DialogueEvent(p,
                "mnemonist_woven_vessel_turn_in", mnemonist.getId()));
        h.assertTrue(HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(p)
                        && !HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(p),
                "Material turn-in either failed or incorrectly counted as the first weave");
        var rewards = h.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(mnemonist.blockPosition()).inflate(2));
        h.assertTrue(rewards.stream().anyMatch(item -> item.getItem().is(ItemInit.bleeding_bulb.get()))
                        && rewards.stream().anyMatch(item -> item.getItem().is(ItemInit.vivacious_enzyme.get())
                                && item.getItem().getCount() == 3),
                "Woven Vessel did not drop its authored Bulb and three enzymes");
        rewards.forEach(item -> item.playerTouch(p));
        h.assertTrue(p.getInventory().contains(new ItemStack(ItemInit.bleeding_bulb.get()))
                        && p.getInventory().contains(new ItemStack(ItemInit.vivacious_enzyme.get())),
                "Woven Vessel did not supply the first weave's Bulb and enzyme");

        var pos = h.absolutePos(new BlockPos(4, 2, 4));
        h.getLevel().setBlockAndUpdate(pos, BlockInit.somatic_loom.get().defaultBlockState());
        var loom = (SomaticLoomBlockEntity) h.getLevel().getBlockEntity(pos);
        loom.addItem(p, p.getInventory().items.stream().filter(stack -> stack.is(ItemInit.hematic_memory.get()))
                .findFirst().orElseThrow(), null);
        loom.addItem(p, p.getInventory().items.stream().filter(stack -> stack.is(ItemInit.bleeding_bulb.get()))
                .findFirst().orElseThrow(), null);
        loom.addItem(p, p.getInventory().items.stream().filter(stack -> stack.is(ItemInit.vivacious_enzyme.get()))
                .findFirst().orElseThrow(), null);
        h.assertTrue(loom.selectRecipe(p, Hemomancy.rloc("memory_weaving/memory_blood_shot"))
                        && loom.getResultItem().is(ItemInit.memory_blood_shot.get()),
                "The supplied D3 materials cannot select the authored Blood Shot weave");
        var blood = HemoCapabilityAccess.requireBloodVolume(p);
        blood.setActive(true);
        blood.setBloodVolume(100);
        h.assertTrue(loom.startRitual(p) && loom.tryChargeRitualBlood(p, 50, true)
                        && loom.isWeavingOrbs() && blood.getBloodVolume() == 50,
                "The first weave did not accept its 50 blood charge");
        for (var orb : loom.getRitualOrbs()) {
            var orbPos = Vec3.atCenterOf(pos).add(orb.offset());
            p.setPos(orbPos.x + 2, orbPos.y - p.getEyeHeight(), orbPos.z);
            p.lookAt(EntityAnchorArgument.Anchor.EYES, orbPos);
            h.assertTrue(loom.dragSelectedOrb(p, 1), "The player could not catch the first strand");
            var target = Vec3.atCenterOf(pos).add(0, 0.4, 0);
            double eyeY = pos.getY() + 0.2 + p.getEyeHeight();
            double horizontal = Math.sqrt(4 - Math.pow(eyeY - target.y, 2));
            p.setPos(target.x + horizontal, pos.getY() + 0.2, target.z);
            p.lookAt(EntityAnchorArgument.Anchor.EYES, target);
            for (int pull = 0; pull < 200 && !orb.completed(); pull++) loom.dragSelectedOrb(p, 1);
            h.assertTrue(orb.completed(), "The first strand could not be drawn into the Loom");
        }
        h.assertTrue(HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(p)
                        && HarbingerAdvancementGranter.isMnemonistWovenVesselFinished(p),
                "Completing Blood Shot did not finish the Woven Vessel chapter proof");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2))
                        .stream().anyMatch(item -> item.getItem().is(ItemInit.memory_blood_shot.get())),
                "The first completed weave did not produce Blood Shot memory");
        p.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void paidLoomProjectionDoesNotRepeatStaffInstructions(GameTestHelper h) {
        var feedback = new ArrayList<net.minecraft.network.chat.Component>();
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "loom-feedback-test"), false);
        var p = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation()) {
            @Override public void displayClientMessage(net.minecraft.network.chat.Component message, boolean overlay) {
                feedback.add(message);
            }
        };
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(3);
        try {
            var pos = h.absolutePos(new BlockPos(4, 2, 4));
            h.getLevel().setBlockAndUpdate(pos, BlockInit.somatic_loom.get().defaultBlockState());
            var loom = (SomaticLoomBlockEntity) h.getLevel().getBlockEntity(pos);
            loom.addItem(p, new ItemStack(ItemInit.hematic_memory.get()), null);
            loom.addItem(p, new ItemStack(ItemInit.bleeding_bulb.get()), null);
            loom.addItem(p, new ItemStack(ItemInit.vivacious_enzyme.get()), null);
            h.assertTrue(loom.selectRecipe(p, Hemomancy.rloc("memory_weaving/memory_blood_shot")),
                    "Fixture must select Blood Shot before payment");
            var blood = HemoCapabilityAccess.requireBloodVolume(p);
            blood.setActive(true);
            blood.setBloodVolume(100);
            h.assertTrue(loom.tryChargeRitualBlood(p, 50, false) && loom.isWeavingOrbs(),
                    "Projection must pay for the strand");
            feedback.clear();
            for (int tick = 0; tick < 20; tick++)
                h.assertTrue(!loom.tryChargeRitualBlood(p, 50, false), "A paid weave cannot accept more blood");
            h.assertTrue(feedback.isEmpty(), "Holding Projection after payment must not repeat Staff instructions every tick");
            h.assertTrue(blood.getBloodVolume() == 50 && loom.getRitualBloodCharged() == 50,
                    "Continued Projection must preserve the single payment");
            loom.provideTendencyFeedback(p);
            h.assertTrue(feedback.size() == 1 && feedback.getFirst().getString().contains("Hold a Living Staff"),
                    "Explicit empty-hand feedback must still explain strand handling");

            feedback.clear();
            var saved = loom.saveWithoutMetadata(h.getLevel().registryAccess());
            saved.putString("selectedRecipeId", "hemomancy:memory_weaving/removed_fixture");
            loom.loadWithComponents(saved, h.getLevel().registryAccess());
            h.assertTrue(!loom.tryChargeRitualBlood(p, 50, false)
                            && feedback.stream().anyMatch(message -> message.getString().contains("weave is paused")),
                    "Projection must still report a missing paid recipe instead of silently ignoring recovery");
        } finally {
            p.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void scriptoriumLessonUnlocksItsRegisteredPageWithoutPromotion(GameTestHelper h) {
        var p = player(h, 3);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        mnemonist.setPos(p.position());
        h.getLevel().addFreshEntity(mnemonist);
        var entry = Hemomancy.rloc("libersanguinium/tendency/pages/enzymatic_scriptorium");
        var choice = mnemonist.progressionDialogue(p).nodes().values().stream()
                .flatMap(node -> node.options().stream())
                .filter(option -> "scriptorium".equals(option.nextNodeId())).findFirst().orElseThrow();
        h.assertTrue(HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON.equals(choice.eventId()),
                "D3 Scriptorium teaching has no book discovery action");
        var event = DialogueOptionPacket.dispatch(p, choice.eventId(), mnemonist.getId());
        h.assertTrue(event != null && event.wasRewardDelivered() && LiberKnowledgeHelper.hasEntry(p, entry)
                        && com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery
                                .LiberEntryDefinitions.get(entry).isPresent(),
                "The actual teacher choice did not unlock a registered operation page");
        var repeat = DialogueOptionPacket.dispatch(p, choice.eventId(), mnemonist.getId());
        h.assertTrue(repeat != null && !repeat.wasRewardDelivered() && p.getInventory().isEmpty()
                        && HemoCapabilityAccess.getPlayerDegreeNumber(p) == 3
                        && !HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(p),
                "Repeated optional teaching granted items or Main promotion proof");
        mnemonist.discard();
        p.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void scriptoriumLessonRejectsEarlyRemoteAndWrongTeachers(GameTestHelper h) {
        var p = player(h, 2);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        mnemonist.setPos(p.position());
        h.getLevel().addFreshEntity(mnemonist);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(p.position());
        h.getLevel().addFreshEntity(alchemist);
        var entry = Hemomancy.rloc("libersanguinium/tendency/pages/enzymatic_scriptorium");
        String lesson = HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON;
        DialogueOptionPacket.dispatch(p, lesson, mnemonist.getId());
        h.assertTrue(!LiberKnowledgeHelper.hasEntry(p, entry), "D2 forged a D3 operation lesson");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(3);
        h.assertTrue(DialogueOptionPacket.dispatch(p, lesson, alchemist.getId()) == null
                        && DialogueOptionPacket.dispatch(p, lesson, 0) == null,
                "Wrong or absent teacher accepted the Mnemonist lesson");
        mnemonist.setPos(p.position().add(9, 0, 0));
        h.assertTrue(DialogueOptionPacket.dispatch(p, lesson, mnemonist.getId()) == null
                        && !LiberKnowledgeHelper.hasEntry(p, entry),
                "Out-of-range teaching revealed the operation page");
        mnemonist.discard();
        alchemist.discard();
        p.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonistStationLessonsFollowDegreeWithoutNewPromotionGates(GameTestHelper h) {
        var p = player(h, 3);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel());
        mnemonist.setPos(p.position());
        var d3 = mnemonist.progressionDialogue(p);
        h.assertTrue(d3.getNode("scriptorium") != null && d3.getNode("memory_practice") == null
                        && d3.getNode("distributor") == null,
                "D3 station lessons are missing or later practice arrived early");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        var d4 = mnemonist.progressionDialogue(p);
        h.assertTrue(d4.getNode("memory_practice") != null && d4.getNode("distributor") == null,
                "D4 did not deepen memory selection or exposed the D5 Distributor");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        var d5 = mnemonist.progressionDialogue(p);
        h.assertTrue(d5.getNode("distributor") != null
                        && d5.getNode("scriptorium").lines().contains("hemomancy.mnemonist.scriptorium.eightfold"),
                "D5 did not teach named loadouts and the Eightfold Script");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(7);
        h.assertTrue(mnemonist.progressionDialogue(p).getNode("scriptorium").lines()
                        .contains("hemomancy.mnemonist.scriptorium.palimpsest_lesson"),
                "D7 did not name the Rite of the Palimpsest");
        h.succeed();
    }

    @GameTest(template = "empty") public static void alchemistRecognizesEarlierTinctureWorkAtD3(GameTestHelper h) {
        var p = player(h, 3);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(p.position());
        var unbriefed = alchemist.progressionDialogue(p);
        h.assertTrue(unbriefed.getNode("thelemic_preparations") != null
                        && unbriefed.getNode("thelemic_preparations").lines()
                                .contains("hemomancy.alchemist.preparations.unbriefed")
                        && unbriefed.getNode("station_upgrade_alembic_1") == null
                        && unbriefed.getNode("station_upgrade_alembic_2") == null,
                "D3 preparation teaching changed or exposed a D4 upgrade early");
        HarbingerAdvancementGranter.grantIfNotDone(p, BodyAnswersAssignment.ADV_BRIEFED);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("thelemic_preparations").lines()
                        .contains("hemomancy.alchemist.preparations.briefed"),
                "An already briefed player was treated as new to Body Answers");
        HarbingerAdvancementGranter.grantIfNotDone(p, BodyAnswersAssignment.ADV_COMPLETE);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("thelemic_preparations").lines()
                        .contains("hemomancy.alchemist.preparations.complete"),
                "An earlier tincture drink was not recognized at D3");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("station_upgrade_alembic_1") != null
                        && alchemist.progressionDialogue(p).getNode("station_upgrade_centrifuge_1") != null,
                "D4 Alchemist station projects were not taught");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("station_upgrade_alembic_2") == null,
                "D5 exposed the D6 Athanor early");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(6);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("station_upgrade_alembic_2") != null,
                "D6 Athanor eligibility disappeared from the existing station progression");
        h.succeed();
    }

    @GameTest(template = "empty") public static void specimenJarLessonGrantsOnlyNewStudentsOnce(GameTestHelper h) {
        var p = player(h, 2);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(p.position());
        h.assertTrue(SpecimenJarLesson.canTeach(p), "New D2 player cannot request the jar lesson");
        var dialogue = alchemist.progressionDialogue(p);
        h.assertTrue(dialogue.getNode("living_bestiary_intro").options().stream()
                .anyMatch(option -> com.vincenthuto.hemomancy.common.entity.npc.dialogue.SpecimenJarLessonDialogue.TEACH.equals(option.eventId())),
                "D2 Living Bestiary dialogue has no jar lesson action");
        h.assertTrue(dialogue.getNode("red_taxonomy_intro").options().stream()
                .anyMatch(option -> "bloodwood_lesson".equals(option.nextNodeId())),
                "D2 Red Taxonomy dialogue has no bloodwood instruction");
        h.assertTrue(SpecimenJarLesson.teach(p, alchemist), "Alchemist did not teach the jar lesson");
        h.assertTrue(!SpecimenJarLesson.teach(p, alchemist), "Jar lesson could be claimed twice");
        h.assertTrue(jarCount(p) == 2, "Jar lesson did not give exactly two jars");
        h.assertTrue(alchemist.progressionDialogue(p).getNode("living_bestiary_intro").options().stream()
                .noneMatch(option -> com.vincenthuto.hemomancy.common.entity.npc.dialogue.SpecimenJarLessonDialogue.TEACH.equals(option.eventId())),
                "Claimed jar lesson remained available in dialogue");
        var restored = player(h, 2);
        restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        h.assertTrue(!SpecimenJarLesson.canTeach(restored), "Jar lesson claim was lost after player data restore");

        var experienced = player(h, 2);
        var otherAlchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        otherAlchemist.setPos(experienced.position());
        HemoCapabilityAccess.requireSpecimenBestiary(experienced).recordSpecimen(Hemomancy.rloc("chitinite"));
        h.assertTrue(SpecimenJarLesson.teach(experienced, otherAlchemist), "Experienced player could not hear the lesson");
        h.assertTrue(jarCount(experienced) == 0, "Existing specimen researcher received duplicate starter jars");
        h.succeed();
    }

    private static int jarCount(ServerPlayer player) {
        return player.getInventory().items.stream()
                .filter(stack -> stack.is(BlockInit.specimen_jar.get().asItem()))
                .mapToInt(ItemStack::getCount).sum();
    }

	@GameTest(template = "empty") public static void firstDrawsCountsCollectedAnimalsAndCarriesSampleToD2(GameTestHelper h) {
		var p = player(h, 1);
		HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
		HemoCapabilityAccess.requireEquipment(p).setStackInSlot(5,
				new ItemStack(ItemInit.charm_of_vascularium.get()));
		var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
		alchemist.setPos(p.position());
		h.assertTrue(alchemist.progressionDialogue(p).getStartNode().options().stream()
				.anyMatch(option -> "first_draws_offer".equals(option.nextNodeId())),
				"D1 dialogue did not offer First Draws");
		h.assertTrue(DialogueHubFactory.decorate(alchemist.progressionDialogue(p), "alchemist", p)
				.presentation().topics(DialogueCategory.QUESTS).stream()
				.anyMatch(topic -> "first_draws_offer".equals(topic.targetNodeId())),
				"First Draws did not appear among Alchemist assignments");
		h.assertTrue(FirstDrawsAssignment.brief(p, alchemist), "D1 Alchemist did not offer First Draws");
		h.assertTrue(!FirstDrawsAssignment.brief(p, alchemist), "Repeat briefing duplicated the one-time supply claim");
		h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("bloody_vial")),
				"First Draws did not teach the replacement Blood Vial recipe");
		var firstEmpty = new ItemStack(ItemInit.bloody_vial.get());
		p.setItemInHand(InteractionHand.MAIN_HAND, firstEmpty);
		ItemInit.bloody_vial.get().onLeftClickEntity(firstEmpty, p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		ItemInit.bloody_vial.get().onLeftClickEntity(p.getMainHandItem(), p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		h.assertTrue(FirstDrawsAssignment.progress(p).samples() == 1,
				"Inspecting a filled vial counted as a second collection");
		for (var type : java.util.List.of(net.minecraft.world.entity.EntityType.COW,
				net.minecraft.world.entity.EntityType.COW,
				net.minecraft.world.entity.EntityType.PIG, net.minecraft.world.entity.EntityType.SHEEP)) {
			var empty = new ItemStack(ItemInit.bloody_vial.get());
			p.setItemInHand(InteractionHand.MAIN_HAND, empty);
			ItemInit.bloody_vial.get().onLeftClickEntity(empty, p, type.create(h.getLevel()));
		}
		h.assertTrue(FirstDrawsAssignment.progress(p).complete(), "Five real samples from three animals were not recorded");
		h.assertTrue(!ClinicalBloodKnowledge.canInject(p), "D1 collection unlocked ordinary injection");
		var restored = player(h, 1);
		restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
				p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
		h.assertTrue(FirstDrawsAssignment.progress(restored).complete(), "First Draws was lost after player data restore");
		HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(2);
		FirstSeparationAssignment.markBriefed(p);
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"The D1 sample in inventory was not recognized at D2 briefing");
		h.succeed();
	}

    private enum PendingChamberGift { DRAWS, BRIEFING, TOOLS, SPINE, MEMORY, TENDRIL }

    @GameTest(template = "empty") public static void dreamRecoveryKeepsDraws(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.DREAM, PendingChamberGift.DRAWS); h.succeed(); }
    @GameTest(template = "empty") public static void dreamRecoveryKeepsBriefing(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.DREAM, PendingChamberGift.BRIEFING); h.succeed(); }
    @GameTest(template = "empty") public static void dreamRecoveryKeepsTools(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.DREAM, PendingChamberGift.TOOLS); h.succeed(); }
    @GameTest(template = "empty") public static void dreamRecoveryKeepsSpine(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.DREAM, PendingChamberGift.SPINE); h.succeed(); }
    @GameTest(template = "empty") public static void dreamRecoveryKeepsMemory(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.DREAM, PendingChamberGift.MEMORY); h.succeed(); }
    @GameTest(template = "empty") public static void dreamRecoveryKeepsTendril(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.DREAM, PendingChamberGift.TENDRIL); h.succeed(); }
    @GameTest(template = "empty") public static void guidedRecoveryKeepsDraws(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.GUIDED, PendingChamberGift.DRAWS); h.succeed(); }
    @GameTest(template = "empty") public static void guidedRecoveryKeepsBriefing(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.GUIDED, PendingChamberGift.BRIEFING); h.succeed(); }
    @GameTest(template = "empty") public static void guidedRecoveryKeepsTools(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.GUIDED, PendingChamberGift.TOOLS); h.succeed(); }
    @GameTest(template = "empty") public static void guidedRecoveryKeepsSpine(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.GUIDED, PendingChamberGift.SPINE); h.succeed(); }
    @GameTest(template = "empty") public static void guidedRecoveryKeepsMemory(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.GUIDED, PendingChamberGift.MEMORY); h.succeed(); }
    @GameTest(template = "empty") public static void guidedRecoveryKeepsTendril(GameTestHelper h) { pendingChamberGift(h, ChamberVisitMode.GUIDED, PendingChamberGift.TENDRIL); h.succeed(); }

    @GameTest(template = "empty") public static void inventoryCarryingChamberVisitsAllowPendingGifts(GameTestHelper h) {
        for (var mode : List.of(ChamberVisitMode.TIMED_CHAIR, ChamberVisitMode.ATTUNED, ChamberVisitMode.ADMIN))
            for (var gift : PendingChamberGift.values()) pendingChamberGift(h, mode, gift);
        h.succeed();
    }

    private static void pendingChamberGift(GameTestHelper h, ChamberVisitMode mode, PendingChamberGift gift) {
        var actor = player(h, 7);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(actor.position());
        try {
            HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++)
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            List<ItemStack> expected = switch (gift) {
                case DRAWS -> {
                    h.assertTrue(FirstDrawsAssignment.brief(actor, alchemist), "Fixture must earn the Draws supply claim");
                    yield List.of(new ItemStack(ItemInit.bloody_vial.get(), 5));
                }
                case BRIEFING -> {
                    FirstSeparationAssignment.markBriefed(actor);
                    FirstSeparationAssignment.giveBriefingSupplies(actor);
                    yield List.of(new ItemStack(ItemInit.bloody_vial.get(),
                            FirstSeparationAssignment.briefingStacks().stream().mapToInt(ItemStack::getCount).sum()));
                }
                case TOOLS -> {
                    FirstSeparationAssignment.markBriefed(actor);
                    HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
                    HarbingerAdvancementGranter.grantIfNotDone(actor, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
                    h.assertTrue(FirstSeparationAssignment.claimRewards(actor), "Fixture must record its supplied earned tool claim");
                    yield FirstSeparationAssignment.rewardStacks();
                }
                case SPINE -> {
                    HemoCapabilityAccess.requireInitiatoryDegree(actor).setQliphothCommunionDone(true);
                    yield List.of(new ItemStack(ItemInit.fungal_spine.get()));
                }
                case MEMORY, TENDRIL -> {
                    var persisted = actor.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                    persisted.putBoolean(gift == PendingChamberGift.MEMORY
                            ? "hemomancy:vesper_memory_pending" : "hemomancy:mycophant_tendril_pending", true);
                    actor.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
                    yield List.of(new ItemStack(gift == PendingChamberGift.MEMORY
                            ? ItemInit.memory_of_vesper.get() : ItemInit.mycophant_tendril.get()));
                }
            };
            var snapshot = actor.getInventory().save(new net.minecraft.nbt.ListTag());
            // Supplied interrupted visit state exercises recovery without requiring a loaded destination.
            var data = actor.getPersistentData();
            data.putBoolean("hemomancy:chamber_visit_active", true);
            data.putString("hemomancy:chamber_visit_mode", mode.name());
            boolean observational = com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isObservational(actor);
            if (observational) data.put("hemomancy:chamber_visit_dream_inventory", snapshot.copy());
            actor.getInventory().clearContent();
            retryPendingChamberGifts(actor);
            boolean temporaryInventoryEmpty = actor.getInventory().isEmpty();
            if (observational) {
                com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.recoverOutsideChamber(actor);
                h.assertTrue(snapshot.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag()))
                                && !com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.isActive(actor),
                        "Recovery must restore the exact entry inventory and close the supplied session");
                actor.getInventory().clearContent();
                retryPendingChamberGifts(actor);
            }
            for (var stack : expected) {
                h.assertTrue(actor.getInventory().countItem(stack.getItem()) == stack.getCount(),
                        mode + " recovery lost or duplicated pending " + gift);
                var delivered = actor.getInventory().items.stream().filter(s -> s.is(stack.getItem())).findFirst().orElseThrow();
                h.assertTrue(delivered.getComponents().equals(stack.getComponents()), "Recovery changed " + gift + " components");
            }
            h.assertTrue(!observational || temporaryInventoryEmpty,
                    "Observational retries must leave the temporary inventory untouched");
            var delivered = actor.getInventory().save(new net.minecraft.nbt.ListTag());
            retryPendingChamberGifts(actor);
            h.assertTrue(delivered.equals(actor.getInventory().save(new net.minecraft.nbt.ListTag()))
                            && HemoCapabilityAccess.getPlayerDegreeNumber(actor) == 7,
                    "Repeated delivery must not duplicate gifts or change degree");
        } finally { actor.discard(); alchemist.discard(); }
    }

    private static void retryPendingChamberGifts(ServerPlayer actor) {
        actor.tickCount = 20;
        var tick = new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor);
        ClinicalBloodKnowledge.tick(tick);
        QliphothBloomEvents.onPlayerTick(tick);
        VesperOrdealManager.onPlayerTick(tick);
        com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager.onPlayerTick(tick);
    }

    @GameTest(template = "empty") public static void firstDrawsFullInventoryKeepsSuppliesAcrossDeath(GameTestHelper h) {
        firstDrawsPendingSupplies(h, false);
    }

    @GameTest(template = "empty") public static void separationRewardWaitsForInventorySpace(GameTestHelper h) {
        separationPendingReward(h, false, false);
    }

    @GameTest(template = "empty") public static void separationBriefingKeepsFullInventoryVialsAcrossDeath(GameTestHelper h) {
        separationPendingBriefing(h, false, false);
    }

    @GameTest(template = "empty") public static void separationBriefingRejectsAStaleIneligibleCallback(GameTestHelper h) {
        for (boolean clarity : new boolean[] {false, true}) {
            var actor = player(h, 2);
            var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
            alchemist.setPos(actor.position());
            h.getLevel().addFreshEntity(alchemist);
            var unstained = HemoCapabilityAccess.getUnstainedProgress(actor).orElseThrow();
            try {
                if (clarity) unstained.setClarityUnlocked(true);
                else unstained.setBegunPurification(true);
                DialogueEventHandler.onDialogueOption(new DialogueEvent(actor,
                        "alchemist_first_separation_brief", alchemist.getId()));
                h.assertTrue(!FirstSeparationAssignment.isBriefed(actor)
                                && actor.getInventory().countItem(ItemInit.bloody_vial.get()) == 0,
                        "Stale ineligible callback must not record a briefing without its supply claim");
                unstained.setClarityUnlocked(false);
                unstained.setBegunPurification(false);
                DialogueEventHandler.onDialogueOption(new DialogueEvent(actor,
                        "alchemist_first_separation_brief", alchemist.getId()));
                h.assertTrue(FirstSeparationAssignment.isBriefed(actor)
                                && actor.getInventory().countItem(ItemInit.bloody_vial.get()) == 2,
                        "Eligible retry must still offer the complete one-time briefing");
            } finally { actor.discard(); alchemist.discard(); }
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void separationBriefingKeepsCreativeDiscardedVials(GameTestHelper h) {
        separationPendingBriefing(h, true, false);
    }

    @GameTest(template = "empty") public static void separationBriefingRetriesOnlyItsRemainingVial(GameTestHelper h) {
        separationPendingBriefing(h, false, true);
    }

    private static void separationPendingBriefing(GameTestHelper h, boolean creative, boolean partial) {
        var actor = player(h, 2);
        var restored = player(h, 2, h.getLevel(), packet -> {}, actor.getGameProfile());
        var respawned = player(h, 2, h.getLevel(), packet -> {}, actor.getGameProfile());
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(actor.position());
        h.getLevel().addFreshEntity(alchemist);
        var area = actor.getBoundingBox().inflate(4);
        try {
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++)
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            if (partial) actor.getInventory().setItem(0, new ItemStack(ItemInit.bloody_vial.get(), 63));
            if (creative) actor.setGameMode(GameType.CREATIVE);
            DialogueEventHandler.onDialogueOption(new DialogueEvent(actor,
                    "alchemist_first_separation_brief", alchemist.getId()));
            h.assertTrue(FirstSeparationAssignment.isBriefed(actor), "Full inventory must not block briefing");
            FirstSeparationAssignment.giveBriefingSupplies(actor);
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                            item -> item.getItem().is(ItemInit.bloody_vial.get())).isEmpty(),
                    "Separation briefing vials must remain pending, not become overflow drops");
            restored.load(actor.saveWithoutId(new CompoundTag()));
            restored.getInventory().clearContent();
            restored.setHealth(0);
            restored.tickCount = 20;
            ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(restored));
            h.assertTrue(restored.getInventory().countItem(ItemInit.bloody_vial.get()) == 0,
                    "Dead player must retain pending briefing supplies despite free inventory space");
            if (partial) {
                actor.getInventory().setItem(1, ItemStack.EMPTY);
                ClinicalBloodKnowledge.login(new PlayerEvent.PlayerLoggedInEvent(actor));
                actor.tickCount = 20;
                ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
                h.assertTrue(actor.getInventory().countItem(ItemInit.bloody_vial.get()) == 65,
                        "Partial retry must deliver only one remaining vial, not another two-vial gift");
            } else {
                respawned.restoreFrom(restored, false);
                respawned.setHealth(20);
                respawned.getInventory().clearContent();
                ClinicalBloodKnowledge.login(new PlayerEvent.PlayerLoggedInEvent(respawned));
                respawned.tickCount = 20;
                ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(respawned));
                h.assertTrue(respawned.getInventory().countItem(ItemInit.bloody_vial.get()) == 2,
                        "Saved/death-cloned briefing must deliver exactly two vials once");
            }
            var recipient = partial ? actor : respawned;
            int before = recipient.getInventory().countItem(ItemInit.bloody_vial.get());
            FirstSeparationAssignment.giveBriefingSupplies(recipient);
            h.assertTrue(recipient.getInventory().countItem(ItemInit.bloody_vial.get()) == before
                            && !FirstSeparationAssignment.hasSampleAcquired(recipient)
                            && !FirstSeparationAssignment.isClaimed(recipient)
                            && HemoCapabilityAccess.getPlayerDegreeNumber(recipient) == 2,
                    "Briefing retry must refuse duplicate gifts without earning collection, reward or rank proof");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                    item -> item.getItem().is(ItemInit.bloody_vial.get())).forEach(ItemEntity::discard);
            actor.discard(); restored.discard(); respawned.discard(); alchemist.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void separationRewardSurvivesCreativeDiscard(GameTestHelper h) {
        separationPendingReward(h, true, false);
    }

    @GameTest(template = "empty") public static void separationRewardRetriesOnlyTheUndeliveredRack(GameTestHelper h) {
        separationPendingReward(h, false, true);
    }

    private static void separationPendingReward(GameTestHelper h, boolean creative, boolean oneSlot) {
        var actor = player(h, 2);
        var restored = player(h, 2, h.getLevel(), packet -> {}, actor.getGameProfile());
        var respawned = player(h, 2, h.getLevel(), packet -> {}, actor.getGameProfile());
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(actor.position());
        h.getLevel().addFreshEntity(alchemist);
        var area = actor.getBoundingBox().inflate(4);
        try {
            FirstSeparationAssignment.markBriefed(actor);
            com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(actor,
                    com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
            com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(actor,
                    com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++)
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            if (oneSlot) actor.getInventory().setItem(0, ItemStack.EMPTY);
            if (creative) actor.setGameMode(GameType.CREATIVE);
            var claim = new DialogueEvent(actor, "alchemist_first_separation_claim", alchemist.getId());
            DialogueEventHandler.onDialogueOption(claim);
            h.assertTrue(FirstSeparationAssignment.isClaimed(actor), "Valid Separation claim was not recorded");
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                            item -> item.getItem().is(ItemInit.living_syringe.get())
                                    || item.getItem().is(ItemInit.vial_rack.get())).isEmpty(),
                    "Earned Separation tools must wait for space rather than become overflow drops");
            restored.load(actor.saveWithoutId(new CompoundTag()));
            restored.getInventory().clearContent();
            restored.setHealth(0);
            restored.tickCount = 20;
            ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(restored));
            h.assertTrue(restored.getInventory().countItem(ItemInit.vial_rack.get()) == 0,
                    "Dead-player retries must not deliver the pending rack");
            if (oneSlot) {
                actor.getInventory().setItem(1, ItemStack.EMPTY);
                ClinicalBloodKnowledge.login(new PlayerEvent.PlayerLoggedInEvent(actor));
                actor.tickCount = 20;
                ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
                h.assertTrue(actor.getInventory().countItem(ItemInit.living_syringe.get()) == 1,
                        "Partial retry duplicated the already delivered syringe");
            } else {
                respawned.restoreFrom(restored, false);
                respawned.setHealth(20);
                respawned.getInventory().clearContent();
                ClinicalBloodKnowledge.login(new PlayerEvent.PlayerLoggedInEvent(respawned));
                respawned.tickCount = 20;
                ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(respawned));
                h.assertTrue(respawned.getInventory().countItem(ItemInit.living_syringe.get()) == 1,
                        "Death-cloned pending claim did not deliver exactly one syringe");
            }
            var recipient = oneSlot ? actor : respawned;
            h.assertTrue(recipient.getInventory().countItem(ItemInit.vial_rack.get()) == 1,
                    "Pending claim did not deliver exactly one initialized rack");
            var rack = recipient.getInventory().items.stream().filter(s -> s.is(ItemInit.vial_rack.get()))
                    .findFirst().orElseThrow();
            h.assertTrue(rack.getComponents().equals(FirstSeparationAssignment.rewardStacks().get(1).getComponents()),
                    "Pending recovery changed the initialized rack components");
            h.assertTrue(!FirstSeparationAssignment.canClaim(actor)
                            && HemoCapabilityAccess.getPlayerDegreeNumber(recipient) == 2,
                    "Tool recovery must not reopen the claim or promote the player");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                    item -> item.getItem().is(ItemInit.living_syringe.get())
                            || item.getItem().is(ItemInit.vial_rack.get())).forEach(ItemEntity::discard);
            actor.discard(); restored.discard(); respawned.discard(); alchemist.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void firstDrawsCreativeDiscardKeepsSuppliesAcrossDeath(GameTestHelper h) {
        firstDrawsPendingSupplies(h, true);
    }

    private static void firstDrawsPendingSupplies(GameTestHelper h, boolean creative) {
        var actor = player(h, 1);
        var restored = player(h, 1, h.getLevel(), packet -> {}, actor.getGameProfile());
        var respawned = player(h, 1, h.getLevel(), packet -> {}, actor.getGameProfile());
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(actor.position());
        var area = actor.getBoundingBox().inflate(4);
        try {
            HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++)
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            if (creative) actor.setGameMode(GameType.CREATIVE);
            h.assertTrue(FirstDrawsAssignment.brief(actor, alchemist), "Full inventory must not block the lesson");
            h.assertTrue(!FirstDrawsAssignment.brief(actor, alchemist), "Repeated lesson duplicated its supply claim");
            h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                            item -> item.getItem().is(ItemInit.bloody_vial.get())).isEmpty(),
                    "First Draws overflow must remain pending, not become a lossy world drop");
            restored.load(actor.saveWithoutId(new CompoundTag()));
            restored.getInventory().clearContent();
            restored.setHealth(0);
            restored.tickCount = 20;
            ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(restored));
            h.assertTrue(restored.getInventory().countItem(ItemInit.bloody_vial.get()) == 0,
                    "Dead-player retries must not deliver supplies before death cloning");
            respawned.restoreFrom(restored, false);
            respawned.setHealth(20);
            HemoCapabilityAccess.requireBloodVolume(respawned).setActive(true);
            ClinicalBloodKnowledge.login(new PlayerEvent.PlayerLoggedInEvent(respawned));
            respawned.tickCount = 20;
            ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(respawned));
            h.assertTrue(FirstDrawsAssignment.isBriefed(respawned)
                            && respawned.getInventory().countItem(ItemInit.bloody_vial.get()) == 5,
                    "Saved pending supplies must survive death cloning and deliver exactly five vials at D1");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                    item -> item.getItem().is(ItemInit.bloody_vial.get())).forEach(ItemEntity::discard);
            actor.discard(); restored.discard(); respawned.discard(); alchemist.discard();
        }
        h.succeed();
    }

    @GameTest(template = "empty") public static void firstDrawsPartialInsertionRetriesOnlyTheRemainingVials(GameTestHelper h) {
        var actor = player(h, 1);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setPos(actor.position());
        var area = actor.getBoundingBox().inflate(4);
        try {
            HemoCapabilityAccess.requireBloodVolume(actor).setActive(true);
            for (int slot = 0; slot < actor.getInventory().getContainerSize(); slot++)
                actor.getInventory().setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            actor.getInventory().setItem(0, new ItemStack(ItemInit.bloody_vial.get(), 63));
            h.assertTrue(FirstDrawsAssignment.brief(actor, alchemist), "Partial room must allow briefing");
            h.assertTrue(actor.getInventory().countItem(ItemInit.bloody_vial.get()) == 64,
                    "Briefing must fill the one available vial position");
            actor.getInventory().setItem(1, ItemStack.EMPTY);
            ClinicalBloodKnowledge.login(new PlayerEvent.PlayerLoggedInEvent(actor));
            actor.tickCount = 20;
            ClinicalBloodKnowledge.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(actor));
            h.assertTrue(actor.getInventory().countItem(ItemInit.bloody_vial.get()) == 68,
                    "Retry must insert only four remaining vials and refuse duplicate delivery");
            h.assertTrue(FirstDrawsAssignment.progress(actor).samples() == 0,
                    "Supply delivery must not count as collecting samples");
        } finally {
            h.getLevel().getEntitiesOfClass(ItemEntity.class, area,
                    item -> item.getItem().is(ItemInit.bloody_vial.get())).forEach(ItemEntity::discard);
            actor.discard(); alchemist.discard();
        }
        h.succeed();
    }

	@GameTest(template = "empty") public static void firstBloodcraftProofsPromoteOnlyAfterVicarReturn(GameTestHelper h) {
		var p = player(h, 1);
		FirstBloodcraftAssignment.recordStructure(p, new ItemStack(BlockInit.hematic_iron_block.get()));
		FirstBloodcraftAssignment.recordVenousStone(p);
		FirstBloodcraftAssignment.recordAbsorption(p, 300);
		h.assertTrue(!FirstBloodcraftAssignment.canClaim(p), "Incomplete D1 lessons allowed promotion");
		FirstBloodcraftAssignment.recordFormation(p);
		FirstBloodcraftAssignment.recordAbsorption(p, 200);
		h.assertTrue(FirstBloodcraftAssignment.canClaim(p), "All four D1 proofs did not unlock the Vicar return");
		h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(p) == 1,
				"Completing proofs promoted the player before speaking to the Vicar");
		h.assertTrue(FirstBloodcraftAssignment.promote(p), "The Vicar could not grant Degree 2");
		h.assertTrue(HemoCapabilityAccess.getPlayerDegreeNumber(p) == 2,
				"The Vicar return did not grant Degree 2");
		h.succeed();
	}

	@GameTest(template = "empty") public static void firstSeparationSamplingProofSurvivesUsingTheVial(GameTestHelper h) {
		var p = player(h, 2);
		FirstSeparationAssignment.markBriefed(p);
		var vial = new ItemStack(ItemInit.bloody_vial.get());
		p.setItemInHand(InteractionHand.MAIN_HAND, vial);
		ItemInit.bloody_vial.get().onLeftClickEntity(vial, p,
				net.minecraft.world.entity.EntityType.COW.create(h.getLevel()));
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"The First Separation did not remember a successful living sample");
		p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(p),
				"Using the sampled vial erased completed ledger progress");
		var restored = player(h, 2);
		restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
				p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
		h.assertTrue(FirstSeparationAssignment.hasSampleAcquired(restored),
				"The completed sample step did not survive persisted player restoration");
		h.succeed();
	}

	@GameTest(template = "empty") public static void assignmentLedgerPacketRoundTripsEveryField(GameTestHelper h) {
		try {
			RecordComponent[] components = OpenHarbingerAssignmentLedgerPacket.class.getRecordComponents();
			Class<?>[] parameterTypes = new Class<?>[components.length];
			Object[] values = new Object[components.length];
			for (int i = 0; i < components.length; i++) {
				parameterTypes[i] = components[i].getType();
				values[i] = "circusDiscovered".equals(components[i].getName())
						? true : ledgerPacketValue(components[i].getType(), i);
			}
			var constructor = OpenHarbingerAssignmentLedgerPacket.class.getDeclaredConstructor(parameterTypes);
			var original = (OpenHarbingerAssignmentLedgerPacket) constructor.newInstance(values);
			FriendlyByteBuf buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
			try {
				OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC.encode(buffer, original);
				h.assertTrue(original.equals(OpenHarbingerAssignmentLedgerPacket.STREAM_CODEC.decode(buffer)),
						"The assignment ledger packet changed fields during its network round trip");
				h.assertTrue(buffer.readableBytes() == 0,
						"The assignment ledger packet left unread network data");
			} finally {
				buffer.release();
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not construct the assignment ledger packet", exception);
		}
		h.succeed();
	}

	private static Object ledgerPacketValue(Class<?> type, int index) {
		if (type == EnumArchonPath.class) return EnumArchonPath.APOTHEOS;
		if (type == int.class) return index * 17 + 3;
		if (type == boolean.class) return index % 3 == 0;
		if (type == FirstSeparationLedgerProgress.class) {
			return new FirstSeparationLedgerProgress(true, false, true, false, true, false, true, true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurveyProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurveyProgress(
					true, 2, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroductionProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroductionProgress(true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingProgress(true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommissionProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommissionProgress(true, false);
		}
		if (type == PhlegethonticCommissionProgress.class) {
			return new PhlegethonticCommissionProgress(true, 4, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiryProgress(
					true, true, true, false);
		}
		if (type == com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress.class) {
			return new com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress(
					275, true, false, true, false);
		}
		throw new IllegalArgumentException("No packet test value for " + type.getName());
	}

    @GameTest(template = "empty") public static void phlegethonticCommissionChecksBothSourcesAndFiveItems(GameTestHelper h) {
        var p = player(h, 5);
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel());
        alchemist.setNoAi(true); alchemist.setPos(p.position()); h.getLevel().addFreshEntity(alchemist);
        var vicar = EntityInit.harbinger_vicar.get().create(h.getLevel());
        vicar.setNoAi(true); vicar.setPos(p.position()); h.getLevel().addFreshEntity(vicar);
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("phlegethontic_commission") == null,
                "Degree 4 saw the formal Nether commission");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        h.assertTrue(alchemist.progressionDialogue(p).getNode("phlegethontic_commission") != null,
                "Degree 5 could not reach the Nether commission");
        var scyphus = BlockInit.escharian_scyphus.get().asItem();
        p.getInventory().setItem(9, new ItemStack(scyphus, 5));
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.bloody_vial.get()));
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Empty vial satisfied the sample step");
        p.setItemInHand(InteractionHand.MAIN_HAND, vial("hemomancy:missing_source"));
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Unreadable vial satisfied the sample step");
        var unrelated = vial("minecraft:creeper");
        BloodSampleData.identify(unrelated);
        p.setItemInHand(InteractionHand.MAIN_HAND, unrelated);
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Unrelated blood satisfied the sample step");
        var excoriated = vial("hemomancy:excoriated");
        p.setItemInHand(InteractionHand.MAIN_HAND, excoriated);
        h.assertTrue(PhlegethonticCommission.validSample(excoriated),
                "A valid source-bearing Excoriated vial required unrelated microscope identification");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(4);
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist),
                "Degree 4 completed a Degree 5 commission with otherwise valid proof");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        p.getInventory().setItem(9, new ItemStack(scyphus, 4));
        h.setBlock(new BlockPos(3, 1, 3), Blocks.BLACKSTONE);
        h.setBlock(new BlockPos(3, 2, 3), BlockInit.escharian_scyphus.get().defaultBlockState()
                .setValue(EscharianScyphusBlock.COUNT, 5));
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Four Scyphus items satisfied a five-item commission");
        p.getInventory().add(new ItemStack(scyphus));
        h.assertTrue(!PhlegethonticCommission.report(p, vicar), "The Vicar accepted the Alchemist's commission");
        h.assertTrue(DialogueOptionPacket.dispatch(p, PhlegethonticCommissionDialogue.BEARING, vicar.getId()) == null,
                "The Vicar dispatched the Alchemist's Basin bearing");
        var bearing = DialogueOptionPacket.dispatch(p, PhlegethonticCommissionDialogue.BEARING, alchemist.getId());
        h.assertTrue(bearing != null && bearing.wasRewardDelivered(), "Alchemist could not give a Basin bearing");
        var before = excoriated.copy();
        var state = PhlegethonticCommission.progress(p);
        h.assertTrue(state.ready(), "Ready state: sample=" + state.sampleProof()
                + ", Scyphus=" + state.scyphusCount() + ", eligible=" + PhlegethonticCommission.eligible(p)
                + ", held=" + p.getMainHandItem() + ", identified=" + BloodSampleData.identified(p.getMainHandItem()));
        var report = DialogueOptionPacket.dispatch(p, PhlegethonticCommissionDialogue.REPORT, alchemist.getId());
        h.assertTrue(report != null && report.wasRewardDelivered(), "Valid Excoriated sample was refused");
        h.assertTrue(ItemStack.isSameItemSameComponents(before, p.getMainHandItem())
                        && p.getInventory().countItem(scyphus) == 5,
                "Commission consumed the specimen or Scyphus items");
        h.assertTrue(!PhlegethonticCommission.report(p, alchemist), "Commission was accepted twice");
        p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        p.getInventory().clearContent();
        h.assertTrue(PhlegethonticCommission.progress(p).complete(), "Completed proof vanished with the items");
        var restored = player(h, 5);
        restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        h.assertTrue(PhlegethonticCommission.progress(restored).complete()
                        && PhlegethonticCommission.progress(restored).sampleProof()
                        && PhlegethonticCommission.progress(restored).scyphusCount() == 5,
                "Saved commission did not retain its completed ledger steps");

        var bombardier = player(h, 5);
        HemoCapabilityAccess.requireBloodVolume(bombardier).setActive(true);
        var bombardierSample = vial("hemomancy:phlegethontic_bombardier");
        bombardier.setItemInHand(InteractionHand.MAIN_HAND, bombardierSample);
        bombardier.getInventory().add(new ItemStack(scyphus, 5));
        h.assertTrue(PhlegethonticCommission.report(bombardier, alchemist),
                "Valid Bombardier sample was refused");
        alchemist.discard(); vicar.discard(); h.succeed();
    }

    @GameTest(template = "empty") public static void vagrantMindDiscoverySupportsTwoIndependentReports(GameTestHelper h) {
        var end = h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END);
        h.assertTrue(end != null, "End is unavailable for the Vagrant Mind inquiry test");
        var p = player(h, 6, end);
        end.getChunkAt(p.blockPosition());
        HemoCapabilityAccess.requireBloodVolume(p).setActive(true);
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(end);
        var alchemist = EntityInit.harbinger_alchemist.get().create(end);
        mnemonist.setPos(p.position()); alchemist.setPos(p.position());
        end.addFreshEntity(mnemonist); end.addFreshEntity(alchemist);
        com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.observeMind(p);
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(p)
                .mindVisited(), "Being anywhere in the End counted as entering the Mind");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(5);
        h.assertTrue(mnemonist.progressionDialogue(p).getNode("vagrant_memory_inquiry") == null
                        && alchemist.progressionDialogue(p).getNode("vagrant_biology_inquiry") == null,
                "Degree 5 saw the Degree 6 Mind inquiries");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(6);
        h.assertTrue(mnemonist.progressionDialogue(p).getNode("vagrant_memory_inquiry") != null,
                "Mnemonist does not offer the D6 Mind inquiry");
        h.assertTrue(alchemist.progressionDialogue(p).getNode("vagrant_biology_inquiry") != null,
                "Alchemist does not offer the D6 biology inquiry");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportMemory(p, mnemonist), "Mind report accepted without a visit");
        var data = p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        var inquiry = new CompoundTag();
        inquiry.putBoolean("MindVisited", true);
        data.put("hemomancy:vagrant_mind_inquiry", inquiry);
        p.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportBiology(p, alchemist), "Biology report accepted without an observation");
        var chorus = p.blockPosition().offset(2, 0, 0);
        end.setBlockAndUpdate(chorus.below(), Blocks.END_STONE.defaultBlockState());
        end.setBlockAndUpdate(chorus, Blocks.CHORUS_FLOWER.defaultBlockState());
        com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.observeBiology(p);
        h.assertTrue(com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(p)
                .biologyObserved(), "Nearby End chorus was not recorded as biology evidence");
        h.assertTrue(DialogueOptionPacket.dispatch(p, "mnemonist_vagrant_memory_report", alchemist.getId()) == null,
                "Alchemist dispatched the Mnemonist's report");
        h.assertTrue(DialogueOptionPacket.dispatch(p, "alchemist_vagrant_biology_report", mnemonist.getId()) == null,
                "Mnemonist dispatched the Alchemist's report");
        p.getInventory().setItem(9, new ItemStack(ItemInit.naeglerophaeon_ganglion.get()));
        var memoryReport = DialogueOptionPacket.dispatch(p, "mnemonist_vagrant_memory_report", mnemonist.getId());
        h.assertTrue(memoryReport != null && memoryReport.wasRewardDelivered(),
                "Mnemonist refused the shared Mind discovery");
        var biologyReport = DialogueOptionPacket.dispatch(p, "alchemist_vagrant_biology_report", alchemist.getId());
        h.assertTrue(biologyReport != null && biologyReport.wasRewardDelivered(),
                "Alchemist refused the same discovery plus Choir observation");
        var progress = com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(p);
        h.assertTrue(progress.memoryReported() && progress.biologyReported(),
                "The two reports did not persist independently");
        h.assertTrue(p.getInventory().countItem(ItemInit.naeglerophaeon_ganglion.get()) == 1,
                "The paired reports consumed the unique ganglion");
        var restored = player(h, 6, end);
        restored.getPersistentData().put(Player.PERSISTED_NBT_TAG,
                p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).copy());
        var saved = com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.progress(restored);
        h.assertTrue(saved.mindVisited() && saved.biologyObserved()
                        && saved.memoryReported() && saved.biologyReported(),
                "The shared discovery or paired reports did not survive saved-data restoration");
        h.assertTrue(!com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportMemory(p, mnemonist) && !com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry
                .reportBiology(p, alchemist), "A completed report was accepted twice");
        mnemonist.discard(); alchemist.discard(); h.succeed();
    }

    @GameTest(template = "empty") public static void silentArchonBonusWaitsForProvenVictory(GameTestHelper h) {
        var p = player(h, 7);
        var degree = HemoCapabilityAccess.requireInitiatoryDegree(p);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_PENDING);
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.claimedWillBonusCap(p) == 0,
                "Pending refusal received the Silent Archon summon cap");
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.SILENT_ARCHON);
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.claimedWillBonusCap(p) == 1,
                "Proven Silent Archon did not receive the configured summon cap");
        degree.setDegreeNumber(8);
        degree.setArchonPath(com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath.APOTHEOS);
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.summon.BoundSummonBehavior.claimedWillBonusCap(p) == 0,
                "Apotheos retained the Silent Archon summon cap");
        h.succeed();
    }

    @GameTest(template = "empty") public static void deadOrdinaryRecruitReleasesProfessionAndOutpost(GameTestHelper h) {
        var p = player(h, 6);
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(h.getLevel().getServer().overworld());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Dead recruit", p.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        var npc = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(npc != null, "Vicar could not be created");
        npc.setPos(h.absolutePos(new BlockPos(2, 2, 2)).getCenter());
        npc.setNoAi(true);
        h.assertTrue(h.getLevel().addFreshEntity(npc), "Vicar could not be spawned");
        var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType());
        String outpost = "test:ordinary_recruit_outpost";
        data.addNpcMember(line.getBloodlineUUID(), npc.getUUID(), type, outpost);
        line.contributeBlood(line.getMaxBloodVolume());
        npc.kill();
        h.assertTrue(!line.hasNpcMember(npc.getUUID()), "Dead ordinary recruit retained its membership");
        h.assertTrue(!line.hasNpcMemberType(type) && !line.hasNpcMemberOutpost(outpost),
                "Dead ordinary recruit still reserved its profession or outpost");
        h.assertTrue(line.getBloodVolume() <= line.getMaxBloodVolume(),
                "Recruit death left the pool above its reduced capacity");
        var restored = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline
                .deserialize(line.serialize());
        h.assertTrue(!restored.hasNpcMember(npc.getUUID()) && !restored.hasNpcMemberType(type)
                        && !restored.hasNpcMemberOutpost(outpost), "Dead recruit returned after saved-data restoration");
        h.assertTrue(line.addNpcMember(UUID.randomUUID(), type, outpost),
                "A replacement could not join the same profession and outpost");
        data.disbandBloodline(line.getBloodlineUUID());
        npc.discard();
        p.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void absentOrCancelledDeathRecruitKeepsMembership(GameTestHelper h) {
        var p = player(h, 6);
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(h.getLevel().getServer().overworld());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Absent recruit", p.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        var npc = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(npc != null, "Vicar could not be created");
        data.addNpcMember(line.getBloodlineUUID(), npc.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType()));
        var death = new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(
                npc, h.getLevel().damageSources().genericKill());
        death.setCanceled(true);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(death);
        h.assertTrue(line.hasNpcMember(npc.getUUID()), "Cancelled death released a living recruit");
        npc.discard();
        h.assertTrue(line.hasNpcMember(npc.getUUID()), "Entity removal released an absent recruit");
        data.disbandBloodline(line.getBloodlineUUID());
        p.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void recruitedOutpostNpcCanHoldVigilStationWithoutSuccession(GameTestHelper h) {
        var p = player(h, 6);
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(h.getLevel().getServer().overworld());
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Vigil helper", p.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(line);
        var recipeId = Hemomancy.rloc("cardinal_rite/covenant_vigil");
        var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe
                .getRiteByLocation(h.getLevel(), recipeId);
        h.assertTrue(recipe != null, "Covenant Vigil recipe is missing");
        BlockPos center = h.absolutePos(new BlockPos(6, 3, 6));
        var rite = com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite.interactive(
                p.getUUID(), center, recipeId, 1200, 5, 6, false, 0);
        for (int i = 0; i < rite.getAnchorBloodMl().length; i++) rite.fillAnchor(i, 50);
        h.assertTrue(rite.enterInscription(), "Vigil did not enter helper assignment phase");
        BlockPos station = center.offset(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .markers(recipe).get(com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR));
        h.getLevel().setBlock(station.below(), Blocks.STONE.defaultBlockState(), 3);
        h.getLevel().setBlock(station, Blocks.AIR.defaultBlockState(), 3);
        h.getLevel().setBlock(station.above(), Blocks.AIR.defaultBlockState(), 3);
        var npc = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(npc != null, "Vicar helper could not be created");
        npc.setPos(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
        npc.setNoAi(true);
        h.assertTrue(h.getLevel().addFreshEntity(npc), "Vicar helper could not be spawned");
        h.assertTrue(!npc.isSuccessor(), "Test NPC unexpectedly entered Succession");
        h.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryAssignNpc(h.getLevel(), p, rite, npc), "Unrecruited outpost Vicar was accepted");
        var updated = data.addNpcMember(line.getBloodlineUUID(), npc.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(npc.getType()));
        h.assertTrue(updated != null, "Recruitment fixture failed");
        HemoCapabilityAccess.requireBloodVolume(p).setBloodLine(updated);
        h.assertTrue(h.getLevel().hasChunkAt(station), "Vigil station chunk was not loaded");
        h.assertTrue(h.getLevel().getBlockState(station.below()).isFaceSturdy(
                h.getLevel(), station.below(), net.minecraft.core.Direction.UP),
                "Vigil station lacked sturdy support");
        h.assertTrue(h.getLevel().noCollision(npc, npc.getBoundingBox()),
                "Vigil station collided with the recruited Vicar");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryAssignNpc(h.getLevel(), p, rite, npc), "Recruited outpost Vicar could not take the station");
        h.assertTrue(rite.getAllyRoles().containsKey(npc.getUUID()),
                "Recruited Vicar passed interaction but was not assigned to a role");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .isAvailable(h.getLevel(), rite, npc.getUUID()),
                "Recruited Vicar did not count as a live helper at " + npc.blockPosition()
                        + " for marker " + station);
        npc.discard();
        h.assertTrue(!com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .hasRequiredHelpers(h.getLevel(), rite), "Lost Vicar still satisfied the Vigil helper requirement");
        var guest = player(h, 5);
        data.addMember(line.getBloodlineUUID(), guest.getUUID());
        guest.setPos(station.getCenter());
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryClaimPlayerRole(h.getLevel(), guest, rite, station), "Guest could not claim the lost helper's station");
        h.assertTrue(rite.getAllyRoles().get(guest.getUUID())
                        == com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.ANCHOR
                        && !rite.getAllyRoles().containsKey(npc.getUUID()),
                "Lost helper kept its station when a bloodline player claimed it");
        rite.removeAlly(guest.getUUID());
        data.removeNpcMember(line.getBloodlineUUID(), npc.getUUID());
        for (var role : com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole.values()) {
            var missing = UUID.randomUUID();
            data.addNpcMember(line.getBloodlineUUID(), missing);
            rite.assignAlly(missing, role);
        }
        var replacement = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(replacement != null, "Replacement Vicar could not be created");
        replacement.setPos(station.getX() + 0.5, station.getY(), station.getZ() + 0.5);
        replacement.setNoAi(true);
        h.assertTrue(h.getLevel().addFreshEntity(replacement), "Replacement Vicar could not be spawned");
        h.assertTrue(data.addNpcMember(line.getBloodlineUUID(), replacement.getUUID(),
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(replacement.getType())) != null,
                "Replacement Vicar could not join the bloodline");
        h.assertTrue(com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .tryAssignNpc(h.getLevel(), p, rite, replacement), "Replacement Vicar interaction was rejected");
        h.assertTrue(rite.getAllyRoles().containsKey(replacement.getUUID()),
                "Missing NPC reservations blocked a legitimate replacement at full quota");
        com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService
                .returnNpcAlliesToFane(h.getLevel(), rite);
        h.assertTrue(rite.getAllyRoles().isEmpty(), "Lost original helper retained an assignment after cleanup");
        replacement.discard();
        guest.discard();
        h.succeed();
    }

    @GameTest(template = "empty") public static void ledgerSummonUsesOriginalAndNeverInventsMissingRecruit(GameTestHelper h) {
        var outpost = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .createOutpostKey(Hemomancy.rloc("test_dimension"),
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(-17, 60, 32, 30, 90, 79));
        var origin = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .outpostOrigin(outpost).orElseThrow();
        h.assertTrue(origin.minChunkX() == -2 && origin.maxChunkX() == 1
                        && origin.minChunkZ() == 2 && origin.maxChunkZ() == 4,
                "Saved outpost bounds did not resolve to their covered chunks");
        h.assertTrue(com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .outpostOrigin("minecraft:overworld|harbinger_outpost|0,0,0|9999,0,9999").isEmpty(),
                "Unbounded outpost origin could force-load arbitrary chunks");
        var p = player(h, 6);
        setServerPlayerLookup(p, true);
        h.assertTrue(p.server.getPlayerList().getPlayer(p.getUUID()) == p,
                "Ledger fixture was not visible in the server player lookup");
        var overworld = h.getLevel().getServer().overworld();
        var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
                .get(overworld);
        var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
                "Ledger summon", p.getUUID(), UUID.randomUUID(), new java.util.ArrayList<>());
        data.registerBloodline(line);
        var volume = HemoCapabilityAccess.requireBloodVolume(p);
        volume.setBloodLine(line);
        com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData.get(h.getLevel())
                .consecrateHeart(p.getUUID(), p.blockPosition());
        var vicar = EntityInit.harbinger_vicar.get().create(h.getLevel());
        h.assertTrue(vicar != null, "Recruited Vicar could not be created");
        vicar.setPos(p.getX() + 2, p.getY() + 25, p.getZ() + 2);
        vicar.setNoAi(true);
        vicar.addTag("original-ledger-recruit");
        h.assertTrue(h.getLevel().addFreshEntity(vicar), "Recruited Vicar could not be spawned");
        UUID vicarId = vicar.getUUID();
        UUID missingId = UUID.randomUUID();
        line.addNpcMember(vicarId);
        BlockPos remote = p.blockPosition().offset(1024, 0, 1024);
        String remoteOutpost = com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerRecruitmentRules
                .createOutpostKey(h.getLevel().dimension().location(),
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(
                                remote.getX(), remote.getY(), remote.getZ(),
                                remote.getX(), remote.getY(), remote.getZ()));
        line.addNpcMember(missingId, null, remoteOutpost);
        h.assertTrue(!h.getLevel().hasChunkAt(remote), "Remote outpost chunk was already loaded");
        h.runAfterDelay(2, () -> {
            h.assertTrue(h.getLevel().getEntity(vicarId) == vicar, "Original Vicar was not loaded");
            invokeLedgerSummon(p, volume);
            var arrived = h.getLevel().getEntity(vicarId);
            h.assertTrue(arrived == vicar && arrived.getType() == vicar.getType()
                            && arrived.getTags().contains("original-ledger-recruit")
                            && arrived.distanceTo(p) < 8.0,
                    "Ledger did not bring the original recruited Vicar to the Fane");
            h.assertTrue(h.getLevel().getEntity(missingId) == null,
                    "Ledger manufactured a replacement for a missing recruited NPC");
            h.assertTrue(h.getLevel().hasChunkAt(remote),
                    "Ledger did not inspect the recorded outpost chunk for an unloaded recruit");
            var late = EntityInit.harbinger_vicar.get().create(h.getLevel());
            h.assertTrue(late != null, "Delayed outpost Vicar could not be created");
            late.setUUID(missingId);
            late.setPos(p.getX() + 2, p.getY() + 25, p.getZ() + 2);
            late.setNoAi(true);
            late.setNoGravity(true);
            late.addTag("late-ledger-recruit");
            h.assertTrue(h.getLevel().addFreshEntity(late), "Delayed outpost Vicar could not be spawned");
            h.runAfterDelay(5, () -> {
                var delayedArrival = h.getLevel().getEntity(missingId);
                h.assertTrue(delayedArrival == late && delayedArrival.getTags().contains("late-ledger-recruit")
                                && delayedArrival.distanceTo(p) < 8.0,
                        "Ledger did not finish the pending summon when the original NPC loaded");
                UUID cancelledId = UUID.randomUUID();
                h.assertTrue(line.removeNpcMember(missingId), "Completed recruit could not leave the test bloodline");
                h.assertTrue(line.addNpcMember(cancelledId, null, remoteOutpost),
                        "Cancellation recruit was not added to the bloodline");
                invokeLedgerSummon(p, volume);
                p.setPos(p.getX() + 100, p.getY(), p.getZ());
                var cancelled = EntityInit.harbinger_vicar.get().create(h.getLevel());
                h.assertTrue(cancelled != null, "Cancellation Vicar could not be created");
                cancelled.setUUID(cancelledId);
                cancelled.setPos(arrived.getX(), arrived.getY() + 25, arrived.getZ());
                cancelled.setNoAi(true);
                cancelled.setNoGravity(true);
                h.assertTrue(h.getLevel().addFreshEntity(cancelled), "Cancellation Vicar could not be spawned");
                Vec3 originalPosition = cancelled.position();
                h.runAfterDelay(3, () -> {
                    h.assertTrue(cancelled.position().distanceToSqr(originalPosition) < 1.0,
                            "Pending ledger summon moved an NPC after the player left the Fane");
                    arrived.discard();
                    delayedArrival.discard();
                    cancelled.discard();
                    data.disbandBloodline(line.getBloodlineUUID());
                    com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData.get(h.getLevel())
                            .remove(p.getUUID());
                    setServerPlayerLookup(p, false);
                    h.succeed();
                });
            });
        });
    }

    private static void invokeLedgerSummon(ServerPlayer player,
            com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume volume) {
        try {
            var summon = com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction.class
                    .getDeclaredMethod("handleNpcSummon", ServerPlayer.class,
                            com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume.class);
            summon.setAccessible(true);
            summon.invoke(null, player, volume);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not invoke ledger summon", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void setServerPlayerLookup(ServerPlayer player, boolean present) {
        try {
            var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
            field.setAccessible(true);
            var players = (java.util.Map<UUID, ServerPlayer>) field.get(player.server.getPlayerList());
            if (present) players.put(player.getUUID(), player);
            else players.remove(player.getUUID(), player);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not register ledger GameTest player", e);
        }
    }

    @GameTest(template = "empty") public static void personalExaminationCountsUniqueSourcesAndUnlocksOrderedCollection(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        state.learn(MICROSCOPE);
        var pig = vial("minecraft:pig");
        examine(h, p, pig);
        h.assertTrue(state.sourceCount() == 1, "First successful examination was not recorded");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, INJECTION), "Examined sample did not qualify for injection lesson");
        examine(h, p, pig.copy());
        h.assertTrue(state.sourceCount() == 1, "Duplicate source advanced count");
        h.assertTrue(!ClinicalBloodKnowledge.canLearn(p, CABINET), "Cabinet unlocked at first source");
        var cow = vial("minecraft:cow"); BloodSampleData.identify(cow);
        p.getInventory().add(cow.copy());
        h.assertTrue(state.sourceCount() == 1, "Traded identification granted personal proof");
        examine(h, p, cow);
        examine(h, p, vial("minecraft:sheep"));
        h.assertTrue(state.sourceCount() == 3 && ClinicalBloodKnowledge.teach(p, alchemist, CABINET), "Third source did not unlock Cabinet lesson");
        h.assertTrue(ClinicalBloodKnowledge.recipeVisible(p, Hemomancy.rloc("phlebotomists_cabinet")), "Known Cabinet recipe hidden");
        h.assertTrue(!ClinicalBloodKnowledge.recipeVisible(p, Hemomancy.rloc("phlebotomists_field_case")), "Untaught Field Case recipe exposed");
        h.succeed();
    }

    @GameTest(template = "empty") public static void cabinetProofComesFromSuccessfulPlayerTransfers(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        state.learn(CABINET);
        h.setBlock(new BlockPos(0, 2, 0), BlockInit.phlebotomists_cabinet.get());
        var be = (PhlebotomistsCabinetBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(new BlockPos(0, 2, 0)));
        var menu = new PhlebotomistsCabinetMenu(1, p.getInventory(), be); p.containerMenu = menu;
        menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(!state.cabinetWithdrawn, "Empty withdrawal counted");
        menu.setCarried(new ItemStack(Items.STONE)); menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(!state.cabinetInserted, "Rejected item counted as specimen storage");
        menu.setCarried(vial("minecraft:pig")); menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(state.cabinetInserted && !state.cabinetWithdrawn, "Successful insertion was not recorded");
        menu.clicked(0, 0, ClickType.PICKUP, p);
        h.assertTrue(state.cabinetWithdrawn, "Successful withdrawal was not recorded");
        h.assertTrue(!state.canLearn(FIELD_REFERRAL, 2), "Missing craft and iron proof bypassed referral");
        ClinicalBloodKnowledge.crafted(new PlayerEvent.ItemCraftedEvent(p, new ItemStack(BlockInit.phlebotomists_cabinet.get()), new net.minecraft.world.SimpleContainer(9)));
        state.hematicIronObtained = true;
        ClinicalBloodKnowledge.met(p, "artificer");
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        var artificer = EntityInit.harbinger_artificer.get().create(h.getLevel()); artificer.setPos(p.position());
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, artificer, FIELD_CASE), "Field Case bypassed Alchemist handoff");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, FIELD_REFERRAL), "Qualified referral denied");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, artificer, FIELD_CASE), "Artificer did not teach referred field conversion");
        menu.removed(p);
        h.succeed();
    }

    @GameTest(template = "empty") public static void mnemonicInquiryHintsUntilReferredFormalStudy(GameTestHelper h) {
        var p = player(h, 2);
        var state = HemoCapabilityAccess.clinicalBlood(p);
        var machine = new ItemStack(BlockInit.clairaudiograph.get());
        var alchemist = EntityInit.harbinger_alchemist.get().create(h.getLevel()); alchemist.setPos(p.position());
        var mnemonist = EntityInit.harbinger_mnemonist.get().create(h.getLevel()); mnemonist.setPos(p.position());
        ClinicalBloodKnowledge.met(p, "mnemonist");
        h.assertTrue(ClinicalBloodDialogue.inquiry(p, "mnemonist", machine).orElseThrow()
                .contains("hemomancy.clinical.inquiry.echo_hint"), "Untaught mnemonic inquiry leaked full workflow");
        for (String source : new String[]{"minecraft:pig", "minecraft:cow", "minecraft:sheep"}) state.recordExamination(source);
        h.assertTrue(ClinicalBloodKnowledge.teach(p, alchemist, ECHO_REFERRAL), "D2 echo referral denied");
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, mnemonist, CLAIRAUDIOGRAPH), "Formal study unlocked before D3");
        HemoCapabilityAccess.requireInitiatoryDegree(p).setDegreeNumber(3);
        h.assertTrue(!ClinicalBloodKnowledge.teach(p, alchemist, CLAIRAUDIOGRAPH), "Alchemist usurped mnemonic teaching");
        h.assertTrue(ClinicalBloodKnowledge.teach(p, mnemonist, CLAIRAUDIOGRAPH), "Qualified mnemonic lesson denied");
        h.assertTrue(p.getRecipeBook().contains(Hemomancy.rloc("wax_cylinder")), "Ordinary cylinder recipe missing");
        h.assertTrue(ClinicalBloodDialogue.inquiry(p, "mnemonist", machine).orElseThrow()
                .contains("hemomancy.clinical.clairaudiograph.lesson"), "Taught inquiry did not show workflow");
        h.succeed();
    }

    @GameTest(template = "empty") public static void injectionRequiresKnowledgeAndInterruptedStudyGrantsNothing(GameTestHelper h) {
        var p = player(h, 1);
        var sample = vial("minecraft:pig");
        p.setItemInHand(InteractionHand.MAIN_HAND, sample);
        sample.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        h.assertTrue(!p.isUsingItem() && BloodSampleData.isFilled(sample), "Untaught injection consumed or started");
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.hematic_microscope.get()));
        p.setItemInHand(InteractionHand.OFF_HAND, sample);
        p.getMainHandItem().use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < 20; i++) p.doTick();
        p.releaseUsingItem();
        for (int i = 0; i < 30; i++) p.doTick();
        h.assertTrue(HemoCapabilityAccess.clinicalBlood(p).sourceCount() == 0, "Interrupted examination granted proof");
        h.assertTrue(!BloodSampleData.identified(sample), "Interrupted examination identified blood");
        h.succeed();
    }
}
