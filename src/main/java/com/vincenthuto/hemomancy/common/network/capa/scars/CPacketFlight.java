package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.AttributeInit.TriState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

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
			Player player = ctx.player();
			if (player instanceof ServerPlayer sender) {
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
