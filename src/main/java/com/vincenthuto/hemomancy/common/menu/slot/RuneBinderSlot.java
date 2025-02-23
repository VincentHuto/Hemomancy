package com.vincenthuto.hemomancy.common.menu.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.common.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.common.itemhandler.RuneBinderItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class RuneBinderSlot extends SlotItemHandler {
	public RuneBinderSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		if (stack.getItem() instanceof ItemRunePattern) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (getItemHandler() instanceof RuneBinderItemHandler)
			((RuneBinderItemHandler) getItemHandler()).setDirty();
	}
}