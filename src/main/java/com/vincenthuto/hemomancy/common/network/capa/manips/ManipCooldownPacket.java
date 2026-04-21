package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.screen.overlay.ManipCooldownOverlay;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server-to-client packet that notifies the client a manipulation was cast
 * and triggers the cooldown vignette overlay.
 */
public class ManipCooldownPacket implements CustomPacketPayload {

	public static final Type<ManipCooldownPacket> TYPE = new Type<>(Hemomancy.rloc("manip_cooldown_packet"));
	public static final StreamCodec<FriendlyByteBuf, ManipCooldownPacket> STREAM_CODEC = StreamCodec.of(ManipCooldownPacket::encode, ManipCooldownPacket::decode);

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

	public static void handle(final ManipCooldownPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ManipCooldownOverlay.startCooldown(message.cooldownTicks);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
