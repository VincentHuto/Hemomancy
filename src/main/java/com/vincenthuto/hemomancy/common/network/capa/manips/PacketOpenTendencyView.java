package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.TendencyViewMenuProvider;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import net.minecraft.network.FriendlyByteBuf;

public class PacketOpenTendencyView implements CustomPacketPayload {

	public static void encode(PacketOpenTendencyView msg, FriendlyByteBuf buf) {
	}

	public static PacketOpenTendencyView decode(FriendlyByteBuf buf) {
		return new PacketOpenTendencyView(buf);
	}

	public static final Type<PacketOpenTendencyView> TYPE = new Type<>(Hemomancy.rloc("packet_open_tendency_view"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenTendencyView> STREAM_CODEC = StreamCodec.of(PacketOpenTendencyView::encode, PacketOpenTendencyView::decode);

	public PacketOpenTendencyView() {
	}

	public PacketOpenTendencyView(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketOpenTendencyView msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ctx.player().doCloseContainer();
			ctx.player().openMenu( new TendencyViewMenuProvider());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
