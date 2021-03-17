package com.huto.hemomancy.containers.slot;

import com.huto.hemomancy.item.rune.ItemRuneBinder;
import com.huto.hemomancy.item.rune.pattern.ItemRunePattern;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class SlotRunePattern extends Slot {

	public SlotRunePattern(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (stack.getItem() instanceof ItemRunePattern || stack.getItem() instanceof ItemRuneBinder) {
			return true;
		} else {
			return false;
		}
	}

}
