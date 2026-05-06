package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.LayerEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketCurvedHornAnimation implements CustomPacketPayload {
	public static final Type<PacketCurvedHornAnimation> TYPE = new Type<>(Hemomancy.rloc("packet_curved_horn_animation"));
	public static final StreamCodec<FriendlyByteBuf, PacketCurvedHornAnimation> STREAM_CODEC = StreamCodec.of(PacketCurvedHornAnimation::encode, PacketCurvedHornAnimation::decode);

	public PacketCurvedHornAnimation() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketCurvedHornAnimation msg) {
		buf.writeByte(0);
	}

	public static PacketCurvedHornAnimation decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketCurvedHornAnimation();
	}

	public static void handle(final PacketCurvedHornAnimation msg, final IPayloadContext ctx) {
		ctx.enqueueWork(LayerEvents::playHornAnimation);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
