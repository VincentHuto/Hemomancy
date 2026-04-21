package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkHooks;

public class PacketOpenVascularView implements CustomPacketPayload {

	public static void encode(PacketOpenVascularView msg, FriendlyByteBuf buf) {
	}

	public static PacketOpenVascularView decode(FriendlyByteBuf buf) {
		return new PacketOpenVascularView(buf);
	}

	public static final Type<PacketOpenVascularView> TYPE = new Type<>(Hemomancy.rloc("packet_open_vascular_view"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenVascularView> STREAM_CODEC = StreamCodec.of(PacketOpenVascularView::encode, PacketOpenVascularView::decode);

	public PacketOpenVascularView() {
	}

	public PacketOpenVascularView(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketOpenVascularView msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ctx.player().doCloseContainer();
			NetworkHooks.openScreen(ctx.player(), new VascularViewMenuProvider());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
