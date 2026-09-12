package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.ManipulationChargeVisualPacket;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ManipulationChargeVisualGameTests {
    private ManipulationChargeVisualGameTests() {}

    @GameTest(templateNamespace = "minecraft", template = "bastion/mobs/empty", batch = "manipulation_charge")
    public static void savedSelectionsKeepPreviewsThroughTheirRegisteredCharge(GameTestHelper helper) {
        verify(helper.getLevel(), helper.absoluteVec(new Vec3(2, 2, 2)));
        helper.succeed();
    }

    /** Also run by the disposable client review, against the same packet handler. */
    static int verify(ServerLevel level, Vec3 position) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "charge-preview-test"), false);
        var player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        var channel = new EmbeddedChannel(connection);
        var packets = new ArrayList<ManipulationVisualPacket>();
        new ServerGamePacketListenerImpl(level.getServer(), connection, player, cookie) {
            @Override public void send(Packet<?> packet) {
                if (packet instanceof ClientboundCustomPayloadPacket custom
                        && custom.payload() instanceof ManipulationVisualPacket visual) packets.add(visual);
            }
        };
        player.setPos(position);
        var players = mutablePlayers(level);
        players.add(player);
        try {
            var blood = HemoCapabilityAccess.requireBloodVolume(player);
            blood.setActive(true);
            blood.setBloodVolume(2000);
            for (var school : EnumBloodTendency.values())
                HemoCapabilityAccess.getBloodTendency(player).orElseThrow().setTendencyAlignment(school, 100);
            var known = HemoCapabilityAccess.requireKnownManipulations(player);
            for (var entry : ManipulationInit.MANIPS.getEntries())
                known.getKnownManips().put(entry.get(), new ManipLevel(4, 185));
            var context = new ServerPayloadContext(player.connection, ManipulationChargeVisualPacket.TYPE.id());
            int tested = 0;
            for (var entry : ManipulationInit.MANIPS.getEntries()) {
                var registered = entry.get();
                if (registered.getType() != EnumManipulationType.CHARGED) continue;
                var form = ManipulationVisuals.chargeForm(registered.getName());
                require(form != null, registered.getName() + " has no charge preview");
                // This is the state restored from a save or selected from a loaded memory list.
                known.setSelectedManip(BloodManipulation.deserialize(registered.serialize()));
                known.setEquippedManipNames(List.of(registered.getName()));
                int duration = registered.getRequiredChargeTicks();
                for (int held : new int[]{4, duration / 2, duration - 4, duration, 0}) {
                    packets.clear();
                    player.getPersistentData().remove("hemomancy:charge_visual_tick");
                    ManipulationChargeVisualPacket.handle(new ManipulationChargeVisualPacket(held), context);
                    require(packets.size() == 1, registered.getName() + " lost its preview at " + held + "/" + duration);
                    var preview = packets.getFirst();
                    require(preview.form() == form && preview.entityId() == player.getId(), "Preview changed source or form");
                    require(Math.abs(preview.radius() - ManipulationCastingRules.chargeFraction(held, duration)) < .0001,
                            registered.getName() + " preview progress disagrees with the gauge at " + held + "/" + duration);
                    require(preview.ticks() == (held == 0 ? 0 : 10), "Preview lost its refresh or release lifetime");
                }
                packets.clear();
                ManipulationChargeVisualPacket.handle(new ManipulationChargeVisualPacket(duration + 1), context);
                ManipulationChargeVisualPacket.handle(new ManipulationChargeVisualPacket(-1), context);
                require(packets.isEmpty(), "Invalid charge duration emitted a preview");
				require(registered.getRemainingCooldownTicks(player) == 0L,
						registered.getName() + " preview started a gameplay cooldown");
                tested++;
            }
            require(tested == 16, "Expected coverage of all 16 registered charged manipulations, got " + tested);
			require(blood.getBloodVolume() == 2000, "Preview spent blood");
            return tested;
        } finally {
            players.remove(player);
            player.discard();
            channel.finishAndReleaseAll();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    private static List<ServerPlayer> mutablePlayers(ServerLevel level) {
        try {
            var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("players");
            field.setAccessible(true);
            return (List<ServerPlayer>) field.get(level.getServer().getPlayerList());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not register the preview packet observer", exception);
        }
    }
}
