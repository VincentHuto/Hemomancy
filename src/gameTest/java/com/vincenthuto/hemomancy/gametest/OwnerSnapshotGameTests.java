package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityRegistrar;
import com.vincenthuto.hemomancy.common.capability.PlayerStateSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTemperamentServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.VascularSystemServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationUpkeep;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder("owner_snapshot_validation")
@PrefixGameTestTemplate(false)
public final class OwnerSnapshotGameTests {
    @GameTest(template = "empty", batch = "owner_snapshots")
    public static void degreeLifecycleSendsSavedPomeProgress(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "pome_sync_test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        var channel = new EmbeddedChannel(connection);
        List<CustomPacketPayload> packets = new ArrayList<>();
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof ClientboundCustomPayloadPacket custom) packets.add(custom.payload());
            }
        };
        try {
            var degree = (InitiatoryDegree) HemoCapabilityAccess.getInitiatoryDegree(player);
            degree.recordPomeConsumed(UUID.randomUUID());
            degree.incrementTotalPomesConsumed();
            var saved = degree.serializeNBT(player.registryAccess());
            degree.resetPomeCommunion();
            degree.deserializeNBT(player.registryAccess(), saved);
            HemoCapabilityRegistrar.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
            assertPomeProgress(helper, packets, 1, "login");
            packets.clear();
            HemoCapabilityRegistrar.onDimensionChange(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
            assertPomeProgress(helper, packets, 1, "dimension change");
            packets.clear();
            degree.setQliphothCommunionDone(true);
            HemoCapabilityRegistrar.onRespawn(new PlayerEvent.PlayerRespawnEvent(player, false));
            assertPomeProgress(helper, packets, 9, "completed Communion respawn");
            helper.succeed();
        } finally {
            player.discard();
            channel.finishAndReleaseAll();
        }
    }

    private static void assertPomeProgress(GameTestHelper helper, List<CustomPacketPayload> packets, int expected, String phase) {
        var progress = packets.stream().filter(PacketSyncPomeProgress.class::isInstance).toList();
        helper.assertTrue(progress.size() == 1, phase + " must send one pome progress packet, got " + progress.size());
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            PacketSyncPomeProgress.encode(buffer,
                    (PacketSyncPomeProgress) progress.getFirst());
            helper.assertTrue(buffer.readInt() == expected, phase + " sent incorrect saved pome progress");
        } finally {
            buffer.release();
        }
    }

    @GameTest(template = "empty", batch = "owner_snapshots")
    public static void lifecycleAndChangePathEachSendOneOwnerPacket(GameTestHelper helper) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "snapshot_test"), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        var channel = new EmbeddedChannel(connection);
        List<CustomPacketPayload> packets = new ArrayList<>();
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {
                if (packet instanceof ClientboundCustomPayloadPacket custom) packets.add(custom.payload());
            }
        };
        try {
            var temper = HemoCapabilityAccess.getBloodTemperament(player);
            var vascular = HemoCapabilityAccess.getVascularSystem(player);
            HemoCapabilityRegistrar.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
            assertOrderedPair(helper, packets, "login");
            packets.clear();
            HemoCapabilityRegistrar.onDimensionChange(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
            assertOrderedPair(helper, packets, "dimension change");
            packets.clear();
            HemoCapabilityRegistrar.onRespawn(new PlayerEvent.PlayerRespawnEvent(player, false));
            assertOrderedPair(helper, packets, "respawn");
            packets.clear();
            temper.addTemperAlignment(EnumBloodTemper.ANIMUS, 2f);
            vascular.setVascularSectionHealth(EnumVeinSections.ARMS, -3f);
            BloodTemperamentEvents.syncTemperament(player, temper);
            VascularSystemEvents.syncVascular(player, vascular);
            assertPair(helper, packets, "direct change-path sends");
            helper.succeed();
        } finally {
            player.discard();
            channel.finishAndReleaseAll();
        }
    }

    /** The lifecycle sends the whole sync table; temper and vascular each go once, temper first. */
    private static void assertOrderedPair(GameTestHelper helper, List<CustomPacketPayload> packets, String phase) {
        var owner = packets.stream()
                .filter(p -> p instanceof BloodTemperamentServerPacket || p instanceof VascularSystemServerPacket).toList();
        assertPair(helper, owner, phase);
    }

    /**
     * S-2: after login, respawn and dimension change, every table entry reaches the owner exactly once (equipment
     * slots once per slot), and only on the lifecycle events it lists.
     */
    @GameTest(template = "empty", batch = "owner_snapshots")
    public static void everySyncedAttachmentReachesTheOwnerOnEachLifecycle(GameTestHelper helper) {
        try (var captured = CapturedPlayer.create(helper)) {
            var player = captured.player;
            int slots = HemoCapabilityAccess.getEquipment(player).getSlots();
            HemoCapabilityRegistrar.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
            assertTable(helper, captured, PlayerStateSync.Lifecycle.LOGIN, slots);
            HemoCapabilityRegistrar.onRespawn(new PlayerEvent.PlayerRespawnEvent(player, false));
            assertTable(helper, captured, PlayerStateSync.Lifecycle.RESPAWN, slots);
            HemoCapabilityRegistrar.onDimensionChange(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
            assertTable(helper, captured, PlayerStateSync.Lifecycle.DIMENSION, slots);
            helper.assertTrue(PlayerStateSync.Entry.ICHORIAN_KNOWLEDGE.resendsOn(PlayerStateSync.Lifecycle.DIMENSION),
                    "F-20: Ichorian knowledge resends on a dimension change");
            helper.assertTrue(PlayerStateSync.Entry.SKILLS.resendsOn(PlayerStateSync.Lifecycle.RESPAWN)
                    && PlayerStateSync.Entry.SKILLS.resendsOn(PlayerStateSync.Lifecycle.DIMENSION),
                    "S-2b: skill progress resends on respawn and dimension change");
            helper.succeed();
        }
    }

    private static void assertTable(GameTestHelper helper, CapturedPlayer captured, PlayerStateSync.Lifecycle lifecycle,
            int slots) {
        for (var entry : PlayerStateSync.Entry.values()) {
            int sent = captured.sent(entry.payload()).size();
            int expected = !entry.resendsOn(lifecycle) ? 0 : entry == PlayerStateSync.Entry.EQUIPMENT_SLOTS ? slots : 1;
            helper.assertTrue(sent == expected,
                    lifecycle + " sent " + entry + " (" + entry.payload().getSimpleName() + ") " + sent + " times, not " + expected);
        }
        captured.payloads.clear();
    }

    private static void assertPair(GameTestHelper helper, List<CustomPacketPayload> packets, String phase) {
        helper.assertTrue(packets.size() == 2, phase + " sent " + packets.size() + " owner packets");
        helper.assertTrue(packets.get(0) instanceof BloodTemperamentServerPacket, phase + " missed temper first");
        helper.assertTrue(packets.get(1) instanceof VascularSystemServerPacket, phase + " missed vascular second");
    }

    /**
     * CQ-11: a known-manipulation save this build cannot read (keys from before the Temper rename) loads as an empty
     * selection. Login upkeep, the owner packet and its wire round trip must not throw, so the player is not kicked.
     */
    @GameTest(template = "empty", batch = "owner_snapshot_old_manipulation_save")
    public static void anUnreadableManipulationSaveLoadsWithoutKickingThePlayer(GameTestHelper helper) {
        try (var captured = CapturedPlayer.create(helper)) {
            var player = captured.player;
            var known = HemoCapabilityAccess.getKnownManipulations(player);
            var map = new java.util.LinkedHashMap<BloodManipulation, ManipLevel>();
            map.put(ManipulationInit.getByName("blood_shot"), new ManipLevel(2, 0));
            known.setKnownManips(map);
            known.setSelectedManip(map.keySet().iterator().next());
            var saved = known.serializeNBT(player.registryAccess());
            for (var element : saved) {
                var entry = (net.minecraft.nbt.CompoundTag) element;
                for (String key : entry.getAllKeys()) {
                    if ((key.equals("Selected") || key.startsWith("Manip")) && entry.get(key) instanceof net.minecraft.nbt.CompoundTag manip) {
                        manip.putString("tend" + "ency", manip.getString("temper"));
                        manip.remove("temper");
                    }
                }
            }
            known.deserializeNBT(player.registryAccess(), saved);
            helper.assertTrue(!known.getKnownManips().containsKey(null), "an unreadable entry was kept as a null key");
            KnownManipulationUpkeep.sanitizeKnownManipulations(known);
            helper.assertTrue(known.getSelectedManip() == BloodManipulation.BLANK,
                    "the selection should fall back to BLANK, got " + known.getSelectedManip().getName());
            var packet = new KnownManipulationServerPacket(known);
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                KnownManipulationServerPacket.STREAM_CODEC.encode(buffer, packet);
                KnownManipulationServerPacket.STREAM_CODEC.decode(buffer);
                helper.assertTrue(buffer.readableBytes() == 0, "the owner packet left " + buffer.readableBytes() + " unread bytes");
            } finally {
                buffer.release();
            }
            HemoCapabilityRegistrar.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
            helper.assertTrue(!captured.sent(KnownManipulationServerPacket.class).isEmpty(),
                    "login should still send the known-manipulation owner packet");
            helper.succeed();
        }
    }
}
