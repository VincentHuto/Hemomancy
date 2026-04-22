package com.vincenthuto.hemomancy.common.network.capa.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.tile.functional.FungalImplantMenuProvider;

import net.minecraft.network.FriendlyByteBuf;

public class PacketOpenSporeInv implements CustomPacketPayload {

	public static void encode(PacketOpenSporeInv msg, FriendlyByteBuf buf) {
	}

	public static PacketOpenSporeInv decode(FriendlyByteBuf buf) {
		return new PacketOpenSporeInv(buf);
	}

	public static final Type<PacketOpenSporeInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_spore_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenSporeInv> STREAM_CODEC = StreamCodec.of(PacketOpenSporeInv::encode, PacketOpenSporeInv::decode);

	public PacketOpenSporeInv() {
	}

	public PacketOpenSporeInv(FriendlyByteBuf buf) {
	}

	public void decode(FriendlyByteBuf buf) {
	}

	public static void handle(PacketOpenSporeInv msg, IPayloadContext ctx) {
		msg.handle(ctx);
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ctx.player().doCloseContainer();
			ctx.player().openMenu( new FungalImplantMenuProvider());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
