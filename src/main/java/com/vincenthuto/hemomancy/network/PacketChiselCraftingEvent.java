package com.vincenthuto.hemomancy.network;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.container.MenuChiselStation;
import com.vincenthuto.hemomancy.tile.BlockEntityChiselStation;

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
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof MenuChiselStation) {
					BlockEntityChiselStation station = ((MenuChiselStation) container).getTe();
					station.craftEvent();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}