package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.AttributeInit.TriState;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;

public final class CPacketFlight implements CustomPacketPayload {

	public static final Type<CPacketFlight> TYPE = new Type<>(Hemomancy.rloc("c_packet_flight"));
	public static final StreamCodec<FriendlyByteBuf, CPacketFlight> STREAM_CODEC = StreamCodec.of(CPacketFlight::encode,
			CPacketFlight::decode);

	public static void encode(FriendlyByteBuf buf, CPacketFlight msg) {
	}

	public static CPacketFlight decode(FriendlyByteBuf buf) {
		return new CPacketFlight();
	}

	@SuppressWarnings("ConstantConditions")
	public static void handle(final CPacketFlight msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.player();

			if (sender != null) {
				sender.stopFallFlying();

				if (!sender.onGround() && !sender.isFallFlying() && !sender.isInWater()
						&& !sender.hasEffect(MobEffects.LEVITATION)
						&& AttributeInit.canFallFly(sender) != TriState.DENY) {
					sender.startFallFlying();
				}
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
