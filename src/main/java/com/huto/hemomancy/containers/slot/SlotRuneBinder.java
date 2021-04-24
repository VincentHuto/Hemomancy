package com.huto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.huto.hemomancy.item.rune.pattern.ItemRunePattern;
import com.huto.hemomancy.itemhandler.RuneBinderItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotRuneBinder extends SlotItemHandler {
	public SlotRuneBinder(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getItemStackLimit(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean isItemValid(@Nonnull ItemStack stack) {
		if (stack.getItem() instanceof ItemRunePattern) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		if (getItemHandler() instanceof RuneBinderItemHandler)
			((RuneBinderItemHandler) getItemHandler()).setDirty();
	}
}