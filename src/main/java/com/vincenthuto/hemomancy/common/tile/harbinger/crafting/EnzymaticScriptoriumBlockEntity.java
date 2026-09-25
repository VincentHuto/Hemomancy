package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.EnzymeItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EnzymaticScriptoriumBlockEntity extends BaseContainerBlockEntity implements com.vincenthuto.hemomancy.common.tile.IBloodReservoir {
    public static final double BLOOD_CAPACITY = 2000;
    public static final int ITEM = 8;
    public static final int LAPIS = 9;
    public static final int SHARD = 10;
    private NonNullList<ItemStack> items = NonNullList.withSize(11, ItemStack.EMPTY);
    private final int[] selected = new int[8];
    private boolean riteLocked;

    public EnzymaticScriptoriumBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.enzymatic_scriptorium.get(), pos, state);
    }

    @Override public com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume getBloodCapability() {
        var volume = getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.BLOCK_BLOOD_VOLUME);
        volume.setActive(true);
        volume.setMaxBloodVolume(BLOOD_CAPACITY);
        return volume;
    }

    @Override public void onLoad() {
        super.onLoad();
        getBloodCapability();
    }

    public int selected(int index) { return selected[index]; }
    public int selectedCount() {
        int count = 0;
        for (int value : selected) count += value;
        return count;
    }

    public boolean select(int index) {
        if (riteLocked || index < 0 || index >= 8) return false;
        int stored = items.get(index).getCount();
        selected[index] = selected[index] >= stored || selectedCount() >= 3 ? 0 : selected[index] + 1;
        sync();
        return true;
    }

    public void consumeSelection() {
        for (int i = 0; i < 8; i++) {
            items.get(i).shrink(selected[i]);
            selected[i] = 0;
        }
        sync();
    }

    public void consumeEnzymes(int[] costs) {
        for (int i = 0; i < 8; i++) {
            items.get(i).shrink(costs[i]);
            selected[i] = 0;
        }
        sync();
    }

    public boolean isRiteLocked() { return riteLocked; }
    public void setRiteLocked(boolean value) { riteLocked = value; sync(); }

    public boolean hasRiteEnzymes(int each) {
        if (!items.get(ITEM).isEmpty() || !items.get(LAPIS).isEmpty() || !items.get(SHARD).isEmpty()) return false;
        for (int i = 0; i < 8; i++) if (items.get(i).getCount() != each) return false;
        return true;
    }

    public void consumeRiteEnzymes(int each) {
        for (int i = 0; i < 8; i++) items.get(i).shrink(each);
        riteLocked = false;
        sync();
    }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public void setItem(int slot, ItemStack stack) {
        if (riteLocked) return;
        if (slot < 8) {
            if (!stack.isEmpty() && (!(stack.getItem() instanceof EnzymeItem enzyme)
                    || enzyme.getTend() != EnumBloodTendency.values()[slot])) return;
            if (stack.getCount() > 64) stack.setCount(64);
            selected[slot] = Math.min(selected[slot], stack.getCount());
        }
        items.set(slot, stack);
        sync();
    }

    @Override public ItemStack removeItem(int slot, int amount) {
        if (riteLocked) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (slot < 8) selected[slot] = Math.min(selected[slot], items.get(slot).getCount());
        sync();
        return result;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (riteLocked) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.takeItem(items, slot);
        if (slot < 8) selected[slot] = 0;
        sync();
        return result;
    }

    @Override protected Component getDefaultName() { return Component.translatable("container.hemomancy.enzymatic_scriptorium"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new EnzymaticScriptoriumMenu(id, inventory, this);
    }
    @Override public int getContainerSize() { return 11; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override public boolean stillValid(Player player) {
        return !isRemoved() && player.distanceToSqr(worldPosition.getX() + .5, worldPosition.getY() + .5,
                worldPosition.getZ() + .5) <= 64;
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(11, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        int[] saved = tag.getIntArray("Selected");
        for (int i = 0; i < 8; i++) selected[i] = i < saved.length ? Math.clamp(saved[i], 0, Math.min(3, items.get(i).getCount())) : 0;
        riteLocked = tag.getBoolean("RiteLocked");
        getBloodCapability().setBloodVolume(Math.clamp(tag.getDouble("BloodVolume"), 0, BLOOD_CAPACITY));
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putIntArray("Selected", selected);
        tag.putBoolean("RiteLocked", riteLocked);
        tag.putDouble("BloodVolume", getBloodVolume());
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        if (packet.getTag() != null) loadAdditional(packet.getTag(), registries);
    }
}
