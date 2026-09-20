package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.common.item.component.SpecimenStack;
import java.util.List;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import java.util.Arrays;
import java.util.function.Consumer;

/** Counts belong to the cabinet, never to the unstackable exemplar. */
public final class CabinetStorage implements IItemHandler {
    public static final int SIZE = 9;
    public static final int CAPACITY = 64;
    private final SpecimenStack[] entries = new SpecimenStack[SIZE];
    private final int capacity;
    private final boolean uniqueIdentities;
    private final Consumer<Boolean> changed;

    public CabinetStorage(Consumer<Boolean> changed) {
        this(CAPACITY, false, changed);
    }
    public CabinetStorage(int capacity, boolean uniqueIdentities, Consumer<Boolean> changed) {
        this.capacity = capacity;
        this.uniqueIdentities = uniqueIdentities;
        this.changed = changed;
        Arrays.fill(entries, SpecimenStack.EMPTY);
    }
    private void check(int slot) {
        if (slot < 0 || slot >= SIZE) throw new IndexOutOfBoundsException(slot);
    }
    @Override public int getSlots() { return SIZE; }
    @Override public int getSlotLimit(int slot) { check(slot); return capacity; }
    @Override public ItemStack getStackInSlot(int slot) { check(slot); return entries[slot].exemplar(); }
    public int count(int slot) { check(slot); return entries[slot].count(); }
    public int totalCount() { return Arrays.stream(entries).mapToInt(SpecimenStack::count).sum(); }
    public int comparatorSignal() { int total = totalCount(); return total == 0 ? 0 : 1 + 14 * total / (SIZE * capacity); }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        check(slot);
        if (!BloodSampleData.isStorableSample(stack)) return false;
        if (uniqueIdentities) for (int i = 0; i < SIZE; i++)
            if (i != slot && count(i) > 0 && entries[i].matches(stack)) return false;
        return count(slot) == 0 || entries[slot].matches(stack);
    }
    public int findInsertionSlot(ItemStack stack) {
        if (!BloodSampleData.isStorableSample(stack)) return -1;
        for (int i = 0; i < SIZE; i++)
            if (count(i) > 0 && count(i) < capacity && isItemValid(i, stack)) return i;
        for (int i = 0; i < SIZE; i++) if (count(i) == 0 && isItemValid(i, stack)) return i;
        return -1;
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        int accepted = Math.min(stack.getCount(), capacity - count(slot));
        if (accepted == 0) return stack;
        if (!simulate) {
            boolean displayChanged = count(slot) == 0;
            entries[slot] = new SpecimenStack(stack, count(slot) + accepted);
            changed.accept(displayChanged);
        }
        return stack.copyWithCount(stack.getCount() - accepted);
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        check(slot);
        if (amount <= 0 || count(slot) == 0) return ItemStack.EMPTY;
        ItemStack result = entries[slot].exemplar();
        if (!simulate) {
            entries[slot] = count(slot) == 1 ? SpecimenStack.EMPTY : new SpecimenStack(result, count(slot) - 1);
            changed.accept(count(slot) == 0);
        }
        return result;
    }
    public int capacity() { return capacity; }
    public List<SpecimenStack> snapshot() { return List.copyOf(Arrays.asList(entries)); }
    public void restore(List<SpecimenStack> snapshot) {
        if (snapshot.size() != SIZE) throw new IllegalArgumentException("Nine specimen cells required");
        for (int i = 0; i < SIZE; i++) entries[i] = snapshot.get(i);
    }
    public CompoundTag save(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putInt("Version", 1);
        var savedEntries = new ListTag();
        for (int i = 0; i < SIZE; i++) {
            var entry = new CompoundTag();
            entry.putInt("Count", count(i));
            if (count(i) > 0) entry.put("Specimen", entries[i].exemplar().save(provider));
            savedEntries.add(entry);
        }
        tag.put("Entries", savedEntries);
        return tag;
    }
    public void load(CompoundTag tag, HolderLookup.Provider provider) {
        Arrays.fill(this.entries, SpecimenStack.EMPTY);
        int version = tag.getInt("Version");
        if (version != 1) LogUtils.getLogger().warn("Cabinet: reading unrecognized storage version {}", version);
        var entries = tag.getList("Entries", Tag.TAG_COMPOUND);
        if (entries.size() > SIZE) LogUtils.getLogger().warn("Cabinet: ignoring entries beyond the nine pigeonholes");
        for (int i = 0; i < Math.min(SIZE, entries.size()); i++) {
            var entry = entries.getCompound(i);
            int count = entry.getInt("Count");
            if (count <= 0) continue;
            var parsed = ItemStack.parse(provider, entry.getCompound("Specimen"));
            if (parsed.isEmpty()) {
                LogUtils.getLogger().warn("Cabinet: could not decode specimen in pigeonhole {}", i);
                continue;
            }
            var stack = parsed.get();
            if (stack.getItem() instanceof BloodVialItem && com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.is(stack))
                stack = com.vincenthuto.hemomancy.common.antecedent.AhaematicSample.migrate(stack);
            if (!BloodSampleData.isSpecimenVessel(stack) || !BloodSampleData.isFilled(stack)) {
                LogUtils.getLogger().warn("Cabinet: rejected non-specimen in pigeonhole {}", i);
                continue;
            }
            if (count > capacity) LogUtils.getLogger().warn("Cabinet: clamped pigeonhole {} count {} to {}", i, count, capacity);
            this.entries[i] = new SpecimenStack(stack, Math.min(count, capacity));
        }
    }
}
