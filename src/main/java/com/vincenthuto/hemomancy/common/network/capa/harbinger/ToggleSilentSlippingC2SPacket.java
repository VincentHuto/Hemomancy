package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleSilentSlippingC2SPacket() implements CustomPacketPayload {
	public static final Type<ToggleSilentSlippingC2SPacket> TYPE =
			new Type<>(Hemomancy.rloc("toggle_silent_slipping"));
	public static final StreamCodec<FriendlyByteBuf, ToggleSilentSlippingC2SPacket> STREAM_CODEC =
			StreamCodec.of(ToggleSilentSlippingC2SPacket::encode, ToggleSilentSlippingC2SPacket::decode);

	public static void encode(FriendlyByteBuf buffer, ToggleSilentSlippingC2SPacket packet) {
		buffer.writeByte(0);
	}

	public static ToggleSilentSlippingC2SPacket decode(FriendlyByteBuf buffer) {
		buffer.readByte();
		return new ToggleSilentSlippingC2SPacket();
	}

	public static void handle(ToggleSilentSlippingC2SPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				SilentArchonArmorAbilityHandler.tryToggleSilentSlipping(player);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
