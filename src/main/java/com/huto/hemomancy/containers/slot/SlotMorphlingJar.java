package com.huto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.huto.hemomancy.item.morphlings.ItemMorphling;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotMorphlingJar extends SlotItemHandler {
	public SlotMorphlingJar(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getItemStackLimit(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean isItemValid(@Nonnull ItemStack stack) {
		// check for shulkers.
		if (stack.getItem() instanceof ItemMorphling) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void onSlotChanged() {
		super.onSlotChanged();
		if (getItemHandler() instanceof MorphlingJarItemHandler)
			((MorphlingJarItemHandler) getItemHandler()).setDirty();
	}
}