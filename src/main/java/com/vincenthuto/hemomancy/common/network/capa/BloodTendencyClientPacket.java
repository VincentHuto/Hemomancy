package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class BloodTendencyClientPacket implements CustomPacketPayload {

	public static final Type<BloodTendencyClientPacket> TYPE = new Type<>(Hemomancy.rloc("blood_tendency_client_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodTendencyClientPacket> STREAM_CODEC = StreamCodec.of(BloodTendencyClientPacket::encode, BloodTendencyClientPacket::decode);

	public static BloodTendencyClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new BloodTendencyClientPacket();
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final BloodTendencyClientPacket msg) {

	}

	public static void handle(final BloodTendencyClientPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.player(); // the client that sent this packet
			if (sender != null) {
				IBloodTendency bloodTendency = HemoCapabilityAccess.getBloodTendency(sender)
						.orElseThrow(IllegalStateException::new);
				PacketHandler.sendToPlayer(sender, new BloodTendencyServerPacket(bloodTendency.getTendency()));
			}
		});
	}

	public BloodTendencyClientPacket() {

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
