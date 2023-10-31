package com.vincenthuto.hemomancy.common.network.capa.runes;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.menu.ChiselStationMenu;
import com.vincenthuto.hemomancy.common.tile.ChiselStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

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
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof ChiselStationMenu) {
					ChiselStationBlockEntity station = ((ChiselStationMenu) container).getTe();
					station.getCurrentRecipe();
					station.craftEvent();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}