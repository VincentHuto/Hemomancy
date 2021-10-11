package com.vincenthuto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.item.rune.pattern.ItemRunePattern;
import com.vincenthuto.hemomancy.itemhandler.RuneBinderItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotRuneBinder extends SlotItemHandler {
	public SlotRuneBinder(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
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