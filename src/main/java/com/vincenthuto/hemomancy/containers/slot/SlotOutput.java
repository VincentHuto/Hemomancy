package com.vincenthuto.hemomancy.containers.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotOutput extends Slot {

	public SlotOutput(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}

}
