package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class StartCentrifugeButtonPacket implements CustomPacketPayload {

	public static final Type<StartCentrifugeButtonPacket> TYPE = new Type<>(Hemomancy.rloc("start_centrifuge_button_packet"));
	public static final StreamCodec<FriendlyByteBuf, StartCentrifugeButtonPacket> STREAM_CODEC = StreamCodec.of(StartCentrifugeButtonPacket::encode, StartCentrifugeButtonPacket::decode);

	public static StartCentrifugeButtonPacket decode(FriendlyByteBuf buf) {
		return new StartCentrifugeButtonPacket();
	}

	public static void encode(StartCentrifugeButtonPacket msg, FriendlyByteBuf buf) {
	}

	public static void handle(final StartCentrifugeButtonPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			AbstractContainerMenu container = ctx.player().containerMenu;
			if (container instanceof VialCentrifugeMenu) {
				VialCentrifugeBlockEntity station = ((VialCentrifugeMenu) container).getTe();
				station.attemptStartup();
			}
		});
	}

	public StartCentrifugeButtonPacket() {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
