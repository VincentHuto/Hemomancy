package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ScarStationSlot extends Slot {

	public ScarStationSlot(Container iItemHandlerModifiable, int index, int xPosition, int yPosition) {
		super(iItemHandlerModifiable, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof ItemKnapper;
	}

}
