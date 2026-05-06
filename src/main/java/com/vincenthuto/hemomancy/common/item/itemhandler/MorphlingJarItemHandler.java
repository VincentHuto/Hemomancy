package com.vincenthuto.hemomancy.common.item.itemhandler;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;

public class MorphlingJarItemHandler extends ItemStackHandler {
	private ItemStack itemStack;

	private int size;
	private boolean dirty = false;
	private boolean loaded = false;
	public MorphlingJarItemHandler(ItemStack itemStack, int size) {
		super(size);
		this.size = size;
		this.itemStack = itemStack;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		setSize(size);
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		for (int i = 0; i < tagList.size(); i++) {
			CompoundTag itemTags = tagList.getCompound(i);
			int slot = itemTags.getInt("Slot");

			if (slot >= 0 && slot < stacks.size()) {
				stacks.set(slot, ItemStack.parseOptional(provider, itemTags));
			}
		}
		onLoad();
	}

	@Nonnull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		dirty = true;
		return super.extractItem(slot, amount, simulate);
	}

	@Nonnull
	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

		// check for some other modded inventories
		if (stack.has(DataComponents.CUSTOM_DATA)) {
			CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			if (tag.contains("Items") || tag.contains("Inventory"))
				return stack;
		}

		// check for itemhandler capability
		if (stack.getCapability(Capabilities.ItemHandler.ITEM) != null)
			return stack;
		dirty = true;
		return super.insertItem(slot, stack, simulate);
	}

	public void load() {
		load(itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
	}

	public void load(@Nonnull CompoundTag nbt) {
		if (nbt.contains("Inventory"))
			deserializeNBT(provider(), nbt.getCompound("Inventory"));
	}

	public void loadIfNotLoaded() {
		if (!loaded)
			load();
		loaded = true;
	}

	@Override
	protected void onContentsChanged(int slot) {
		super.onContentsChanged(slot);
		dirty = true;
	}

	public void save() {
		if (dirty) {
			CompoundTag nbt = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			nbt.put("Inventory", serializeNBT(provider()));
			itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
			dirty = false;
		}
	}

	public void setDirty() {
		this.dirty = true;
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		validateSlotIndex(slot);
		if (!ItemStack.isSameItemSameComponents(stack, stacks.get(slot))) {
			onContentsChanged(slot);
		}
		this.stacks.set(slot, stack);
	}

	private HolderLookup.Provider provider() {
		return ServerLifecycleHooks.getCurrentServer() != null
				? ServerLifecycleHooks.getCurrentServer().registryAccess()
				: RegistryAccess.EMPTY;
	}

}