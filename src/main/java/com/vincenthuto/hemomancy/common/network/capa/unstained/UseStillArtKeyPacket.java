package com.vincenthuto.hemomancy.common.network.capa.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class UseStillArtKeyPacket implements CustomPacketPayload {
	public static final Type<UseStillArtKeyPacket> TYPE = new Type<>(Hemomancy.rloc("use_still_art_key_packet"));
	public static final StreamCodec<FriendlyByteBuf, UseStillArtKeyPacket> STREAM_CODEC =
			StreamCodec.of(UseStillArtKeyPacket::encode, UseStillArtKeyPacket::decode);

	public UseStillArtKeyPacket() {
	}

	public static void encode(FriendlyByteBuf buf, UseStillArtKeyPacket msg) {
	}

	public static UseStillArtKeyPacket decode(FriendlyByteBuf buf) {
		return new UseStillArtKeyPacket();
	}

	public static void handle(final UseStillArtKeyPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (!(ctx.player() instanceof ServerPlayer player)) {
				return;
			}
			HemoCapabilityAccess.getKnownStillArts(player).ifPresent(known ->
					known.getSelectedArt().performAction(player, player.level(), player.getMainHandItem(), player.blockPosition()));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
