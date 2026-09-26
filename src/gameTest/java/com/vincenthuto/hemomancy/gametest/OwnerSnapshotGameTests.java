package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityRegistrar;
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
            var tendency = HemoCapabilityAccess.requireBloodTendency(player);
            var vascular = HemoCapabilityAccess.requireVascularSystem(player);
            HemoCapabilityRegistrar.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
            assertPair(helper, packets, "login");
            packets.clear();
            HemoCapabilityRegistrar.onDimensionChange(new PlayerEvent.PlayerChangedDimensionEvent(player, Level.OVERWORLD, Level.NETHER));
            assertPair(helper, packets, "dimension change");
            packets.clear();
            HemoCapabilityRegistrar.onRespawn(new PlayerEvent.PlayerRespawnEvent(player, false));
            assertPair(helper, packets, "respawn");
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

    private static void assertPair(GameTestHelper helper, List<CustomPacketPayload> packets, String phase) {
        helper.assertTrue(packets.size() == 2, phase + " sent " + packets.size() + " owner packets");
        helper.assertTrue(packets.get(0) instanceof BloodTendencyServerPacket, phase + " missed tendency first");
        helper.assertTrue(packets.get(1) instanceof VascularSystemServerPacket, phase + " missed vascular second");
    }
}
