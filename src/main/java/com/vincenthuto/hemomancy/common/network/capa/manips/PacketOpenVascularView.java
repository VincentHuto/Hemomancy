package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import net.minecraft.network.FriendlyByteBuf;

public class PacketOpenVascularView implements CustomPacketPayload {
	public static final Type<PacketOpenVascularView> TYPE = new Type<>(Hemomancy.rloc("packet_open_vascular_view"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenVascularView> STREAM_CODEC = StreamCodec.of(PacketOpenVascularView::encode, PacketOpenVascularView::decode);

	public PacketOpenVascularView() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenVascularView msg) {
		buf.writeByte(0);
	}

	public static PacketOpenVascularView decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenVascularView();
	}

	public static void handle(final PacketOpenVascularView msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ctx.player().doCloseContainer();
			ctx.player().openMenu(new VascularViewMenuProvider());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
