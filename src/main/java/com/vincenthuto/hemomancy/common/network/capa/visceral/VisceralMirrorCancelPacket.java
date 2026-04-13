package com.vincenthuto.hemomancy.common.network.capa.visceral;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client → Server packet sent when the player clicks "Cancel" or closes
 * the Visceral Mirror screen during an active ritual.
 */
public class VisceralMirrorCancelPacket {

	private final BlockPos pos;

	public VisceralMirrorCancelPacket(BlockPos pos) {
		this.pos = pos;
	}

	public static void encode(VisceralMirrorCancelPacket msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
	}

	public static VisceralMirrorCancelPacket decode(FriendlyByteBuf buf) {
		return new VisceralMirrorCancelPacket(buf.readBlockPos());
	}

	public static void handle(VisceralMirrorCancelPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;

			if (sender.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5,
					msg.pos.getZ() + 0.5) > 64.0) return;

			BlockEntity be = sender.level().getBlockEntity(msg.pos);
			if (!(be instanceof VisceralMirrorBlockEntity mirror)) return;

			mirror.cancelRitual(sender);
		});
		ctx.get().setPacketHandled(true);
	}
}
