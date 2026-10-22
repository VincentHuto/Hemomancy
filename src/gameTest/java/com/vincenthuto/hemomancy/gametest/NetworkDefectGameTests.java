package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Serverbound payloads refuse what a modified client could otherwise make them do (audit section F). */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NetworkDefectGameTests {
    private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

    private NetworkDefectGameTests() {}

    /** A server player whose every outgoing packet, with or without a send listener, lands in {@code sent}. */
    private static ServerPlayer connectedPlayer(GameTestHelper helper, List<Packet<?>> sent) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(),
                "net_" + UUID.randomUUID().toString().substring(0, 8)), false);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(),
                cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(Packet<?> packet) { sent.add(packet); }
            @Override public void send(Packet<?> packet, @Nullable PacketSendListener listener) { sent.add(packet); }
        };
        player.setPos(helper.absoluteVec(new Vec3(2, 2, 2)));
        helper.getLevel().addNewPlayer(player);
        return player;
    }

    /** F-6: ledger actions run only while the sender holds the ledger. */
    @GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 20)
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
