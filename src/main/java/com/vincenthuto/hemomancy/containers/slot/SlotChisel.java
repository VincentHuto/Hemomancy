package com.vincenthuto.hemomancy.containers.slot;

import com.vincenthuto.hutoslib.common.item.ItemKnapper;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotChisel extends Slot {

	public SlotChisel(Container iItemHandlerModifiable, int index, int xPosition, int yPosition) {
		super(iItemHandlerModifiable, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if (stack.getItem() instanceof ItemKnapper) {
			return true;
		} else {
			return false;
		}
	}

}
