package com.vincenthuto.hemomancy.common.network.morphling;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.FriendlyByteBuf;

/** Legacy packet – auto-pickup feature has been removed. Kept for wire compatibility. */
public class JarTogglePickupPacket implements CustomPacketPayload {

	public static final Type<JarTogglePickupPacket> TYPE = new Type<>(Hemomancy.rloc("jar_toggle_pickup_packet"));
	public static final StreamCodec<FriendlyByteBuf, JarTogglePickupPacket> STREAM_CODEC = StreamCodec.of(JarTogglePickupPacket::encode, JarTogglePickupPacket::decode);
	public static JarTogglePickupPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new JarTogglePickupPacket();
	}

	public static void encode(final FriendlyByteBuf buffer, final JarTogglePickupPacket message) {
		buffer.writeByte(0);
	}

	public static void handle(final JarTogglePickupPacket message, final IPayloadContext ctx) {
		// Auto-pickup has been removed; this packet is now a no-op.
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
