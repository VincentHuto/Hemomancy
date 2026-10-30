package com.vincenthuto.hemomancy.common.world;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The heart-node's short moments of presentation, shown to players near where they happen. Each is a client effect
 * only: the server has already made the change it presents.
 */
public enum HeartNodeMoment {
	/** A node was planted: roots punch out, spores burst, the heart beats hard three times. */
	PLANTED,
	/** A node withered (replanted elsewhere or broken by its owner): its last frame greys and collapses. */
	WITHERED,
	/** Its owner opened a fruiting-body cache: the cap splits and the body sinks away. */
	CACHE_OPENED;

	private static final HeartNodeMoment[] VALUES = values();
	public static final StreamCodec<ByteBuf, HeartNodeMoment> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(
			HeartNodeMoment::byId, HeartNodeMoment::ordinal);

	/** The moment with this wire id; an unknown id reads as {@link #PLANTED}, which only plays an effect. */
	public static HeartNodeMoment byId(int id) {
		return id >= 0 && id < VALUES.length ? VALUES[id] : PLANTED;
	}
}
