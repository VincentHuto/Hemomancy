package com.vincenthuto.hemomancy.common.item.itemhandler;

import com.vincenthuto.hemomancy.client.util.ItemInventoryClientAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/** Fixed-size legacy item inventory. Call save after a transaction, or setDirty after editing a held stack in place. */
public abstract class StackBackedItemHandler extends ItemStackHandler {
    protected final ItemStack itemStack;
    private final int inventorySize;
    private boolean dirty;
    private boolean loaded;

    protected StackBackedItemHandler(ItemStack itemStack, int size) {
        super(size);
        this.itemStack = itemStack;
        this.inventorySize = size;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        // Keep the item's capacity even when old NBT contains a different Size.
        setSize(inventorySize);
        ListTag items = nbt.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag item = items.getCompound(i);
            int slot = item.getInt("Slot");
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, ItemStack.parseOptional(provider, item));
            }
        }
        onLoad();
        loaded = true;
        dirty = false;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Items") || tag.contains("Inventory")
                || stack.getCapability(Capabilities.ItemHandler.ITEM) != null) {
            return stack;
        }
        return super.insertItem(slot, stack, simulate);
    }

    public void load() {
        load(itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
    }

    public void load(CompoundTag root) {
        deserializeNBT(provider(), root.getCompound("Inventory"));
    }

    public void loadIfNotLoaded() {
        if (!loaded) load();
    }

    @Override
    protected void onContentsChanged(int slot) {
        dirty = true;
    }

    public void setDirty() {
        dirty = true;
    }

    public void save() {
        if (!dirty) return;
        CompoundTag root = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        root.put("Inventory", serializeNBT(provider()));
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        dirty = false;
    }

    private HolderLookup.Provider provider() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) return server.registryAccess();
        if (FMLEnvironment.dist.isClient()) return ItemInventoryClientAccess.registryAccess();
        return RegistryAccess.EMPTY;
    }
}
