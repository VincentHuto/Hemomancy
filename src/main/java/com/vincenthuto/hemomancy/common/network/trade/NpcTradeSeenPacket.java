package com.vincenthuto.hemomancy.common.network.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCards;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the player opened the Trade box of speaker {@code entityId}, so today's daily cards there count as
 * seen ({@link NpcTradeCards#markRotationSeen}) and stop showing the unread dot.
 */
public record NpcTradeSeenPacket(int entityId) implements CustomPacketPayload {
	public static final Type<NpcTradeSeenPacket> TYPE = new Type<>(Hemomancy.rloc("npc_trade_seen"));
	public static final StreamCodec<FriendlyByteBuf, NpcTradeSeenPacket> STREAM_CODEC =
			StreamCodec.of(NpcTradeSeenPacket::encode, NpcTradeSeenPacket::decode);

	private static void encode(FriendlyByteBuf buf, NpcTradeSeenPacket packet) {
		buf.writeVarInt(packet.entityId);
	}

	private static NpcTradeSeenPacket decode(FriendlyByteBuf buf) {
		return new NpcTradeSeenPacket(buf.readVarInt());
	}

	public static void handle(NpcTradeSeenPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)) return;
			NpcTradeSpeaker speaker = NpcTradeSpeaker.near(player, packet.entityId, NpcTradeRequestPacket.REACH);
			if (speaker == null || !speaker.tradesWith(player)) return;
			NpcTradeCards.markRotationSeen(player, speaker.tradeSpeakerId());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
