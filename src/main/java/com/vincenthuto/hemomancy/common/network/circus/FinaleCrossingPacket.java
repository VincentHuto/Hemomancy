package com.vincenthuto.hemomancy.common.network.circus;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The Ringmaster's finale crosses its player into his Chamber now (2026-11-18): the client keeps its last frame of the
 * real tent and the overworld's sky, so the change of level is never seen. {@code sceneTick} is how far into his
 * opening scene the crossing falls.
 */
public record FinaleCrossingPacket(int sceneTick) implements CustomPacketPayload {
	public static final Type<FinaleCrossingPacket> TYPE = new Type<>(Hemomancy.rloc("finale_crossing"));
	public static final StreamCodec<FriendlyByteBuf, FinaleCrossingPacket> STREAM_CODEC =
			StreamCodec.of((buffer, packet) -> buffer.writeVarInt(packet.sceneTick),
					buffer -> new FinaleCrossingPacket(buffer.readVarInt()));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
