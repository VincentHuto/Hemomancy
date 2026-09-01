package com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.VisceralMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server packet sent when the player clicks "Cancel" or closes
 * the Visceral Mirror screen during an active ritual.
 */
public class VisceralMirrorCancelPacket implements CustomPacketPayload {

	public static final Type<VisceralMirrorCancelPacket> TYPE = new Type<>(Hemomancy.rloc("visceral_mirror_cancel_packet"));
	public static final StreamCodec<FriendlyByteBuf, VisceralMirrorCancelPacket> STREAM_CODEC = StreamCodec.of(VisceralMirrorCancelPacket::encode, VisceralMirrorCancelPacket::decode);

	private final BlockPos pos;

	public VisceralMirrorCancelPacket(BlockPos pos) {
		this.pos = pos;
	}

	public static void encode(FriendlyByteBuf buf, VisceralMirrorCancelPacket msg) {
		buf.writeBlockPos(msg.pos);
	}

	public static VisceralMirrorCancelPacket decode(FriendlyByteBuf buf) {
		return new VisceralMirrorCancelPacket(buf.readBlockPos());
	}

	public static void handle(final VisceralMirrorCancelPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (!(player instanceof ServerPlayer sender)) return;

			if (sender.distanceToSqr(msg.pos.getX() + 0.5, msg.pos.getY() + 0.5,
					msg.pos.getZ() + 0.5) > 64.0) return;

			BlockEntity be = sender.level().getBlockEntity(msg.pos);
			if (!(be instanceof VisceralMirrorBlockEntity mirror)) return;

			mirror.cancelRitual(sender);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
