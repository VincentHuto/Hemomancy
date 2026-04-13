package com.vincenthuto.hemomancy.common.network.capa.visceral;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.tile.functional.VisceralMirrorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client → Server packet sent when the player clicks "Begin Extraction"
 * in the Visceral Mirror screen.
 */
public class VisceralMirrorExtractPacket {

	private final BlockPos pos;
	private final int organOrdinal;

	public VisceralMirrorExtractPacket(BlockPos pos, int organOrdinal) {
		this.pos = pos;
		this.organOrdinal = organOrdinal;
	}

	public static void encode(VisceralMirrorExtractPacket msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.pos);
		buf.writeInt(msg.organOrdinal);
	}

	public static VisceralMirrorExtractPacket decode(FriendlyByteBuf buf) {
		return new VisceralMirrorExtractPacket(buf.readBlockPos(), buf.readInt());
	}

	public static void handle(VisceralMirrorExtractPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			if (sender == null) return;

			// Validate range
			if (sender.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5,
					msg.pos.getZ() + 0.5) > 64.0) return;

			BlockEntity be = sender.level().getBlockEntity(msg.pos);
			if (!(be instanceof VisceralMirrorBlockEntity mirror)) return;

			EnumOrgan[] organs = EnumOrgan.values();
			if (msg.organOrdinal < 0 || msg.organOrdinal >= organs.length) return;

			mirror.startRitual(sender, organs[msg.organOrdinal]);
		});
		ctx.get().setPacketHandled(true);
	}
}
