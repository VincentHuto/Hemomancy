package com.vincenthuto.hemomancy.common.item.itemhandler;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public class CharmItemHandler implements IItemHandlerModifiable {
	private final ItemStack itemStack;

	public CharmItemHandler(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		if (existing.getCount() <= 0)
			return ItemStack.EMPTY;

		int toExtract = Math.min(amount, existing.getMaxStackSize());

		if (existing.getCount() <= toExtract) {
			if (!simulate) {
				setStackInSlot(slot, ItemStack.EMPTY);
			}
			return existing;
		} else {
			if (!simulate) {
				setStackInSlot(slot, copyWithSize(existing, existing.getCount() - toExtract));
			}
			return copyWithSize(existing, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	// Ensure that the serialization is always compatible, even if it were to change
	// upstream
	@Override
	public int getSlots() {
		return Mth.clamp(getTag().getInt("Size"), 2, 9);
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		ListTag tagList = getTag().getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag itemTags = tagList.getCompound(i);
			if (itemTags.getInt("Slot") != slot)
				continue;

			return ItemStack.parseOptional(provider(), itemTags);
		}

		return ItemStack.EMPTY;
	}

	private CompoundTag getTag() {
		if (!itemStack.has(DataComponents.CUSTOM_DATA)) {
			itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()));
		}
		return itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

		if (stack.getCount() <= 0)
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = getStackInSlot(slot);

		int limit = stack.getMaxStackSize();

		if (existing.getCount() > 0) {
			if (!canStacksStack(stack, existing))
				return stack;

			limit -= existing.getCount();
		}

		if (limit <= 0)
			return stack;

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate) {
			if (existing.getCount() <= 0) {
				existing = reachedLimit ? copyWithSize(stack, limit) : stack;
			} else {
				existing.grow(reachedLimit ? limit : stack.getCount());
			}
			setStackInSlot(slot, existing);
		}

		return reachedLimit ? copyWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		validateSlotIndex(slot);
		CompoundTag rootTag = getTag();

		CompoundTag itemTag = null;
		boolean hasStack = stack.getCount() > 0;
		if (hasStack) {
			itemTag = (CompoundTag) stack.save(provider());
			itemTag.putInt("Slot", slot);
		}

		ListTag tagList = rootTag.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag existing = tagList.getCompound(i);
			if (existing.getInt("Slot") != slot)
				continue;

			if (hasStack)
				tagList.set(i, itemTag);
			else
				tagList.remove(i);
			return;
		}

		if (hasStack)
			tagList.add(itemTag);

		rootTag.put("Items", tagList);
		itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(rootTag));
	}

	private HolderLookup.Provider provider() {
		return ServerLifecycleHooks.getCurrentServer() != null
				? ServerLifecycleHooks.getCurrentServer().registryAccess()
				: RegistryAccess.EMPTY;
	}

	private void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= getSlots())
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + getSlots() + ")");
	}

	private static ItemStack copyWithSize(ItemStack stack, int size) {
		if (size <= 0) {
			return ItemStack.EMPTY;
		}
		return stack.copyWithCount(size);
	}

	private static boolean canStacksStack(ItemStack first, ItemStack second) {
		return ItemStack.isSameItemSameComponents(first, second);
	}
}
