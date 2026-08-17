package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

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

public class MuscleMemoryState implements INBTSerializable<CompoundTag> {
    private final Set<MuscleMemory> known = EnumSet.noneOf(MuscleMemory.class);
    private final EnumMap<MuscleMemory, Integer> reserves = new EnumMap<>(MuscleMemory.class);
    private final EnumMap<EnumVeinSections, MuscleMemory> enabled = new EnumMap<>(EnumVeinSections.class);
    private final EnumMap<MuscleMemory, Long> cooldowns = new EnumMap<>(MuscleMemory.class);
    private final EnumMap<MuscleMemory, Long> effectActiveUntil = new EnumMap<>(MuscleMemory.class);
    private final EnumMap<MuscleMemory, Long> empoweredUntil = new EnumMap<>(MuscleMemory.class);
    private final EnumMap<MuscleMemory, Long> legacyPrimedUntil = new EnumMap<>(MuscleMemory.class);
    private MuscleMemory overexertMemory;
    private long overexertUntil;

    public void learnAndAddReserve(MuscleMemory memory, int ticks) {
        known.add(memory);
        reserves.put(memory, MuscleMemoryPrimingRules.addReserve(reserveTicks(memory), ticks));
    }

    public void learnAndPrime(MuscleMemory memory, long gameTime, int durationTicks) {
        learnAndAddReserve(memory, durationTicks);
    }

    public boolean knows(MuscleMemory memory) { return known.contains(memory); }
    public int knownCount() { return known.size(); }
    public Set<MuscleMemory> knownMemories() { return Set.copyOf(known); }
    public int reserveTicks(MuscleMemory memory) { return Math.max(0, reserves.getOrDefault(memory, 0)); }
    public boolean hasReserve(MuscleMemory memory) { return reserveTicks(memory) > 0; }

    public boolean activate(MuscleMemory memory) {
        if (!knows(memory) || !hasReserve(memory)) return false;
        enabled.put(memory.section(), memory);
        return true;
    }

    public boolean deactivate(MuscleMemory memory) {
        return enabled.remove(memory.section(), memory);
    }

    public boolean toggle(MuscleMemory memory) {
        if (isEnabled(memory)) {
            deactivate(memory);
            clearOverexertion(memory);
            return false;
        }
        return activate(memory);
    }

    public boolean isEnabled(MuscleMemory memory) {
        return enabled.get(memory.section()) == memory && hasReserve(memory);
    }

    public Optional<MuscleMemory> enabledMemory(EnumVeinSections section) {
        MuscleMemory memory = enabled.get(section);
        return memory != null && hasReserve(memory) ? Optional.of(memory) : Optional.empty();
    }

    public boolean tickActiveReserves() {
        boolean changed = false;
        for (EnumVeinSections section : EnumVeinSections.values()) {
            MuscleMemory memory = enabled.get(section);
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

    public void armOverexertion(MuscleMemory memory, long gameTime) {
        if (!isEnabled(memory)) return;
        overexertMemory = memory;
        overexertUntil = gameTime + MuscleMemoryPrimingRules.OVEREXERT_WINDOW_TICKS;
    }

    public boolean isOverexertionArmed(MuscleMemory memory, long gameTime) {
        if (overexertMemory == null || overexertUntil <= gameTime) {
            overexertMemory = null;
            overexertUntil = 0L;
            return false;
        }
        return overexertMemory == memory;
    }

    public boolean consumeOverexertion(MuscleMemory memory, long gameTime) {
        if (!isOverexertionArmed(memory, gameTime)) return false;
        overexertMemory = null;
        overexertUntil = 0L;
        return true;
    }

    public void clearOverexertion(MuscleMemory memory) {
        if (overexertMemory == memory) {
            overexertMemory = null;
            overexertUntil = 0L;
        }
    }

    public Optional<MuscleMemory> getPrimed(EnumVeinSections section, long gameTime) {
        migrateLegacyPriming(gameTime);
        Optional<MuscleMemory> active = enabledMemory(section);
        if (active.isPresent()) return active;
        return known.stream().filter(memory -> memory.section() == section && hasReserve(memory)).findFirst();
    }

    public boolean isPrimed(MuscleMemory memory, long gameTime) {
        migrateLegacyPriming(gameTime);
        return hasReserve(memory);
    }

    public long remainingTicks(EnumVeinSections section, long gameTime) {
        return getPrimed(section, gameTime).map(this::reserveTicks).orElse(0);
    }

    public void migrateLegacyPriming(long gameTime) {
        if (legacyPrimedUntil.isEmpty()) return;
        legacyPrimedUntil.forEach((memory, expiresAt) -> learnAndAddReserve(memory,
                (int) Math.max(0L, Math.min(Integer.MAX_VALUE, expiresAt - gameTime))));
        legacyPrimedUntil.clear();
    }

    public void clearPreparedState() {
        reserves.clear();
        enabled.clear();
        cooldowns.clear();
        effectActiveUntil.clear();
        empoweredUntil.clear();
        legacyPrimedUntil.clear();
        overexertMemory = null;
        overexertUntil = 0L;
    }

    public void clearPrimed() { clearPreparedState(); }
    public boolean isCooldownReady(MuscleMemory memory, long gameTime) { return cooldowns.getOrDefault(memory, 0L) <= gameTime; }
    public void setCooldownUntil(MuscleMemory memory, long gameTime) { cooldowns.put(memory, gameTime); }
    public boolean isActive(MuscleMemory memory, long gameTime) { return effectActiveUntil.getOrDefault(memory, 0L) > gameTime; }
    public void setActiveUntil(MuscleMemory memory, long gameTime) { effectActiveUntil.put(memory, gameTime); }
    public boolean isEmpowered(MuscleMemory memory, long gameTime) { return empoweredUntil.getOrDefault(memory, 0L) > gameTime; }
    public void setEmpoweredUntil(MuscleMemory memory, long gameTime) { empoweredUntil.put(memory, gameTime); }

    public MuscleMemoryState copyForDeath() {
        MuscleMemoryState copy = new MuscleMemoryState();
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
        legacyPrimedUntil.clear(); overexertMemory = null; overexertUntil = 0L;
        ListTag knownList = root.getList("Known", Tag.TAG_STRING);
        for (int index = 0; index < knownList.size(); index++) {
            MuscleMemory.byId(knownList.getString(index)).ifPresent(known::add);
        }
        readInts(root.getCompound("Reserves"), reserves);
        CompoundTag enabledTag = root.getCompound("Enabled");
        for (EnumVeinSections section : EnumVeinSections.values()) {
            MuscleMemory.byId(enabledTag.getString(section.name()))
                    .filter(memory -> memory.section() == section && hasReserve(memory))
                    .ifPresent(memory -> enabled.put(section, memory));
        }
        readTimes(root.getCompound("Cooldowns"), cooldowns);
        readTimes(root.getCompound("Active"), effectActiveUntil);
        readTimes(root.getCompound("Empowered"), empoweredUntil);
        MuscleMemory.byId(root.getString("OverexertMemory")).ifPresent(memory -> {
            overexertMemory = memory;
            overexertUntil = root.getLong("OverexertUntil");
        });
        readLegacyPriming(root.getCompound("Primed"));
    }

    private void readLegacyPriming(CompoundTag primedTag) {
        for (EnumVeinSections section : EnumVeinSections.values()) {
            if (!primedTag.contains(section.name(), Tag.TAG_COMPOUND)) continue;
            CompoundTag entry = primedTag.getCompound(section.name());
            MuscleMemory.byId(entry.getString("Memory"))
                    .filter(memory -> memory.section() == section)
                    .ifPresent(memory -> legacyPrimedUntil.put(memory, entry.getLong("ExpiresAt")));
        }
    }

    private static CompoundTag writeInts(EnumMap<MuscleMemory, Integer> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((memory, value) -> tag.putInt(memory.id(), value));
        return tag;
    }

    private static void readInts(CompoundTag tag, EnumMap<MuscleMemory, Integer> target) {
        for (MuscleMemory memory : MuscleMemory.values()) {
            if (tag.contains(memory.id(), Tag.TAG_INT)) target.put(memory,
                    Math.max(0, Math.min(MuscleMemoryPrimingRules.MAX_RESERVE_TICKS, tag.getInt(memory.id()))));
        }
    }

    private static CompoundTag writeTimes(EnumMap<MuscleMemory, Long> values) {
        CompoundTag tag = new CompoundTag();
        values.forEach((memory, time) -> tag.putLong(memory.id(), time));
        return tag;
    }

    private static void readTimes(CompoundTag tag, EnumMap<MuscleMemory, Long> target) {
        for (MuscleMemory memory : MuscleMemory.values()) {
            if (tag.contains(memory.id(), Tag.TAG_LONG)) target.put(memory, tag.getLong(memory.id()));
        }
    }
}
