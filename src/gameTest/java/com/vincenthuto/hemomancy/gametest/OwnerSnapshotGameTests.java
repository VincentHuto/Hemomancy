package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityRegistrar;
import com.vincenthuto.hemomancy.common.capability.PlayerStateSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.BloodTendencyEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodTendencyServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.VascularSystemServerPacket;
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
            degree.recordPomeConsumed(UUID.randomUUID(), 0L);
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
            var tendency = HemoCapabilityAccess.getBloodTendency(player);
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
            tendency.addTendencyAlignment(EnumBloodTendency.ANIMUS, 2f);
            vascular.setVascularSectionHealth(EnumVeinSections.ARMS, -3f);
            BloodTendencyEvents.syncTendency(player, tendency);
            VascularSystemEvents.syncVascular(player, vascular);
            assertPair(helper, packets, "direct change-path sends");
            helper.succeed();
        } finally {
            player.discard();
            channel.finishAndReleaseAll();
        }
    }

    /** The lifecycle sends the whole sync table; tendency and vascular each go once, tendency first. */
    private static void assertOrderedPair(GameTestHelper helper, List<CustomPacketPayload> packets, String phase) {
        var owner = packets.stream()
                .filter(p -> p instanceof BloodTendencyServerPacket || p instanceof VascularSystemServerPacket).toList();
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
            helper.assertTrue(!PlayerStateSync.Entry.ICHORIAN_KNOWLEDGE.resendsOn(PlayerStateSync.Lifecycle.DIMENSION),
                    "Ichorian knowledge gains a dimension resend only through F-20");
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
        helper.assertTrue(packets.get(0) instanceof BloodTendencyServerPacket, phase + " missed tendency first");
        helper.assertTrue(packets.get(1) instanceof VascularSystemServerPacket, phase + " missed vascular second");
    }
}
