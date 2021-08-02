package com.huto.hemomancy.capa.rune;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;

public class RunesApi {

	// Retrieves the Runes inventory capability handler for the supplied player
	public static LazyOptional<IRunesItemHandler> getRunesHandler(Player player) {
		return player.getCapability(RunesCapabilities.RUNES);
	}

	public static int isRuneEquipped(Player player, Item mindrune) {
		return getRunesHandler(player).map(handler -> {
			for (int a = 0; a < handler.getSlots(); a++) {
				if (!handler.getStackInSlot(a).isEmpty() && handler.getStackInSlot(a).getItem() == mindrune)
					return a;
			}
			return -1;
		}).orElse(-1);
	}
}