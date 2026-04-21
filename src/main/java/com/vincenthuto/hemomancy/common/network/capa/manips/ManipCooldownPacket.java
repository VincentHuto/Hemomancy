package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.client.screen.overlay.ManipCooldownOverlay;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkEvent;

/**
 * Server-to-client packet that notifies the client a manipulation was cast
 * and triggers the cooldown vignette overlay.
 */
public class ManipCooldownPacket {

	private final int cooldownTicks;

	public ManipCooldownPacket(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
	}

	public static void encode(final ManipCooldownPacket message, final FriendlyByteBuf buffer) {
		buffer.writeInt(message.cooldownTicks);
	}

	public static ManipCooldownPacket decode(final FriendlyByteBuf buffer) {
		return new ManipCooldownPacket(buffer.readInt());
	}

	public static void handle(final ManipCooldownPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ManipCooldownOverlay.startCooldown(message.cooldownTicks);
		});
		ctx.get().setPacketHandled(true);
	}
}
