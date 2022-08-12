/*
 * Decompiled with CFR 0.151.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  net.minecraft.core.NonNullList
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.items.ItemHandlerHelper
 */
package com.vincenthuto.hemomancy.radial;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public abstract class AbstractItemHandler implements IExtendedItemHandler {
	protected NonNullList<ItemStackEntry> stacks;
	protected Set<IInventoryListener> listeners = new HashSet<IInventoryListener>();

	public AbstractItemHandler() {
		this(1);
	}

	public AbstractItemHandler(int size) {
		this.stacks = NonNullList.withSize((int) size,  ItemStackEntry.EMPTY);
	}

	public AbstractItemHandler(NonNullList<ItemStackEntry> stacks) {
		this.stacks = stacks;
	}

	@Override
	public void enlarge(int size) {
		if (size < this.stacks.size()) {
			throw new RuntimeException("Cannot reduce the size of an AbstractItemHandler, currently contains: "
					+ this.stacks.size() + " slots, cannot reduce to " + size + " slots");
		}
		if (size == this.stacks.size()) {
			return;
		}
		NonNullList newList = NonNullList.withSize((int) size,  ItemStackEntry.EMPTY);
		for (int i = 0; i < this.stacks.size(); ++i) {
			newList.set(i,  ((ItemStackEntry) this.stacks.get(i)));
		}
		this.stacks = newList;
	}

	public abstract int getSlotLimit(int var1);

	public int getSlotLimit(int slot, ItemStack stack) {
		return this.getSlotLimit(slot);
	}

	@Override
	public int size() {
		return this.stacks.size();
	}

	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		this.validateSlotIndex(slot);
		this.stacks.set(slot,  new ItemStackEntry(stack));
		this.onContentsChanged(slot);
	}

	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		this.validateSlotIndex(slot);
		return ((ItemStackEntry) this.stacks.get(slot)).getStackOriginal();
	}

	@Override
	public long getCountInSlot(int slot) {
		this.validateSlotIndex(slot);
		return ((ItemStackEntry) this.stacks.get(slot)).getCount();
	}

	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		boolean reachedLimit;
		if (stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if (!this.isItemValid(slot, stack)) {
			return stack;
		}
		this.validateSlotIndex(slot);
		ItemStackEntry entry = (ItemStackEntry) this.stacks.get(slot);
		ItemStack existing = entry.getStackOriginal();
		int limit = this.getSlotLimit(slot, stack);
		if (!existing.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack((ItemStack) stack, (ItemStack) existing)) {
				return stack;
			}
			limit -= existing.getCount();
		}
		if (limit <= 0) {
			return stack;
		}
		boolean bl = reachedLimit = stack.getCount() > limit;
		if (!simulate) {
			if (existing.isEmpty()) {
				if (reachedLimit) {
					entry = new ItemStackEntry(stack.copy());
					entry.setCount(limit);
					this.stacks.set(slot,  entry);
				} else {
					entry = new ItemStackEntry(stack);
					this.stacks.set(slot,  entry);
				}
			} else {
				entry.grow(stack.getCount());
			}
			this.onContentsChanged(slot);
		}
		return reachedLimit ? ItemHandlerHelper.copyStackWithSize((ItemStack) stack, (int) (stack.getCount() - limit))
				: ItemStack.EMPTY;
	}

	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0) {
			return ItemStack.EMPTY;
		}
		this.validateSlotIndex(slot);
		ItemStackEntry entry = (ItemStackEntry) this.stacks.get(slot);
		ItemStack existing = entry.getStackOriginal();
		if (existing.isEmpty()) {
			return ItemStack.EMPTY;
		}
		int toExtract = Math.min(amount, existing.getMaxStackSize());
		if (existing.getCount() <= toExtract) {
			if (!simulate) {
				this.stacks.set(slot,  ItemStackEntry.EMPTY);
				this.onContentsChanged(slot);
				return existing;
			}
			return existing.copy();
		}
		if (!simulate) {
			entry = new ItemStackEntry(
					ItemHandlerHelper.copyStackWithSize((ItemStack) existing, (int) (existing.getCount() - toExtract)));
			this.stacks.set(slot,  entry);
			this.onContentsChanged(slot);
		}
		return ItemHandlerHelper.copyStackWithSize((ItemStack) existing, (int) toExtract);
	}

	public abstract boolean isItemValid(int var1, @Nonnull ItemStack var2);

	protected void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= this.stacks.size()) {
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.stacks.size() + ")");
		}
	}

	public void onContentsChanged(int slot) {
		for (IInventoryListener listener : this.listeners) {
			listener.inventoryChanged(this, slot);
		}
	}

	@Override
	public void addListener(IInventoryListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public CompoundTag serialize() {
		CompoundTag result = new CompoundTag();
		result.putInt("slots", this.size());
		for (int i = 0; i < this.size(); ++i) {
			ItemStackEntry entry = (ItemStackEntry) this.stacks.get(i);
			if (entry.isEmpty())
				continue;
			result.put("" + i, entry.serialize());
		}
		return result;
	}

	@Override
	public void deserialize(CompoundTag result) {
		int size = result.getInt("slots");
		this.enlarge(Math.max(size, this.size()));
		for (int i = 0; i < size; ++i) {
			ItemStackEntry entry = ItemStackEntry.deserialize(result.get("" + i));
			this.stacks.set(i,  entry);
		}
	}

	public boolean isEmpty() {
		for (int i = 0; i < this.size(); ++i) {
			if (this.getStackInSlot(i).isEmpty())
				continue;
			return false;
		}
		return true;
	}
}
