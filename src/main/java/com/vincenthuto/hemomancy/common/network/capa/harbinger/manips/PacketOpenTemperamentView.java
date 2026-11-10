package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.TemperamentViewMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenTemperamentView implements CustomPacketPayload {
	public static final Type<PacketOpenTemperamentView> TYPE = new Type<>(Hemomancy.rloc("packet_open_temperament_view"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenTemperamentView> STREAM_CODEC = StreamCodec.of(PacketOpenTemperamentView::encode, PacketOpenTemperamentView::decode);

	public PacketOpenTemperamentView() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenTemperamentView msg) {
		buf.writeByte(0);
	}

	public static PacketOpenTemperamentView decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenTemperamentView();
	}

	public static void handle(final PacketOpenTemperamentView msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				player.closeContainer();
				player.openMenu(new TemperamentViewMenuProvider());
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
