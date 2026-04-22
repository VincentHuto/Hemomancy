package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.crafting.SomaticLoomBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClearLoomStatePacket implements CustomPacketPayload {

	public static final Type<ClearLoomStatePacket> TYPE = new Type<>(Hemomancy.rloc("clear_loom_state_packet"));
	public static final StreamCodec<FriendlyByteBuf, ClearLoomStatePacket> STREAM_CODEC =
			StreamCodec.of(ClearLoomStatePacket::encode, ClearLoomStatePacket::decode);

	private final BlockPos pos;

	public ClearLoomStatePacket(BlockPos pos) {
		this.pos = pos;
	}

	public static ClearLoomStatePacket decode(FriendlyByteBuf buf) {
		return new ClearLoomStatePacket(buf.readBlockPos());
	}

	public static void encode(ClearLoomStatePacket msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
	}

	public static void handle(final ClearLoomStatePacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer player = (ServerPlayer) ctx.player();
			if (player != null) {
				BlockEntity tile = player.level().getBlockEntity(msg.pos);
				if (tile instanceof SomaticLoomBlockEntity station) {
					// Verify the player is close enough
					if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) <= 64.0) {
						station.clearTendency();
					}
				}
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}