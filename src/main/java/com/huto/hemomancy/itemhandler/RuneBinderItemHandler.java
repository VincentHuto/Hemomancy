package com.huto.hemomancy.itemhandler;

import javax.annotation.Nonnull;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class RuneBinderItemHandler extends ItemStackHandler {
	public RuneBinderItemHandler(ItemStack itemStack, int size) {
		super(size);
		this.size = size;
		this.itemStack = itemStack;
	}

	private ItemStack itemStack;
	private int size;
	private boolean dirty = false;
	private boolean loaded = false;

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		// check for shulkers.
		if (stack.getItem() instanceof BlockItem) {
			if (((BlockItem) stack.getItem()).getBlock().isIn(BlockTags.SHULKER_BOXES)) {
				return stack;
			}
		}
		// check for some other modded inventories
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getTag();
			if (tag.contains("Items") || tag.contains("Inventory"))
				return stack;
		}

		// check for itemhandler capability
		if (stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent())
			return stack;
		dirty = true;
		return super.insertItem(slot, stack, simulate);
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		dirty = true;
		return super.extractItem(slot, amount, simulate);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		validateSlotIndex(slot);
		if (!ItemStack.areItemStackTagsEqual(stack, stacks.get(slot))) {
			onContentsChanged(slot);
		}
		this.stacks.set(slot, stack);
	}

	public void setDirty() {
		this.dirty = true;
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		dirty = true;
	}

	public void load() {
		load(itemStack.getOrCreateTag());
	}

	public void loadIfNotLoaded() {
		if (!loaded)
			load();
		loaded = true;
	}

	public void load(@Nonnull CompoundNBT nbt) {
		if (nbt.contains("Inventory"))
			deserializeNBT(nbt.getCompound("Inventory"));
	}

	public void save() {
		if (dirty) {
			CompoundNBT nbt = itemStack.getOrCreateTag();
			nbt.put("Inventory", serializeNBT());
			dirty = false;
		}
	}

	@Override
	public void deserializeNBT(CompoundNBT nbt) {
		setSize(size);
		ListNBT tagList = nbt.getList("Items", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundNBT itemTags = tagList.getCompound(i);
			int slot = itemTags.getInt("Slot");

			if (slot >= 0 && slot < stacks.size()) {
				stacks.set(slot, ItemStack.read(itemTags));
			}
		}
		onLoad();
	}

}