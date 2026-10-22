package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class PacketOpenBloodlinePoolScreen implements CustomPacketPayload {
	public static final Type<PacketOpenBloodlinePoolScreen> TYPE =
			new Type<>(Hemomancy.rloc("packet_open_bloodline_pool_screen"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenBloodlinePoolScreen> STREAM_CODEC =
			StreamCodec.of(PacketOpenBloodlinePoolScreen::encode, PacketOpenBloodlinePoolScreen::decode);

	public PacketOpenBloodlinePoolScreen() {
	}

	public static void encode(FriendlyByteBuf buf, PacketOpenBloodlinePoolScreen msg) {
		buf.writeByte(0);
	}

	public static PacketOpenBloodlinePoolScreen decode(FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenBloodlinePoolScreen();
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

