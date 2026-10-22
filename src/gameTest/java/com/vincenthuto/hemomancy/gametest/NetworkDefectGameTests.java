package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketUpdatePoolSettings;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Serverbound payloads refuse what a modified client could otherwise make them do (audit section F). */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NetworkDefectGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private NetworkDefectGameTests() {}

    /** A server player whose every outgoing packet, with or without a send listener, lands in {@code sent}. */
    private static ServerPlayer connectedPlayer(GameTestHelper helper, List<Packet<?>> sent) {
        return connectedPlayer(helper, sent, () -> false);
    }

    /**
     * As above, with operator status read from {@code operator}. The GameTest server grants operators permission
     * level 0, so tests cannot op a fixture player through the player list.
     */
    private static ServerPlayer connectedPlayer(GameTestHelper helper, List<Packet<?>> sent, BooleanSupplier operator) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),
                "net_" + UUID.randomUUID().toString().substring(0, 8)), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
                cookie.clientInformation()) {
            @Override public boolean hasPermissions(int level) {
                return operator.getAsBoolean() || super.hasPermissions(level);
            }
        };
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(Packet<?> packet) { sent.add(packet); }
            @Override public void send(Packet<?> packet, @Nullable PacketSendListener listener) { sent.add(packet); }
        };
        // Not added to the level: a live player would change nearby-player logic in tests running alongside.
        player.setPos(helper.absoluteVec(new Vec3(2, 2, 2)));
        return player;
    }

    /** F-8: non-finite bloodline pool settings are rejected instead of slipping past the min/max clamps. */
    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20, batch = "network_defects")
    public static void poolSettingsRejectNonFiniteValues(GameTestHelper helper) {
        var player = connectedPlayer(helper, new ArrayList<>());
        try {
            var volume = HemoCapabilityAccess.requireBloodVolume(player);
            volume.setTrickleRate(5.0D);
            volume.setAutoDrawThreshold(0.5D);
            var context = new ServerPayloadContext(player.connection, PacketUpdatePoolSettings.TYPE.id());

            PacketUpdatePoolSettings.handle(new PacketUpdatePoolSettings(true, Double.NaN, true, 0.25D), context);
            PacketUpdatePoolSettings.handle(new PacketUpdatePoolSettings(true, 2.0D, true, Double.POSITIVE_INFINITY), context);
            helper.assertTrue(volume.getTrickleRate() == 5.0D && volume.getAutoDrawThreshold() == 0.5D,
                    "Non-finite pool settings must change nothing");

            PacketUpdatePoolSettings.handle(new PacketUpdatePoolSettings(true, 2.0D, true, 0.25D), context);
            helper.assertTrue(volume.getTrickleRate() == 2.0D && volume.getAutoDrawThreshold() == 0.25D,
                    "Finite pool settings still apply");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    /** F-9: pasting a structure needs creative mode, operator permission and the Structure Spawner in hand. */
    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20, batch = "network_defects")
    public static void structurePasteRequiresOperatorAndHeldSpawner(GameTestHelper helper) {
        List<Packet<?>> sent = new ArrayList<>();
        boolean[] operator = {false};
        var player = connectedPlayer(helper, sent, () -> operator[0]);
        try {
            player.setGameMode(GameType.CREATIVE);
            var context = new ServerPayloadContext(player.connection, PlaceStructurePacket.TYPE.id());
            // An unknown floor only answers "could not find", so a run is visible without placing anything.
            var packet = new PlaceStructurePacket(Hemomancy.rloc("not_a_floor"), PlaceStructurePacket.StructureType.RITE_FLOOR, false);

            PlaceStructurePacket.handle(packet, context);
            helper.assertTrue(chatCount(sent) == 1 && !player.hasPermissions(2),
                    "A creative player without operator permission is refused with a message");

            operator[0] = true;
            PlaceStructurePacket.handle(packet, context);
            helper.assertTrue(chatCount(sent) == 1, "An operator without the spawner in hand gets nothing: " + chats(sent));

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.structure_spawner.get()));
            PlaceStructurePacket.handle(packet, context);
            helper.assertTrue(chatCount(sent) == 2, "An operator holding the spawner reaches the paste");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    private static List<String> chats(List<Packet<?>> sent) {
        return sent.stream().filter(ClientboundSystemChatPacket.class::isInstance)
                .map(packet -> ((ClientboundSystemChatPacket) packet).content().getString()).toList();
    }

    private static long chatCount(List<Packet<?>> sent) {
        return sent.stream().filter(ClientboundSystemChatPacket.class::isInstance).count();
    }

    /** F-6: ledger actions run only while the sender holds the ledger. */
    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20, batch = "network_defects")
    public static void ledgerActionsRequireTheLedgerInHand(GameTestHelper helper) {
        List<Packet<?>> sent = new ArrayList<>();
        var player = connectedPlayer(helper, sent);
        try {
            var context = new ServerPayloadContext(player.connection, PacketLedgerAction.TYPE.id());
            PacketLedgerAction.handle(new PacketLedgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT), context);
            helper.assertTrue(sent.stream().noneMatch(ClientboundSystemChatPacket.class::isInstance),
                    "A ledger action without the ledger must do nothing");

            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.unsigned_ancestral_ledger.get()));
            PacketLedgerAction.handle(new PacketLedgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT), context);
            helper.assertTrue(sent.stream().anyMatch(ClientboundSystemChatPacket.class::isInstance),
                    "With the ledger in hand the action must run and report that the player has no bloodline");
            helper.succeed();
        } finally {
            player.discard();
        }
    }
}
