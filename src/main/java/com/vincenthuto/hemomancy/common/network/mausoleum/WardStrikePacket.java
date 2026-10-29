package com.vincenthuto.hemomancy.common.network.mausoleum;

import com.vincenthuto.hemomancy.Hemomancy;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A Saint mausoleum's ward answering something: a refused break, place, blast or push on {@code pos}'s {@code face},
 * a visitor turned back at the threshold (the slab block they touched, {@code face} outward), or the hallowed ground
 * burning the Apotheos {@code entityId} (-1 for everything else).
 */
public record WardStrikePacket(BlockPos pos, Direction face, Kind kind, int entityId) implements CustomPacketPayload {
	public static final Type<WardStrikePacket> TYPE = new Type<>(Hemomancy.rloc("ward_strike"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WardStrikePacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, WardStrikePacket::pos,
			Direction.STREAM_CODEC, WardStrikePacket::face,
			Kind.STREAM_CODEC, WardStrikePacket::kind,
			ByteBufCodecs.VAR_INT, WardStrikePacket::entityId,
			WardStrikePacket::new);

	public enum Kind {
		BREAK, PLACE, EXPLOSION, PISTON, THRESHOLD, BURN;

		static final StreamCodec<ByteBuf, Kind> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
				index -> values()[Math.floorMod(index, values().length)], Kind::ordinal);
	}

	public static WardStrikePacket strike(BlockPos pos, Direction face, Kind kind) {
		return new WardStrikePacket(pos, face, kind, -1);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
