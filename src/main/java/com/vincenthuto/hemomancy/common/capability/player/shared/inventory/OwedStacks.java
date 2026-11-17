package com.vincenthuto.hemomancy.common.capability.player.shared.inventory;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

/**
 * Exact stacks owed to a player until they really land, behind a {@code PendingDelivery} claim: the Scale Grip-bound
 * stacks a death took off (F-4), and one-time NPC rewards and the initiation kit (PT-3, PT-16).
 */
public final class OwedStacks implements INBTSerializable<CompoundTag> {
	private static final String STACKS = "Stacks";
	private final List<ItemStack> stacks = new ArrayList<>();

	public List<ItemStack> owed() {
		return stacks.stream().map(ItemStack::copy).toList();
	}

	public void park(List<ItemStack> saved) {
		saved.forEach(stack -> {
			if (!stack.isEmpty()) stacks.add(stack.copy());
		});
	}

	public void settle(List<ItemStack> left) {
		stacks.clear();
		park(left);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (ItemStack stack : stacks) if (!stack.isEmpty()) list.add(stack.save(provider));
		CompoundTag tag = new CompoundTag();
		tag.put(STACKS, list);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		stacks.clear();
		ListTag list = tag.getList(STACKS, Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) ItemStack.parse(provider, list.getCompound(i)).ifPresent(stacks::add);
	}
}
