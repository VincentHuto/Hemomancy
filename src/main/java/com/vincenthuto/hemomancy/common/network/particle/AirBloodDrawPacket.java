package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class AirBloodDrawPacket implements CustomPacketPayload {

	public static final Type<AirBloodDrawPacket> TYPE = new Type<>(Hemomancy.rloc("air_blood_draw_packet"));
	public static final StreamCodec<FriendlyByteBuf, AirBloodDrawPacket> STREAM_CODEC = StreamCodec.of(AirBloodDrawPacket::encode, AirBloodDrawPacket::decode);

	public static AirBloodDrawPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new AirBloodDrawPacket(buffer.readFloat());
	}

	public static void encode(final FriendlyByteBuf buffer, final AirBloodDrawPacket message) {
		buffer.writeByte(0);
		buffer.writeFloat(message.parTick);
	}

	public static void handle(final AirBloodDrawPacket message, final IPayloadContext ctx) {
	}

	float parTick;

	public AirBloodDrawPacket() {
	}

	public AirBloodDrawPacket(float par) {
		this.parTick = par;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
