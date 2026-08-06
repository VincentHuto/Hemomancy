package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.client.event.CardinalRiteImpactClientEvents;
import com.vincenthuto.hemomancy.client.screen.overlay.SanguineOmenOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

	public static void handle(CardinalRiteImpactPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			CardinalRiteImpactClientEvents.start(packet.durationTicks, packet.seed);
			if (SanguineOmenOverlay.instance != null) {
				SanguineOmenOverlay.instance.start(packet.durationTicks, packet.peakAlpha,
						packet.seed, SanguineOmenOverlay.Mode.SCREEN_OVERLAY);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
