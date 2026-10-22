package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Server to client: the rite spaces of the receiver's current dimension, with the purpose each admits and the
 * floor style its boundary is drawn in. Each packet replaces the client's whole list.
 */
public record RiteSpaceSyncPacket(List<Space> spaces) implements CustomPacketPayload {
	public static final int MAX_SPACES = 512;

	public record Space(BlockPos focus, int half, CardinalRitePurpose purpose, int floorStyle) {
		private static final StreamCodec<ByteBuf, CardinalRitePurpose> PURPOSE_CODEC = ByteBufCodecs.VAR_INT.map(
				ordinal -> CardinalRitePurpose.values()[Math.floorMod(ordinal, CardinalRitePurpose.values().length)],
				CardinalRitePurpose::ordinal);
		public static final StreamCodec<ByteBuf, Space> STREAM_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, Space::focus,
				ByteBufCodecs.VAR_INT, Space::half,
				PURPOSE_CODEC, Space::purpose,
				ByteBufCodecs.VAR_INT, Space::floorStyle,
				Space::new);

		public static Space of(RiteSpace space) {
			return new Space(space.focus(), space.half(), space.purpose(), CardinalRiteFloorVisualRules.styleIndex(
					CardinalRitePurpose.floorStyleOf(space.floor().getPath())));
		}

		public boolean covers(BlockPos pos) {
			return RiteSpaceRules.covers(focus, half, pos);
		}
	}

	public static final Type<RiteSpaceSyncPacket> TYPE = new Type<>(Hemomancy.rloc("rite_space_sync"));
	public static final StreamCodec<ByteBuf, RiteSpaceSyncPacket> STREAM_CODEC = Space.STREAM_CODEC
			.apply(ByteBufCodecs.list(MAX_SPACES))
			.map(RiteSpaceSyncPacket::new, RiteSpaceSyncPacket::spaces);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
