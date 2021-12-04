package com.vincenthuto.hemomancy.containers.slot;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.item.morphlings.ItemMorphling;
import com.vincenthuto.hemomancy.itemhandler.MorphlingJarItemHandler;

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
		return stack.getItem() instanceof ItemMorphling;


	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (getItemHandler() instanceof MorphlingJarItemHandler)
			((MorphlingJarItemHandler) getItemHandler()).setDirty();
	}
}