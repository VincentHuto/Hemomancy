package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.item.rune.ItemRuneBinder;
import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class RunePatternSlot extends Slot {

	public RunePatternSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof ItemRunePattern || stack.getItem() instanceof ItemRuneBinder;
	}

}
