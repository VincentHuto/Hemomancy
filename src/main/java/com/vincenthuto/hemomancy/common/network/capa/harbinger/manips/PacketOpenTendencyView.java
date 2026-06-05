package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.TendencyViewMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenTendencyView implements CustomPacketPayload {
	public static final Type<PacketOpenTendencyView> TYPE = new Type<>(Hemomancy.rloc("packet_open_tendency_view"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenTendencyView> STREAM_CODEC = StreamCodec.of(PacketOpenTendencyView::encode, PacketOpenTendencyView::decode);

	public PacketOpenTendencyView() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenTendencyView msg) {
		buf.writeByte(0);
	}

	public static PacketOpenTendencyView decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenTendencyView();
	}

	public static void handle(final PacketOpenTendencyView msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				player.closeContainer();
				player.openMenu(new TendencyViewMenuProvider());
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
