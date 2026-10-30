package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.world.HeartNodeMoment;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A heart-node moment at {@code pos} for players near it: planting, withering, or a cache split open. {@code stage}
 * is the node's maturity when it withered, so the client's ghost shows the body it had; 0 otherwise.
 */
public record HeartNodeMomentPacket(BlockPos pos, HeartNodeMoment moment, int stage) implements CustomPacketPayload {
	public static final Type<HeartNodeMomentPacket> TYPE = new Type<>(Hemomancy.rloc("heart_node_moment"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HeartNodeMomentPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, HeartNodeMomentPacket::pos,
			HeartNodeMoment.STREAM_CODEC, HeartNodeMomentPacket::moment,
			ByteBufCodecs.VAR_INT, HeartNodeMomentPacket::stage,
			HeartNodeMomentPacket::new);

	public HeartNodeMomentPacket {
		stage = Math.max(0, Math.min(HeartNodeRules.MATURE_STAGE, stage));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
