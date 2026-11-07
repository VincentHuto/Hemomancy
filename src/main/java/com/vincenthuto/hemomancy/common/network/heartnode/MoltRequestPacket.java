package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.MoltingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.MoltingService;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the owner of the heart-node at {@code pos} molts into {@code allocation}, one whole number per
 * tendency in {@code EnumBloodTendency} order. The node must be in reach and the sender's; {@link MoltingService}
 * checks everything else again.
 */
public record MoltRequestPacket(BlockPos pos, int[] allocation) implements CustomPacketPayload {
	public static final Type<MoltRequestPacket> TYPE = new Type<>(Hemomancy.rloc("molt_request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MoltRequestPacket> STREAM_CODEC = StreamCodec.of(
			MoltRequestPacket::encode, MoltRequestPacket::decode);
	private static final String RATE_KEY = "molt_request";
	private static final int RATE_TICKS = 10;

	public MoltRequestPacket {
		allocation = allocation.clone();
	}

	private static void encode(RegistryFriendlyByteBuf buf, MoltRequestPacket packet) {
		BlockPos.STREAM_CODEC.encode(buf, packet.pos);
		buf.writeVarInt(packet.allocation.length);
		for (int value : packet.allocation) buf.writeVarInt(value);
	}

	private static MoltRequestPacket decode(RegistryFriendlyByteBuf buf) {
		BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
		int count = buf.readVarInt();
		if (count != MoltingRules.TENDENCIES) throw new DecoderException("Molt needs one value per tendency: " + count);
		int[] allocation = new int[count];
		for (int i = 0; i < count; i++) allocation[i] = buf.readVarInt();
		return new MoltRequestPacket(pos, allocation);
	}

	public static void handle(MoltRequestPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer sender) accept(sender, packet);
		});
	}

	/** Molts if the node is the sender's and in reach; false when the packet is ignored or the molt refused. */
	public static boolean accept(ServerPlayer sender, MoltRequestPacket packet) {
		if (!ServerboundGuards.activePlayer(sender) || !ServerboundRateLimiter.tryAcquire(sender, RATE_KEY, RATE_TICKS)) {
			return false;
		}
		HeartNodeBlockEntity node = ServerboundGuards.nearBlockEntity(sender, packet.pos, HeartNodeBlockEntity.class,
				ServerboundGuards.BLOCK_REACH_SQR);
		if (node == null || !HeartNodeRules.mayOpen(node.owner(), sender.getUUID())) return false;
		return MoltingService.molt(sender, packet.pos, packet.allocation) == MoltingRules.Refusal.NONE;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
