package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the owner of the heart-node at {@code pos} leaves the Gardens from its screen (NQ-12-4). The node
 * must be in reach and the sender's, and the sender alive; {@link HeartNodeService#leaveTheGardens} picks the way.
 */
public record HeartNodeLeavePacket(BlockPos pos) implements CustomPacketPayload {
	public static final Type<HeartNodeLeavePacket> TYPE = new Type<>(Hemomancy.rloc("heart_node_leave"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HeartNodeLeavePacket> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> BlockPos.STREAM_CODEC.encode(buf, packet.pos),
			buf -> new HeartNodeLeavePacket(BlockPos.STREAM_CODEC.decode(buf)));
	private static final String RATE_KEY = "heart_node_leave";
	private static final int RATE_TICKS = 20;

	public static void handle(HeartNodeLeavePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer sender) accept(sender, packet);
		});
	}

	/** Sends the sender home if the node is theirs and in reach; false when the packet is ignored. */
	public static boolean accept(ServerPlayer sender, HeartNodeLeavePacket packet) {
		if (!sender.isAlive() || !ServerboundRateLimiter.tryAcquire(sender, RATE_KEY, RATE_TICKS)) return false;
		HeartNodeBlockEntity node = ServerboundGuards.nearBlockEntity(sender, packet.pos, HeartNodeBlockEntity.class,
				ServerboundGuards.BLOCK_REACH_SQR);
		if (node == null || !HeartNodeRules.mayOpen(node.owner(), sender.getUUID())) return false;
		return HeartNodeService.leaveTheGardens(sender);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
