package com.huto.hemomancy.containers.slot;

import com.huto.hemomancy.item.tool.ItemKnapper;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotChisel extends Slot {

	public SlotChisel(IInventory iItemHandlerModifiable, int index, int xPosition, int yPosition) {
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
