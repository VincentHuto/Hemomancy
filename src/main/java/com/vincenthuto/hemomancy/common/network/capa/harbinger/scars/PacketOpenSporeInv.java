package com.vincenthuto.hemomancy.common.network.capa.harbinger.scars;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.tile.functional.FungalImplantMenuProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenSporeInv implements CustomPacketPayload {
	public static final Type<PacketOpenSporeInv> TYPE = new Type<>(Hemomancy.rloc("packet_open_spore_inv"));
	public static final StreamCodec<FriendlyByteBuf, PacketOpenSporeInv> STREAM_CODEC = StreamCodec.of(PacketOpenSporeInv::encode, PacketOpenSporeInv::decode);

	public PacketOpenSporeInv() {
	}

	public static void encode(final FriendlyByteBuf buf, final PacketOpenSporeInv msg) {
		buf.writeByte(0);
	}

	public static PacketOpenSporeInv decode(final FriendlyByteBuf buf) {
		buf.readByte();
		return new PacketOpenSporeInv();
	}

	public static void handle(final PacketOpenSporeInv msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player) {
				player.closeContainer();
				player.openMenu(new FungalImplantMenuProvider());
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
