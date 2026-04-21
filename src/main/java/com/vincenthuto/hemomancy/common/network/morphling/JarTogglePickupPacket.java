package com.vincenthuto.hemomancy.common.network.morphling;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/** Legacy packet – auto-pickup feature has been removed. Kept for wire compatibility. */
public class JarTogglePickupPacket {
	public static JarTogglePickupPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new JarTogglePickupPacket();
	}

	public static void encode(final JarTogglePickupPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final JarTogglePickupPacket message, final Supplier<NetworkEvent.Context> ctx) {
		// Auto-pickup has been removed; this packet is now a no-op.
		ctx.get().setPacketHandled(true);
	}
}