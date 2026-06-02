package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.GourdvineTapBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.tile.BloodContainerTransfer;
import com.vincenthuto.hemomancy.common.tile.IBloodTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class GourdvineTapBlockEntity extends BlockEntity implements IBloodTile, Container {

    public static final int SLOT_GOURD = 0;
    public static final int INVENTORY_SIZE = 1;
    public static final double MAX_BLOOD = 10_000.0;

    private static final String TAG_BLOOD_LEVEL = "bloodLevel";

    private final NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private boolean initialised;

    public GourdvineTapBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.gourdvine_tap.get(), pos, state);
    }

    @Override
    public void onLoad() {
        ensureReservoirConfigured();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GourdvineTapBlockEntity te) {
        te.ensureReservoirConfigured();
        IBloodVolume reservoir = te.resolveVolume();
        if (reservoir == null) return;

        int stage = state.hasProperty(GourdvineTapBlock.STAGE) ? state.getValue(GourdvineTapBlock.STAGE) : 0;
        double genRate = generationRate(stage);

        boolean changed = false;
        if (genRate > 0 && !reservoir.isFull()) {
            double missing = reservoir.getMaxBloodVolume() - reservoir.getBloodVolume();
            double add = Math.min(genRate, missing);
            if (add > 0) {
                reservoir.addBloodVolume(add);
                changed = true;
            }
        }

        ItemStack gourdStack = te.items.get(SLOT_GOURD);
        if (!gourdStack.isEmpty() && genRate > 0) {
            if (BloodContainerTransfer.drainReservoirIntoGourdSlot(te, reservoir, SLOT_GOURD, genRate)) {
                changed = true;
            }
        }

        if (changed) {
            setChanged(level, pos, state);
            te.sendUpdates();
        }
    }

    private static double generationRate(int stage) {
        return switch (stage) {
            case 1 -> 0.20D;
            case 2 -> 0.35D;
            case 3 -> 0.50D;
            default -> 0.10D;
        };
    }

    @Nullable
    private IBloodVolume resolveVolume() {
        return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
    }

    private void ensureReservoirConfigured() {
        if (initialised) return;
        IBloodVolume vol = resolveVolume();
        if (vol != null) {
            vol.setActive(true);
            vol.setMaxBloodVolume(MAX_BLOOD);
            if (vol.getBloodVolume() > MAX_BLOOD) {
                vol.setBloodVolume(MAX_BLOOD);
            }
        }
        initialised = true;
        setChanged();
    }

    public boolean insertGourd(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof BloodGourdItem)) return false;
        if (!items.get(SLOT_GOURD).isEmpty()) return false;

        ItemStack toInsert = stack.copyWithCount(1);
        configureInsertedGourd(toInsert);
        items.set(SLOT_GOURD, toInsert);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        setChanged();
        sendUpdates();
        return true;
    }

    public ItemStack extractGourd() {
        ItemStack held = items.get(SLOT_GOURD);
        if (held.isEmpty()) return ItemStack.EMPTY;

        items.set(SLOT_GOURD, ItemStack.EMPTY);
        setChanged();
        sendUpdates();
        return held;
    }

    private static void configureInsertedGourd(ItemStack stack) {
        if (!(stack.getItem() instanceof BloodGourdItem gourd)) return;
        IBloodVolume vol = HemoCapabilityAccess.getBloodVolume(stack).orElse(null);
        if (vol == null) return;
        double max = gourd.getMaxBlood();
        if (max > 0) {
            vol.setMaxBloodVolume(max);
            if (vol.getBloodVolume() > max) {
                vol.setBloodVolume(max);
            }
        }
    }

    // ── IBloodTile ────────────────────────────────────────────────────────────

    @Override
    public void sendUpdates() {
        if (level == null) return;
        level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        setChanged();
    }

    // ── Container ─────────────────────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.get(SLOT_GOURD).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_GOURD && stack.getItem() instanceof BloodGourdItem;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clearContent() {
        items.set(SLOT_GOURD, ItemStack.EMPTY);
    }

    // ── NBT ───────────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        ContainerHelper.saveAllItems(tag, items, provider);

        IBloodVolume vol = resolveVolume();
        if (vol != null) {
            tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        ContainerHelper.loadAllItems(tag, items, provider);

        if (tag.contains(TAG_BLOOD_LEVEL)) {
            IBloodVolume vol = resolveVolume();
            if (vol != null) {
                vol.setActive(true);
                vol.setMaxBloodVolume(MAX_BLOOD);
                vol.setBloodVolume(Math.min(tag.getDouble(TAG_BLOOD_LEVEL), MAX_BLOOD));
                initialised = true;
            }
        }
    }
}
