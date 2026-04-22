package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.event.LayerEvents;

import net.minecraft.network.FriendlyByteBuf;

public class PacketCurvedHornAnimation implements CustomPacketPayload {

	public static void encode(PacketCurvedHornAnimation msg, FriendlyByteBuf buf) {
	}

	public static PacketCurvedHornAnimation decode(FriendlyByteBuf buf) {
		return new PacketCurvedHornAnimation(buf);
	}

	public static final Type<PacketCurvedHornAnimation> TYPE = new Type<>(Hemomancy.rloc("packet_curved_horn_animation"));
	public static final StreamCodec<FriendlyByteBuf, PacketCurvedHornAnimation> STREAM_CODEC = StreamCodec.of(PacketCurvedHornAnimation::encode, PacketCurvedHornAnimation::decode);

	public PacketCurvedHornAnimation() {
	}

	public PacketCurvedHornAnimation(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketCurvedHornAnimation msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(LayerEvents::playHornAnimation);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
