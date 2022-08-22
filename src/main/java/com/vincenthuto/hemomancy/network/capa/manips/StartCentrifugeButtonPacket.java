package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.container.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.tile.VialCentrifugeBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class StartCentrifugeButtonPacket {

	public StartCentrifugeButtonPacket() {
	}

	public static void encode(StartCentrifugeButtonPacket msg, FriendlyByteBuf buf) {

	}

	public static StartCentrifugeButtonPacket decode(FriendlyByteBuf buf) {
		return new StartCentrifugeButtonPacket();
	}

	public static class Handler {

		public static void handle(final StartCentrifugeButtonPacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof VialCentrifugeMenu) {
					VialCentrifugeBlockEntity station = ((VialCentrifugeMenu) container).getTe();
					station.dataAccess.set(0, 200);
					
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}