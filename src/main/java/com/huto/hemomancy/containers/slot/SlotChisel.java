package com.huto.hemomancy.containers.slot;

import com.huto.hemomancy.item.tool.ItemKnapper;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotChisel extends Slot {

	public SlotChisel(Container iItemHandlerModifiable, int index, int xPosition, int yPosition) {
		super(iItemHandlerModifiable, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (stack.getItem() instanceof ItemKnapper) {
			return true;
		} else {
			return false;
		}
	}

}
