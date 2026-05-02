package com.vincenthuto.hemomancy.common.menu.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.itemhandler.ScarBinderItemHandler;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ScarBinderSlot extends SlotItemHandler {
	public ScarBinderSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if (stack.getItem() instanceof ItemScarPattern) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (getItemHandler() instanceof ScarBinderItemHandler)
			((ScarBinderItemHandler) getItemHandler()).setDirty();
	}
}