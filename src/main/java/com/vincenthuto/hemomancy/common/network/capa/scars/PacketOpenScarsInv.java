package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenScarsInv implements CustomPacketPayload {
	public static final Type<PacketOpenScarsInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_scars_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenScarsInv> STREAM_CODEC = StreamCodec.of(PacketOpenScarsInv::encode, PacketOpenScarsInv::decode);

	public PacketOpenScarsInv() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenScarsInv msg) {
		buf.writeByte(0);
	}

	public static PacketOpenScarsInv decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenScarsInv();
	}

	public static void handle(final PacketOpenScarsInv msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				player.closeContainer();
				player.openMenu(new ScarMenuProvider());
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
