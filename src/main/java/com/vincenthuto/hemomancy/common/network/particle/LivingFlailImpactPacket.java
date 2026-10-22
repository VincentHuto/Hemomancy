package com.vincenthuto.hemomancy.common.network.particle;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record LivingFlailImpactPacket(float charge, int seed) implements CustomPacketPayload {
	public static final Type<LivingFlailImpactPacket> TYPE = new Type<>(Hemomancy.rloc("living_flail_impact"));
	public static final StreamCodec<FriendlyByteBuf, LivingFlailImpactPacket> STREAM_CODEC =
			StreamCodec.of(LivingFlailImpactPacket::encode, LivingFlailImpactPacket::decode);

	public LivingFlailImpactPacket {
		charge = Math.max(0.0F, Math.min(1.0F, charge));
	}

	private static void encode(FriendlyByteBuf buffer, LivingFlailImpactPacket packet) {
		buffer.writeFloat(packet.charge);
		buffer.writeInt(packet.seed);
	}

	private static LivingFlailImpactPacket decode(FriendlyByteBuf buffer) {
		return new LivingFlailImpactPacket(buffer.readFloat(), buffer.readInt());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
