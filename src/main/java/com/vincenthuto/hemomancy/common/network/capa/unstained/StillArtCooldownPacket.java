package com.vincenthuto.hemomancy.common.network.capa.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.StillArtCooldownOverlay;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client packet that starts the pale focus-recovery vignette after a
 * Still Art is cast.
 */
public class StillArtCooldownPacket implements CustomPacketPayload {

	public static final Type<StillArtCooldownPacket> TYPE = new Type<>(Hemomancy.rloc("still_art_cooldown_packet"));
	public static final StreamCodec<FriendlyByteBuf, StillArtCooldownPacket> STREAM_CODEC = StreamCodec.of(
			StillArtCooldownPacket::encode, StillArtCooldownPacket::decode);

	private final int cooldownTicks;

	public StillArtCooldownPacket(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
	}

	public static void encode(final FriendlyByteBuf buffer, final StillArtCooldownPacket message) {
		buffer.writeInt(message.cooldownTicks);
	}

	public static StillArtCooldownPacket decode(final FriendlyByteBuf buffer) {
		return new StillArtCooldownPacket(buffer.readInt());
	}

	public static void handle(final StillArtCooldownPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> StillArtCooldownOverlay.startCooldown(message.cooldownTicks));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
