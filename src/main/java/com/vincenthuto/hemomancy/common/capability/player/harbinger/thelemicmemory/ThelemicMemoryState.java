package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

public class ThelemicMemoryState implements INBTSerializable<CompoundTag> {
    private final Set<ThelemicMemory> known = EnumSet.noneOf(ThelemicMemory.class);
    private final EnumMap<ThelemicMemory, Integer> reserves = new EnumMap<>(ThelemicMemory.class);
    private final EnumMap<EnumVeinSections, ThelemicMemory> enabled = new EnumMap<>(EnumVeinSections.class);
    private final EnumMap<ThelemicMemory, Long> cooldowns = new EnumMap<>(ThelemicMemory.class);
    private final EnumMap<ThelemicMemory, Long> effectActiveUntil = new EnumMap<>(ThelemicMemory.class);
    private final EnumMap<ThelemicMemory, Long> empoweredUntil = new EnumMap<>(ThelemicMemory.class);
    private ThelemicMemory overexertMemory;
    private long overexertUntil;

    public void learnAndAddReserve(ThelemicMemory memory, int ticks) {
        known.add(memory);
        reserves.put(memory, ThelemicMemoryPrimingRules.addReserve(reserveTicks(memory), ticks));
    }

    public boolean knows(ThelemicMemory memory) { return known.contains(memory); }
    public int knownCount() { return known.size(); }
    public Set<ThelemicMemory> knownMemories() { return Set.copyOf(known); }
    public int reserveTicks(ThelemicMemory memory) { return Math.max(0, reserves.getOrDefault(memory, 0)); }
    public boolean hasReserve(ThelemicMemory memory) { return reserveTicks(memory) > 0; }

    public boolean activate(ThelemicMemory memory) {
        if (!knows(memory) || !hasReserve(memory)) return false;
        enabled.put(memory.section(), memory);
        return true;
    }

    public boolean deactivate(ThelemicMemory memory) {
        return enabled.remove(memory.section(), memory);
    }

    public boolean toggle(ThelemicMemory memory) {
        if (isEnabled(memory)) {
            deactivate(memory);
            clearOverexertion(memory);
            return false;
        }
        return activate(memory);
    }

    public boolean isEnabled(ThelemicMemory memory) {
        return enabled.get(memory.section()) == memory && hasReserve(memory);
    }

    public Optional<ThelemicMemory> enabledMemory(EnumVeinSections section) {
        ThelemicMemory memory = enabled.get(section);
        return memory != null && hasReserve(memory) ? Optional.of(memory) : Optional.empty();
    }

    public boolean tickActiveReserves() {
        boolean changed = false;
        for (EnumVeinSections section : EnumVeinSections.values()) {
            ThelemicMemory memory = enabled.get(section);
            if (memory == null) continue;
            int remaining = Math.max(0, reserveTicks(memory) - 1);
            reserves.put(memory, remaining);
            changed = true;
            if (remaining == 0) {
                enabled.remove(section);
                clearOverexertion(memory);
            }
        }
        return changed;
    }

    public boolean hasEnabledMemories() { return !enabled.isEmpty(); }

    public void armOverexertion(ThelemicMemory memory, long gameTime) {
        if (!isEnabled(memory)) return;
        overexertMemory = memory;
        overexertUntil = gameTime + ThelemicMemoryPrimingRules.OVEREXERT_WINDOW_TICKS;
    }

    public boolean isOverexertionArmed(ThelemicMemory memory, long gameTime) {
        if (overexertMemory == null || overexertUntil <= gameTime) {
            overexertMemory = null;
            overexertUntil = 0L;
            return false;
        }
        return overexertMemory == memory;
    }

    public boolean consumeOverexertion(ThelemicMemory memory, long gameTime) {
        if (!isOverexertionArmed(memory, gameTime)) return false;
        overexertMemory = null;
        overexertUntil = 0L;
        return true;
    }

    public void clearOverexertion(ThelemicMemory memory) {
        if (overexertMemory == memory) {
            overexertMemory = null;
            overexertUntil = 0L;
        }
    }

    public Optional<ThelemicMemory> getPrimed(EnumVeinSections section, long gameTime) {
        Optional<ThelemicMemory> active = enabledMemory(section);
        if (active.isPresent()) return active;
        return known.stream().filter(memory -> memory.section() == section && hasReserve(memory)).findFirst();
    }

    public long remainingTicks(EnumVeinSections section, long gameTime) {
        return getPrimed(section, gameTime).map(this::reserveTicks).orElse(0);
    }


    public void clearPreparedState() {
        reserves.clear();
        enabled.clear();
        cooldowns.clear();
        effectActiveUntil.clear();
        empoweredUntil.clear();
        overexertMemory = null;
        overexertUntil = 0L;
    }

    public void clearPrimed() { clearPreparedState(); }
    public boolean isCooldownReady(ThelemicMemory memory, long gameTime) { return cooldowns.getOrDefault(memory, 0L) <= gameTime; }
    public void setCooldownUntil(ThelemicMemory memory, long gameTime) { cooldowns.put(memory, gameTime); }
    public boolean isActive(ThelemicMemory memory, long gameTime) { return effectActiveUntil.getOrDefault(memory, 0L) > gameTime; }
    public void setActiveUntil(ThelemicMemory memory, long gameTime) { effectActiveUntil.put(memory, gameTime); }
    public boolean isEmpowered(ThelemicMemory memory, long gameTime) { return empoweredUntil.getOrDefault(memory, 0L) > gameTime; }
    public void setEmpoweredUntil(ThelemicMemory memory, long gameTime) { empoweredUntil.put(memory, gameTime); }

    public ThelemicMemoryState copyForDeath() {
        ThelemicMemoryState copy = new ThelemicMemoryState();
        copy.known.addAll(known);
        return copy;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag root = new CompoundTag();
        ListTag knownList = new ListTag();
        known.forEach(memory -> knownList.add(StringTag.valueOf(memory.id())));
        root.put("Known", knownList);
        root.put("Reserves", writeInts(reserves));
        CompoundTag enabledTag = new CompoundTag();
        enabled.forEach((section, memory) -> enabledTag.putString(section.name(), memory.id()));
        root.put("Enabled", enabledTag);
        root.put("Cooldowns", writeTimes(cooldowns));
        root.put("Active", writeTimes(effectActiveUntil));
        root.put("Empowered", writeTimes(empoweredUntil));
        if (overexertMemory != null) {
            root.putString("OverexertMemory", overexertMemory.id());
            root.putLong("OverexertUntil", overexertUntil);
        }
        return root;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag root) {
        known.clear(); reserves.clear(); enabled.clear(); cooldowns.clear(); effectActiveUntil.clear(); empoweredUntil.clear();
        overexertMemory = null; overexertUntil = 0L;
        ListTag knownList = root.getList("Known", Tag.TAG_STRING);
        for (int index = 0; index < knownList.size(); index++) {
            ThelemicMemory.byId(knownList.getString(index)).ifPresent(known::add);
        }
        readInts(root.getCompound("Reserves"), reserves);
        CompoundTag enabledTag = root.getCompound("Enabled");
        for (EnumVeinSections section : EnumVeinSections.values()) {
            ThelemicMemory.byId(enabledTag.getString(section.name()))
                    .filter(memory -> memory.section() == section && hasReserve(memory))
                    .ifPresent(memory -> enabled.put(section, memory));
        }
        readTimes(root.getCompound("Cooldowns"), cooldowns);
        readTimes(root.getCompound("Active"), effectActiveUntil);
        readTimes(root.getCompound("Empowered"), empoweredUntil);
        ThelemicMemory.byId(root.getString("OverexertMemory")).ifPresent(memory -> {
            overexertMemory = memory;
            overexertUntil = root.getLong("OverexertUntil");
        });
    }


    private static CompoundTag writeInts(EnumMap<ThelemicMemory, Integer> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((memory, value) -> tag.putInt(memory.id(), value));
        return tag;
    }

    private static void readInts(CompoundTag tag, EnumMap<ThelemicMemory, Integer> target) {
        for (ThelemicMemory memory : ThelemicMemory.values()) {
            if (tag.contains(memory.id(), Tag.TAG_INT)) target.put(memory,
                    Math.max(0, Math.min(ThelemicMemoryPrimingRules.MAX_RESERVE_TICKS, tag.getInt(memory.id()))));
        }
    }

    private static CompoundTag writeTimes(EnumMap<ThelemicMemory, Long> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((memory, time) -> tag.putLong(memory.id(), time));
        return tag;
    }

    private static void readTimes(CompoundTag tag, EnumMap<ThelemicMemory, Long> target) {
        for (ThelemicMemory memory : ThelemicMemory.values()) {
            if (tag.contains(memory.id(), Tag.TAG_LONG)) target.put(memory, tag.getLong(memory.id()));
        }
    }
}
