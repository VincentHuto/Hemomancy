package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.PhlebotomistsCabinetBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashSet;
import java.util.Set;

public class PhlebotomistsCabinetBlockEntity extends BlockEntity implements MenuProvider, SpecimenContainer {
    private final CabinetStorage storage = new CabinetStorage(this::contentsChanged);
    private final Set<Player> viewers = new HashSet<>();
    private int closeDelay;
    private float progress, previousProgress;
    private final int[] displayCounts = new int[CabinetStorage.SIZE];
    private final ItemStack[] display = new ItemStack[CabinetStorage.SIZE];

    public PhlebotomistsCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.phlebotomists_cabinet.get(), pos, state);
        java.util.Arrays.fill(display, ItemStack.EMPTY);
    }
    @Override public BlockEntity blockEntity() { return this; }
    public CabinetStorage storage() { return storage; }
    public ItemStack displaySpecimen(int slot) {
        return level != null && level.isClientSide ? display[slot].copy() : storage.getStackInSlot(slot);
    }
    public int displayCount(int slot) {
        return level != null && level.isClientSide ? displayCounts[slot] : storage.count(slot);
    }
    private void contentsChanged(boolean displayChanged) {
        com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenDisplayState.sync(this, storage);
        setChanged();
        if (level == null || level.isClientSide) return;
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    public boolean hasSpecimens() {
        if (level != null && level.isClientSide)
            return java.util.Arrays.stream(display).anyMatch(s -> !s.isEmpty());
        return storage.totalCount() != 0;
    }
    public void startOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator()) return;
        viewers.add(player);
        closeDelay = 5;
        setOpen(true);
    }
    public void stopOpen(Player player) { viewers.remove(player); }
    private void setOpen(boolean open) {
        var state = getBlockState();
        if (state.getValue(PhlebotomistsCabinetBlock.OPEN) == open) return;
        level.setBlock(worldPosition, state.setValue(PhlebotomistsCabinetBlock.OPEN, open), Block.UPDATE_CLIENTS);
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE,
                SoundSource.BLOCKS, 0.35F, 0.8F);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, PhlebotomistsCabinetBlockEntity cabinet) {
        com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenDisplayState.sync(cabinet, cabinet.storage);
        if (level.isClientSide) {
            cabinet.previousProgress = cabinet.progress;
            cabinet.progress = Mth.clamp(cabinet.progress + (state.getValue(PhlebotomistsCabinetBlock.OPEN) ? 0.1F : -0.1F), 0, 1);
        } else {
            cabinet.viewers.removeIf(p -> !p.isAlive() || p.isRemoved() || p.isSpectator()
                    || !(p.containerMenu instanceof PhlebotomistsCabinetMenu menu)
                    || !menu.isCabinet(cabinet) || !menu.stillValid(p)
                    || (p instanceof net.minecraft.server.level.ServerPlayer sp && sp.hasDisconnected()));
            if (!cabinet.viewers.isEmpty()) cabinet.closeDelay = 5;
            else if (cabinet.closeDelay > 0) cabinet.closeDelay--;
            else cabinet.setOpen(false);
        }
    }
    public float doorProgress(float partialTick) { return Mth.lerp(partialTick, previousProgress, progress); }
    @Override public Component getDisplayName() { return Component.translatable("block.hemomancy.phlebotomists_cabinet"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new PhlebotomistsCabinetMenu(id, inventory, this);
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Storage", storage.save(provider));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Display", Tag.TAG_LIST)) {
            var entries = tag.getList("Display", Tag.TAG_COMPOUND);
            var counts = tag.getIntArray("DisplayCounts");
            for (int i = 0; i < displayCounts.length; i++)
                displayCounts[i] = i < counts.length ? Mth.clamp(counts[i], 0, 64) : 0;
            for (int i = 0; i < display.length; i++)
                display[i] = i < entries.size() ? ItemStack.parseOptional(provider, entries.getCompound(i)) : ItemStack.EMPTY;
        } else {
            storage.load(tag.getCompound("Storage"), provider);
            viewers.clear();
            closeDelay = 0;
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        var entries = new ListTag();
        for (int i = 0; i < CabinetStorage.SIZE; i++) entries.add(storage.getStackInSlot(i).saveOptional(provider));
        tag.put("Display", entries);
        int[] counts = new int[CabinetStorage.SIZE];
        for (int i = 0; i < counts.length; i++) counts[i] = storage.count(i);
        tag.putIntArray("DisplayCounts", counts);
        return tag;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        // Ctrl-pick must obey the same stationary-storage rule as normal drops.
        stack.remove(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA);
        stack.set(net.minecraft.core.component.DataComponents.BLOCK_STATE,
                net.minecraft.world.item.component.BlockItemStateProperties.EMPTY.with(
                        PhlebotomistsCabinetBlock.GLAZED, getBlockState().getValue(PhlebotomistsCabinetBlock.GLAZED)));
    }
}
