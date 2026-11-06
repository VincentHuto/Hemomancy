package com.vincenthuto.hemomancy.common.network.trade;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeSpeaker;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: trade on {@code offerId} with the speaker {@code entityId}, paying from main-inventory
 * {@code slots}. The server checks everything again ({@link NpcTradeService#attempt}) and answers with a
 * {@link NpcTradeResultPacket}.
 */
public record NpcTradeRequestPacket(int entityId, ResourceLocation offerId, int[] slots) implements CustomPacketPayload {
	/** Dialogue reach: the distance the dialogue packets accept. */
	public static final double REACH = 8.0D;

	public static final Type<NpcTradeRequestPacket> TYPE = new Type<>(Hemomancy.rloc("npc_trade_request"));
	public static final StreamCodec<FriendlyByteBuf, NpcTradeRequestPacket> STREAM_CODEC =
			StreamCodec.of(NpcTradeRequestPacket::encode, NpcTradeRequestPacket::decode);

	private static void encode(FriendlyByteBuf buf, NpcTradeRequestPacket packet) {
		buf.writeVarInt(packet.entityId);
		buf.writeResourceLocation(packet.offerId);
		buf.writeVarIntArray(packet.slots);
	}

	private static NpcTradeRequestPacket decode(FriendlyByteBuf buf) {
		return new NpcTradeRequestPacket(buf.readVarInt(), buf.readResourceLocation(),
				buf.readVarIntArray(NpcTradeRules.MAX_SLOTS));
	}

	public static void handle(NpcTradeRequestPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)) return;
			if (!ServerboundGuards.distinctSlots(packet.slots, player.getInventory().items.size())) return;
			NpcTradeSpeaker speaker = NpcTradeSpeaker.near(player, packet.entityId, REACH);
			if (speaker == null || !speaker.tradesWith(player)) return;
			NpcTradeService.attempt(player, speaker, packet.entityId, packet.offerId, packet.slots);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
