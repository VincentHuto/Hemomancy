package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarMenuProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkHooks;

public class PacketOpenScarsInv implements CustomPacketPayload {

	public static void encode(PacketOpenScarsInv msg, FriendlyByteBuf buf) {
	}

	public static PacketOpenScarsInv decode(FriendlyByteBuf buf) {
		return new PacketOpenScarsInv(buf);
	}

	public static final Type<PacketOpenScarsInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_scars_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenScarsInv> STREAM_CODEC = StreamCodec.of(PacketOpenScarsInv::encode, PacketOpenScarsInv::decode);

	public PacketOpenScarsInv() {
	}

	public PacketOpenScarsInv(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketOpenScarsInv msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ctx.player().doCloseContainer();
			NetworkHooks.openScreen(ctx.player(), new ScarMenuProvider());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
