package com.vincenthuto.hemomancy.common.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/** How a viewer stands to a sanctum: its planter, or anyone else. */
public enum HeartNodeRelation {
	OWNER, STRANGER;

	public static final StreamCodec<ByteBuf, HeartNodeRelation> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
			index -> values()[Math.floorMod(index, values().length)], HeartNodeRelation::ordinal);

	public static HeartNodeRelation of(UUID owner, UUID viewer) {
		return owner.equals(viewer) ? OWNER : STRANGER;
	}
}
