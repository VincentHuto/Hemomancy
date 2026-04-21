package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketOpenNormalInv implements CustomPacketPayload {

	public static void encode(PacketOpenNormalInv msg, FriendlyByteBuf buf) {
	}

	public static PacketOpenNormalInv decode(FriendlyByteBuf buf) {
		return new PacketOpenNormalInv(buf);
	}

	public static final Type<PacketOpenNormalInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_normal_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenNormalInv> STREAM_CODEC = StreamCodec.of(PacketOpenNormalInv::encode, PacketOpenNormalInv::decode);

	public PacketOpenNormalInv() {
	}

	public PacketOpenNormalInv(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketOpenNormalInv msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer playerEntity = ctx.player();
			if (playerEntity != null) {
				playerEntity.containerMenu.removed(playerEntity);
				playerEntity.containerMenu = playerEntity.inventoryMenu;
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
