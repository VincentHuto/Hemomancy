package com.vincenthuto.hemomancy.gui.radial;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.itemhandler.AbstractItemHandler;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;

public class ItemInventoryBase
extends AbstractItemHandler {
    private static final String TAG_ITEMS = "Items";
    protected final ItemStack stack;

    public ItemInventoryBase(ItemStack stack) {
        super(54);
        this.stack = stack;
        this.setup();
    }

    public ItemInventoryBase(ItemStack stack, int numSlots) {
        super(numSlots);
        this.stack = stack;
        this.setup();
    }

    public NonNullList<ItemStack> getAllItems() {
        NonNullList items = NonNullList.create();
        this.stacks.forEach(is -> items.add(is.getStackOriginal()));
        return items;
    }

    public NonNullList<ItemStack> getNonAirItems() {
        NonNullList items = NonNullList.create();
        this.stacks.forEach(is -> {
            if (!is.isEmpty()) {
                items.add(is.getStackOriginal());
            }
        });
        return items;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    protected boolean isSameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && ItemStack.tagMatches(a, b);
    }

    protected void moveItemsBetweenStacks(ItemStack source, ItemStack dest, int slot) {
        int i = Math.min(this.getSlotLimit(slot, dest), dest.getMaxStackSize());
        int j = Math.min(source.getCount(), i - dest.getCount());
        if (j > 0) {
            dest.grow(j);
            source.shrink(j);
            this.markDirty();
        }
    }

    protected void moveItemToEmptySlots(ItemStack stack) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);
            if (!itemstack.isEmpty()) continue;
            this.setStackInSlot(i, stack.copy());
            stack.setCount(0);
            return;
        }
    }

    protected void moveItemToOccupiedSlotsWithSameType(ItemStack stack) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemstack = this.getStackInSlot(i);
            if (!this.isSameItem(itemstack, stack)) continue;
            this.moveItemsBetweenStacks(stack, itemstack, i);
            if (!stack.isEmpty()) continue;
            return;
        }
    }

    public void readItemStack() {
        if (this.stack.getTag() != null && this.stack.getTag().contains(TAG_ITEMS)) {
            if (this.stack.getTag().getCompound(TAG_ITEMS).contains("slots")) {
                this.deserialize(this.stack.getTag().getCompound(TAG_ITEMS));
            } else {
                this.readNBT(this.stack.getTag());
            }
        }
    }

    private void readNBT(CompoundTag compound) {
        Hemomancy.LOGGER.info("Upconverting old ritual kit to Practitioner's Pouch");
        NonNullList list = NonNullList.withSize(this.getSlots(), (Object)ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, list);
        int mySlotIndex = 0;
        for (Object element : list) {
            if (((ItemStack)element).isEmpty()) continue;
            this.setStackInSlot(mySlotIndex++, (ItemStack)element);
        }
        this.writeItemStack();
    }

    public void setup() {
        this.readItemStack();
    }

    public void writeItemStack() {
        if (this.isEmpty()) {
            this.stack.removeTagKey(TAG_ITEMS);
        } else {
            CompoundTag tag = this.stack.getOrCreateTag();
            tag.put(TAG_ITEMS, this.serialize());
        }
    }
}

