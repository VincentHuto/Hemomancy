package com.huto.hemomancy.containers.slot;

import com.huto.hemomancy.item.rune.ItemRuneBinder;
import com.huto.hemomancy.item.rune.pattern.ItemRunePattern;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotRunePattern extends Slot {

	public SlotRunePattern(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.getItem() instanceof ItemRunePattern || stack.getItem() instanceof ItemRuneBinder) {
			return true;
		} else {
			return false;
		}
	}

}
