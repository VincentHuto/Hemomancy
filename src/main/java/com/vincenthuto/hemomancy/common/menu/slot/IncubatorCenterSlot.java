package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingPolyp;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot that only accepts morphling polyps or morphling items (center input).
 */
public class IncubatorCenterSlot extends Slot {

	public IncubatorCenterSlot(Container container, int index, int x, int y) {
		super(container, index, x, y);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof ItemMorphlingPolyp || stack.getItem() instanceof IMorphling;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}
}
