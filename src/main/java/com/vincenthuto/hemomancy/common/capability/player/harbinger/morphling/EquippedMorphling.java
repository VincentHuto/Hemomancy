package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class EquippedMorphling implements IEquippedMorphling, INBTSerializable<CompoundTag> {

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

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		if (!equippedMorphling.isEmpty()) {
			tag.put("EquippedMorphling", equippedMorphling.save(provider));
		}
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		if (nbt.contains("EquippedMorphling")) {
			equippedMorphling = ItemStack.parseOptional(provider, nbt.getCompound("EquippedMorphling"));
		} else {
			equippedMorphling = ItemStack.EMPTY;
		}
	}

}
