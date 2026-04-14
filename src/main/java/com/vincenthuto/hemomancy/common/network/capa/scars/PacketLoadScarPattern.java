package com.vincenthuto.hemomancy.common.network.capa.scars;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.menu.ScarStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sent from client to server when a player places an ItemScarPattern
 * into the pattern slot. The server will read the pattern from the item
 * and auto-fill the scar grid.
 */
public class PacketLoadScarPattern {

	public PacketLoadScarPattern() {
	}

	public static void encode(PacketLoadScarPattern msg, FriendlyByteBuf buf) {
	}

	public static PacketLoadScarPattern decode(FriendlyByteBuf buf) {
		return new PacketLoadScarPattern();
	}

	public static class Handler {

		public static void handle(final PacketLoadScarPattern msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof ScarStationMenu) {
					ScarStationBlockEntity station = ((ScarStationMenu) container).getTe();
					station.tryLoadPatternFromSlot();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}

