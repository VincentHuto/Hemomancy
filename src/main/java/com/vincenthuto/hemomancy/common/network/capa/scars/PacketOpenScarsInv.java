package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenScarsInv implements CustomPacketPayload {
	public static final Type<PacketOpenScarsInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_scars_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenScarsInv> STREAM_CODEC = StreamCodec.of(PacketOpenScarsInv::encode, PacketOpenScarsInv::decode);
	private static final double MAX_USE_DISTANCE_SQR = 64.0D;

	private final BlockPos pos;

	public PacketOpenScarsInv() {
		this(BlockPos.ZERO);
	}

	public PacketOpenScarsInv(BlockPos pos) {
		this.pos = pos;
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenScarsInv msg) {
		buf.writeBlockPos(msg.pos);
	}

	public static PacketOpenScarsInv decode(final FriendlyByteBuf buf) {
		return new PacketOpenScarsInv(buf.readBlockPos());
	}

	public static void handle(final PacketOpenScarsInv msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				if (!canOpenScarletVanity(player, msg.pos)) {
					return;
				}
				player.closeContainer();
				player.openMenu(new ScarMenuProvider(true));
			}
		});
	}

	private static boolean canOpenScarletVanity(ServerPlayer player, BlockPos pos) {
		return player.level().hasChunkAt(pos)
				&& player.level().getBlockState(pos).is(BlockInit.scarlet_vanity.get())
				&& player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D)
						<= MAX_USE_DISTANCE_SQR;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
