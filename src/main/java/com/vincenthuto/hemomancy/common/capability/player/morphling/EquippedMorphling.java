package com.vincenthuto.hemomancy.common.capability.player.morphling;

import net.minecraft.world.item.ItemStack;

public class EquippedMorphling implements IEquippedMorphling {

	private ItemStack equippedMorphling = ItemStack.EMPTY;

	@Override
	public ItemStack getEquippedMorphling() {
		return equippedMorphling;
	}

	@Override
	public void setEquippedMorphling(ItemStack stack) {
		this.equippedMorphling = stack == null ? ItemStack.EMPTY : stack;
	}

	@Override
	public void clearMorphling() {
		this.equippedMorphling = ItemStack.EMPTY;
	}

	@Override
	public boolean hasMorphling() {
		return !equippedMorphling.isEmpty();
	}

}
