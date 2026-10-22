package com.vincenthuto.hemomancy.common.network.particle;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-authored cue for the daemon's brief player impact feedback. */
public record CardinalRiteImpactPacket(int durationTicks, float peakAlpha, int seed)
		implements CustomPacketPayload {
	public static final Type<CardinalRiteImpactPacket> TYPE = new Type<>(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite_impact"));
	public static final StreamCodec<FriendlyByteBuf, CardinalRiteImpactPacket> STREAM_CODEC =
			StreamCodec.of(CardinalRiteImpactPacket::encode, CardinalRiteImpactPacket::decode);

	public CardinalRiteImpactPacket {
		durationTicks = Math.max(1, durationTicks);
		peakAlpha = Math.max(0.0F, Math.min(1.0F, peakAlpha));
	}

	public static void encode(FriendlyByteBuf buffer, CardinalRiteImpactPacket packet) {
		buffer.writeVarInt(packet.durationTicks);
		buffer.writeFloat(packet.peakAlpha);
		buffer.writeInt(packet.seed);
	}

	public static CardinalRiteImpactPacket decode(FriendlyByteBuf buffer) {
		return new CardinalRiteImpactPacket(
				buffer.readVarInt(), buffer.readFloat(), buffer.readInt());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
