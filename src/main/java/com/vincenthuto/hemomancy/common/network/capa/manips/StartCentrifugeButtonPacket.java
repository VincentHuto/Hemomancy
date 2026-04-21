package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.NetworkEvent;

public class StartCentrifugeButtonPacket {

	public static StartCentrifugeButtonPacket decode(FriendlyByteBuf buf) {
		return new StartCentrifugeButtonPacket();
	}

	public static void encode(StartCentrifugeButtonPacket msg, FriendlyByteBuf buf) {
	}

	public static void handle(final StartCentrifugeButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			AbstractContainerMenu container = ctx.get().getSender().containerMenu;
			if (container instanceof VialCentrifugeMenu) {
				VialCentrifugeBlockEntity station = ((VialCentrifugeMenu) container).getTe();
				station.attemptStartup();
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public StartCentrifugeButtonPacket() {
	}
}