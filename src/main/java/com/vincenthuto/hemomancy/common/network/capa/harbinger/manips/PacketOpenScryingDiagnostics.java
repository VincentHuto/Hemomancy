package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.menu.ScryingDiagnosticsMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenScryingDiagnostics implements CustomPacketPayload {
	public static final Type<PacketOpenScryingDiagnostics> TYPE =
			new Type<>(Hemomancy.rloc("packet_open_scrying_diagnostics"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenScryingDiagnostics> STREAM_CODEC =
			StreamCodec.of(PacketOpenScryingDiagnostics::encode, PacketOpenScryingDiagnostics::decode);
	private static final double MAX_USE_DISTANCE_SQR = 64.0D;

	private final BlockPos pos;

	public PacketOpenScryingDiagnostics(BlockPos pos) {
		this.pos = pos;
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenScryingDiagnostics msg) {
		buf.writeBlockPos(msg.pos);
	}

	public static PacketOpenScryingDiagnostics decode(final FriendlyByteBuf buf) {
		return new PacketOpenScryingDiagnostics(buf.readBlockPos());
	}

	public static void handle(final PacketOpenScryingDiagnostics msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				if (!canOpenScryingDiagnostics(player, msg.pos)) {
					return;
				}
				player.closeContainer();
				player.openMenu(new ScryingDiagnosticsMenuProvider());
			}
		});
	}

	private static boolean canOpenScryingDiagnostics(ServerPlayer player, BlockPos pos) {
		return player.level().hasChunkAt(pos)
				&& player.level().getBlockState(pos).is(BlockInit.scrying_podium.get())
				&& player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
						<= MAX_USE_DISTANCE_SQR;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
