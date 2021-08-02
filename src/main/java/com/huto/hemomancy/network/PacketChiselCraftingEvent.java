package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.container.ContainerChiselStation;
import com.huto.hemomancy.tile.TileEntityChiselStation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketChiselCraftingEvent {

	public PacketChiselCraftingEvent() {
	}

	public static void encode(PacketChiselCraftingEvent msg, FriendlyByteBuf buf) {
	}

	public static PacketChiselCraftingEvent decode(FriendlyByteBuf buf) {
		return new PacketChiselCraftingEvent();
	}

	public static class Handler {

		public static void handle(final PacketChiselCraftingEvent msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu container = ctx.get().getSender().openContainer;
				if (container instanceof ContainerChiselStation) {
					TileEntityChiselStation station = ((ContainerChiselStation) container).getTe();
					station.craftEvent();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}