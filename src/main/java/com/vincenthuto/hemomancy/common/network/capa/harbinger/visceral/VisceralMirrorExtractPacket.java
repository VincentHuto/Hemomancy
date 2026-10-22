package com.vincenthuto.hemomancy.common.network.capa.harbinger.visceral;

import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.organs.EnumOrgan;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.VisceralMirrorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client → Server packet sent when the player clicks "Begin Extraction"
 * in the Visceral Mirror screen.
 */
public class VisceralMirrorExtractPacket implements CustomPacketPayload {

	public static final Type<VisceralMirrorExtractPacket> TYPE = new Type<>(Hemomancy.rloc("visceral_mirror_extract_packet"));
	public static final StreamCodec<FriendlyByteBuf, VisceralMirrorExtractPacket> STREAM_CODEC = StreamCodec.of(VisceralMirrorExtractPacket::encode, VisceralMirrorExtractPacket::decode);

	private final BlockPos pos;
	private final int organOrdinal;

	public VisceralMirrorExtractPacket(BlockPos pos, int organOrdinal) {
		this.pos = pos;
		this.organOrdinal = organOrdinal;
	}

	public static void encode(FriendlyByteBuf buf, VisceralMirrorExtractPacket msg) {
		buf.writeBlockPos(msg.pos);
		buf.writeInt(msg.organOrdinal);
	}

	public static VisceralMirrorExtractPacket decode(FriendlyByteBuf buf) {
		return new VisceralMirrorExtractPacket(buf.readBlockPos(), buf.readInt());
	}

	public static void handle(final VisceralMirrorExtractPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (!(player instanceof ServerPlayer sender)) return;

			VisceralMirrorBlockEntity mirror = ServerboundGuards.nearBlockEntity(sender, msg.pos,
					VisceralMirrorBlockEntity.class, ServerboundGuards.BLOCK_REACH_SQR);
			if (mirror == null) return;

			EnumOrgan[] organs = EnumOrgan.values();
			if (msg.organOrdinal < 0 || msg.organOrdinal >= organs.length) return;

			mirror.startRitual(sender, organs[msg.organOrdinal]);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
