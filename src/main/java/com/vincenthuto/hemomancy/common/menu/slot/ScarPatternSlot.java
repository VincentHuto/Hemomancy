package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.item.scar.ItemScarBinder;
import com.vincenthuto.hemomancy.common.item.scar.pattern.ItemScarPattern;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ScarPatternSlot extends Slot {

	public ScarPatternSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return stack.getItem() instanceof ItemScarPattern || stack.getItem() instanceof ItemScarBinder;
	}

}
