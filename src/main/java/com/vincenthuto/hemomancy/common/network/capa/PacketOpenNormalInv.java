package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PacketOpenNormalInv implements CustomPacketPayload {
	public static final Type<PacketOpenNormalInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_normal_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenNormalInv> STREAM_CODEC = StreamCodec.of(PacketOpenNormalInv::encode, PacketOpenNormalInv::decode);

	public PacketOpenNormalInv() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenNormalInv msg) {
		buf.writeByte(0);
	}

	public static PacketOpenNormalInv decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenNormalInv();
	}

	public static void handle(final PacketOpenNormalInv msg, final IPayloadContext ctx) {
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
