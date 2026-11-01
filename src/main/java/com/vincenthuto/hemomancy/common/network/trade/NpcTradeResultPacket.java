package com.vincenthuto.hemomancy.common.network.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeBoard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService.Outcome;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server to client: how a trade request ended, and the speaker's Trade box as it now stands (its cards, rapport and
 * tier). The client updates an open dialogue with {@code entityId} and ignores it otherwise.
 */
public record NpcTradeResultPacket(int entityId, ResourceLocation offerId, Outcome outcome, String reasonKey,
		NpcTradeBoard board) implements CustomPacketPayload {
	public static final Type<NpcTradeResultPacket> TYPE = new Type<>(Hemomancy.rloc("npc_trade_result"));
	public static final StreamCodec<FriendlyByteBuf, NpcTradeResultPacket> STREAM_CODEC =
			StreamCodec.of(NpcTradeResultPacket::encode, NpcTradeResultPacket::decode);

	private static void encode(FriendlyByteBuf buf, NpcTradeResultPacket packet) {
		buf.writeVarInt(packet.entityId);
		buf.writeResourceLocation(packet.offerId);
		buf.writeEnum(packet.outcome);
		buf.writeUtf(packet.reasonKey);
		packet.board.toNetwork(buf);
	}

	private static NpcTradeResultPacket decode(FriendlyByteBuf buf) {
		return new NpcTradeResultPacket(buf.readVarInt(), buf.readResourceLocation(), buf.readEnum(Outcome.class),
				buf.readUtf(), NpcTradeBoard.fromNetwork(buf));
	}

	public int rapport() {
		return board.rapport();
	}

	public int tier() {
		return board.tier();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
