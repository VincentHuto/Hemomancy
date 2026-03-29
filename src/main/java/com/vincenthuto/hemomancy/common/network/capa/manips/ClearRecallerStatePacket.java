package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.tile.VisceralRecallerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class ClearRecallerStatePacket {

	private final BlockPos pos;

	public ClearRecallerStatePacket(BlockPos pos) {
		this.pos = pos;
	}

	public static ClearRecallerStatePacket decode(FriendlyByteBuf buf) {
		return new ClearRecallerStatePacket(buf.readBlockPos());
	}

	public static void encode(ClearRecallerStatePacket msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
	}

	public static class Handler {

		public static void handle(final ClearRecallerStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ServerPlayer player = ctx.get().getSender();
				if (player != null) {
					BlockEntity tile = player.level().getBlockEntity(msg.pos);
					if (tile instanceof VisceralRecallerBlockEntity station) {
						// Verify the player is close enough
						if (player.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5, msg.pos.getZ() + 0.5) <= 64.0) {
							station.clearTendency();
						}
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}