package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartNodeBlockEntity;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeService;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the owner of the heart-node at {@code pos} sets how their sanctum looks. Only presentation rides
 * here, so the only claims to check are that the node is in reach and is the sender's.
 */
public record HeartNodeSettingsPacket(BlockPos pos, HeartNodeSettings settings) implements CustomPacketPayload {
	public static final Type<HeartNodeSettingsPacket> TYPE = new Type<>(Hemomancy.rloc("heart_node_settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, HeartNodeSettingsPacket> STREAM_CODEC = StreamCodec.of(
			HeartNodeSettingsPacket::encode, HeartNodeSettingsPacket::decode);

	private static void encode(RegistryFriendlyByteBuf buf, HeartNodeSettingsPacket packet) {
		BlockPos.STREAM_CODEC.encode(buf, packet.pos);
		buf.writeVarInt(packet.settings.bits());
		buf.writeByte(packet.settings.intensity());
	}

	private static HeartNodeSettingsPacket decode(RegistryFriendlyByteBuf buf) {
		// The settings record drops unknown bits and clamps the intensity.
		return new HeartNodeSettingsPacket(BlockPos.STREAM_CODEC.decode(buf),
				new HeartNodeSettings(buf.readVarInt(), buf.readByte()));
	}

	public static void handle(HeartNodeSettingsPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer sender) accept(sender, packet);
		});
	}

	/** Applies the packet if its node is within block reach and planted by {@code sender}; false when it is ignored. */
	public static boolean accept(ServerPlayer sender, HeartNodeSettingsPacket packet) {
		HeartNodeBlockEntity node = ServerboundGuards.nearBlockEntity(sender, packet.pos, HeartNodeBlockEntity.class,
				ServerboundGuards.BLOCK_REACH_SQR);
		if (node == null || !HeartNodeRules.mayOpen(node.owner(), sender.getUUID())) return false;
		return HeartNodeService.updateSettings(sender, packet.pos, packet.settings);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
