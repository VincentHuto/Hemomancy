package com.vincenthuto.hemomancy.common.network.capa.runes;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.menu.ChiselStationMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.ChiselStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sent from client to server when a player places an ItemRunePattern
 * into the pattern slot. The server will read the pattern from the item
 * and auto-fill the rune grid.
 */
public class PacketLoadChiselPattern {

	public PacketLoadChiselPattern() {
	}

	public static void encode(PacketLoadChiselPattern msg, FriendlyByteBuf buf) {
	}

	public static PacketLoadChiselPattern decode(FriendlyByteBuf buf) {
		return new PacketLoadChiselPattern();
	}

	public static class Handler {

		public static void handle(final PacketLoadChiselPattern msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				AbstractContainerMenu container = ctx.get().getSender().containerMenu;
				if (container instanceof ChiselStationMenu) {
					ChiselStationBlockEntity station = ((ChiselStationMenu) container).getTe();
					station.tryLoadPatternFromSlot();
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}

