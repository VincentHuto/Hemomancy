package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.event.ClientEvents;
import com.vincenthuto.hemomancy.client.screen.overlay.ManipCooldownOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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

	public static void encode(final FriendlyByteBuf buffer, final ManipCooldownPacket message) {
		buffer.writeInt(message.cooldownTicks);
	}

	public static ManipCooldownPacket decode(final FriendlyByteBuf buffer) {
		return new ManipCooldownPacket(buffer.readInt());
	}

	public static void handle(final ManipCooldownPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (message.cooldownTicks > 0) ClientEvents.manipulationCastAccepted();
			ManipCooldownOverlay.startCooldown(message.cooldownTicks);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
