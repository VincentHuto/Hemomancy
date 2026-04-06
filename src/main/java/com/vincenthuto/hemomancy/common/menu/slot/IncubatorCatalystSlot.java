package com.vincenthuto.hemomancy.common.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot that accepts catalyst ingredients or enzyme items for the incubator.
 * Any item is allowed here since polyp recipes use vanilla items as catalysts.
 */
public class IncubatorCatalystSlot extends Slot {

	public IncubatorCatalystSlot(Container container, int index, int x, int y) {
		super(container, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return true; // Catalysts are varied — allow anything
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}
}
