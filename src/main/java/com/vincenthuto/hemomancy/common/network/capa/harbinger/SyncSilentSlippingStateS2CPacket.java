package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSilentSlippingStateS2CPacket(boolean active, int remainingTicks) implements CustomPacketPayload {
	public static final Type<SyncSilentSlippingStateS2CPacket> TYPE =
			new Type<>(Hemomancy.rloc("sync_silent_slipping_state"));
	public static final StreamCodec<FriendlyByteBuf, SyncSilentSlippingStateS2CPacket> STREAM_CODEC =
			StreamCodec.of(SyncSilentSlippingStateS2CPacket::encode, SyncSilentSlippingStateS2CPacket::decode);

	public static void encode(FriendlyByteBuf buffer, SyncSilentSlippingStateS2CPacket packet) {
		buffer.writeBoolean(packet.active);
		buffer.writeVarInt(packet.remainingTicks);
	}

	public static SyncSilentSlippingStateS2CPacket decode(FriendlyByteBuf buffer) {
		return new SyncSilentSlippingStateS2CPacket(buffer.readBoolean(), buffer.readVarInt());
	}

	public static void handle(SyncSilentSlippingStateS2CPacket packet, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player != null) {
				SilentArchonArmorAbilityHandler.handleClientSilentSlippingState(
						player, packet.active, packet.remainingTicks);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
