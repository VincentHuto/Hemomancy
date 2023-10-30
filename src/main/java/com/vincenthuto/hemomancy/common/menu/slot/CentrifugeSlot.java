package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.registry.ItemInit;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CentrifugeSlot extends Slot {

	public CentrifugeSlot(Container pContainer, int pSlot, int pX, int pY) {
		super(pContainer, pSlot, pX, pY);
	}

	@Override
	public boolean mayPlace(ItemStack pStack) {
		return pStack.getItem() == ItemInit.bloody_vial.get();
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

}
