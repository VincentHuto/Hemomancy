package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server to owner: the Sculpting that claims what the player aims at refused its pulse (protected ground, nothing to
 * burn, too many walls), so the verb HUD shows its barred mark for {@code ticks}. A refused pulse waits out a cadence
 * and is refused again while the aim stays, so the mark refreshes itself and lapses once the work is paid.
 */
public record VerbRefusalPacket(int ticks) implements CustomPacketPayload {
	public static final Type<VerbRefusalPacket> TYPE = new Type<>(Hemomancy.rloc("verb_refusal"));
	public static final StreamCodec<FriendlyByteBuf, VerbRefusalPacket> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> buffer.writeVarInt(packet.ticks()),
			buffer -> new VerbRefusalPacket(buffer.readVarInt()));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
