package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.PhlebotomistsFieldCaseBlock;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.item.component.FieldCaseContents;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.*;

public class PhlebotomistsFieldCaseBlockEntity extends BlockEntity implements MenuProvider, SpecimenContainer {
    private final CabinetStorage storage = new CabinetStorage(16, true, this::contentsChanged);
    private final Set<Player> viewers = new HashSet<>();
    private final ItemStack[] display = new ItemStack[9];
    private float progress, previousProgress;
    private int closeDelay, transfers;
    private long closedAt;
    private boolean packing;
    private Player pendingBreak;
    // Invalid decoded contents remain saved and cannot be opened or overwritten.
    private FieldCaseContents unreadable;

    public PhlebotomistsFieldCaseBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.phlebotomists_field_case.get(), pos, state);
        Arrays.fill(display, ItemStack.EMPTY);
    }
    @Override public BlockEntity blockEntity() { return this; }
    @Override public CabinetStorage storage() { return storage; }
    @Override public boolean available() { return !packing && pendingBreak == null && unreadable == null && !isRemoved(); }
    @Override public boolean beginTransfer() {
        if (!available()) return false;
        transfers++;
        return true;
    }
    @Override public void endTransfer() {
        if (transfers <= 0) throw new IllegalStateException("Unbalanced field-case transfer");
        if (--transfers == 0 && pendingBreak != null) {
            var player = pendingBreak;
            pendingBreak = null;
            pack(player, true);
        }
    }
    public FieldCaseContents contents() { return unreadable != null ? unreadable : new FieldCaseContents(storage.snapshot()); }
    public void readContents(FieldCaseContents contents) {
        if (!contents.valid()) {
            unreadable = contents;
            LogUtils.getLogger().warn("Field case at {} has invalid contents; keeping data and refusing access", worldPosition);
        } else {
            storage.restore(contents.entries());
            unreadable = null;
        }
    }
    public boolean hasSpecimens() {
        return unreadable != null || (level != null && level.isClientSide
                ? Arrays.stream(display).anyMatch(s -> !s.isEmpty()) : storage.totalCount() > 0);
    }
    public ItemStack displaySpecimen(int cell) { return level != null && level.isClientSide ? display[cell].copy() : storage.getStackInSlot(cell); }
    private void contentsChanged(boolean displayChanged) {
        com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenDisplayState.sync(this, storage);
        setChanged();
        if (displayChanged && level != null && !level.isClientSide)
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
    @Override public void startOpen(Player player) {
        if (level == null || level.isClientSide || player.isSpectator() || !available()) return;
        viewers.add(player);
        closeDelay = 5;
        setOpen(true);
    }
    @Override public void stopOpen(Player player) { viewers.remove(player); }
    private void reconcileViewers() {
        viewers.removeIf(p -> !p.isAlive() || p.isRemoved() || p.isSpectator()
                || !(p.containerMenu instanceof PhlebotomistsCabinetMenu menu) || !menu.isCabinet(this)
                || p.level() != level || p.distanceToSqr(worldPosition.getCenter()) > 64
                || p instanceof ServerPlayer sp && sp.hasDisconnected());
    }
    private void setOpen(boolean open) {
        var state = getBlockState();
        if (state.getValue(PhlebotomistsFieldCaseBlock.OPEN) == open) return;
        level.setBlock(worldPosition, state.setValue(PhlebotomistsFieldCaseBlock.OPEN, open), Block.UPDATE_CLIENTS);
        if (!open) closedAt = level.getGameTime() + 10;
        level.playSound(null, worldPosition, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, .35F, 1.2F);
    }
    public static void tick(Level level, BlockPos pos, BlockState state, PhlebotomistsFieldCaseBlockEntity be) {
        com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenDisplayState.sync(be, be.storage);
        if (level.isClientSide) {
            be.previousProgress = be.progress;
            be.progress = Mth.clamp(be.progress + (state.getValue(PhlebotomistsFieldCaseBlock.OPEN) ? .1F : -.1F), 0, 1);
        } else {
            be.reconcileViewers();
            if (!be.viewers.isEmpty()) be.closeDelay = 5;
            else if (be.closeDelay > 0) be.closeDelay--;
            else be.setOpen(false);
        }
    }
    public float lidProgress(float partialTick) { return Mth.lerp(partialTick, previousProgress, progress); }
    public boolean canPickUp() {
        reconcileViewers();
        return !packing && pendingBreak == null && transfers == 0 && viewers.isEmpty()
                && !getBlockState().getValue(PhlebotomistsFieldCaseBlock.OPEN) && level.getGameTime() >= closedAt;
    }
    /** One server-thread transaction delivers the filled item before removing its source. */
    public boolean pack(Player player, boolean breaking) {
        if (level == null || level.isClientSide || isRemoved() || packing || pendingBreak != null
                || player.isSpectator() || !player.isAlive() || player.level() != level
                || player.distanceToSqr(worldPosition.getCenter()) > 64) return false;
        if (transfers > 0) {
            if (breaking) pendingBreak = player;
            return false;
        }
        if (!breaking && !canPickUp()) return false;
        packing = true;
        int inventorySlot = -1;
        ItemEntity drop = null;
        try {
            if (breaking) {
                for (var viewer : List.copyOf(viewers)) {
                    if (viewer.containerMenu instanceof PhlebotomistsCabinetMenu menu && menu.isCabinet(this)) {
                        if (viewer instanceof ServerPlayer server) server.closeContainer();
                        else { menu.removed(viewer); viewer.containerMenu = viewer.inventoryMenu; }
                    }
                }
                viewers.clear();
                setOpen(false);
            }
            var item = new ItemStack(BlockInit.phlebotomists_field_case.get());
            item.set(DataComponentInit.FIELD_CASE_CONTENTS.get(), contents());
            if (!breaking) inventorySlot = player.getInventory().getFreeSlot();
            if (inventorySlot >= 0) {
                player.getInventory().setItem(inventorySlot, item);
                player.getInventory().setChanged();
            } else {
                drop = safeDrop(item, player);
                if (drop == null) return false;
                var expected = item.copy();
                if (!level.addFreshEntity(drop) || drop.isRemoved() || !ItemStack.matches(expected, drop.getItem())) {
                    drop.discard();
                    return false;
                }
            }
            if (level.getBlockEntity(worldPosition) != this || !level.removeBlock(worldPosition, false)) {
                if (inventorySlot >= 0) player.getInventory().setItem(inventorySlot, ItemStack.EMPTY);
                if (drop != null) drop.discard();
                return false;
            }
            player.inventoryMenu.broadcastChanges();
            return true;
        } finally { packing = false; }
    }
    private ItemEntity safeDrop(ItemStack stack, Player player) {
        var facing = getBlockState().getValue(PhlebotomistsFieldCaseBlock.FACING);
        Vec3[] candidates = {worldPosition.getCenter().add(facing.getStepX(), 0, facing.getStepZ()),
                player.position().add(0, .2, 0), worldPosition.above().getCenter()};
        for (var position : candidates) {
            var pos = BlockPos.containing(position);
            if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.below()).isEmpty()
                    || level.getBlockState(pos).is(net.minecraft.tags.BlockTags.FIRE)
                    || level.getBlockState(pos.below()).is(Blocks.CACTUS)
                    || level.getBlockState(pos.below()).is(Blocks.MAGMA_BLOCK)
                    || level.getBlockState(pos.below()).getBlock() instanceof CampfireBlock) continue;
            var item = new ItemEntity(level, position.x, position.y, position.z, stack);
            item.setDeltaMovement(Vec3.ZERO);
            item.setDefaultPickUpDelay();
            if (level.noCollision(item)) return item;
        }
        return null;
    }
    @Override public Component getDisplayName() { return Component.translatable("block.hemomancy.phlebotomists_field_case"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return available() ? new PhlebotomistsCabinetMenu(ContainerInit.phlebotomists_field_case.get(), id, inventory, this) : null;
    }
    @Override protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        readContents(input.getOrDefault(DataComponentInit.FIELD_CASE_CONTENTS.get(), FieldCaseContents.EMPTY));
    }
    @Override protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponentInit.FIELD_CASE_CONTENTS.get(), contents());
    }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        FieldCaseContents.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), contents())
                .resultOrPartial(LogUtils.getLogger()::error).ifPresent(data -> tag.put("Contents", data));
    }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("Display", Tag.TAG_LIST)) {
            var entries = tag.getList("Display", Tag.TAG_COMPOUND);
            for (int i = 0; i < 9; i++) display[i] = i < entries.size() ? ItemStack.parseOptional(provider, entries.getCompound(i)) : ItemStack.EMPTY;
        } else {
            FieldCaseContents.CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), tag.get("Contents"))
                    .resultOrPartial(LogUtils.getLogger()::error).ifPresent(this::readContents);
            viewers.clear();
            closeDelay = 0;
        }
    }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        var tag = new CompoundTag(); var entries = new ListTag();
        for (int i = 0; i < 9; i++) entries.add(storage.getStackInSlot(i).saveOptional(provider));
        tag.put("Display", entries);
        return tag;
    }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void saveToItem(ItemStack stack, HolderLookup.Provider provider) {
        // Creative pick-block is a template, never a second specimen inventory.
        stack.remove(DataComponentInit.FIELD_CASE_CONTENTS.get());
        stack.remove(DataComponents.BLOCK_ENTITY_DATA);
    }
}
