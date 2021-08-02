package com.huto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.huto.hemomancy.item.morphlings.ItemMorphling;
import com.huto.hemomancy.itemhandler.MorphlingJarItemHandler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotMorphlingJar extends SlotItemHandler {
	public SlotMorphlingJar(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(@Nonnull ItemStack stack) {
		return 1;
	}

	@Override
	public boolean mayPlace(@Nonnull ItemStack stack) {
		// check for shulkers.
		if (stack.getItem() instanceof ItemMorphling) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (getItemHandler() instanceof MorphlingJarItemHandler)
			((MorphlingJarItemHandler) getItemHandler()).setDirty();
	}
}