package com.vincenthuto.hemomancy.common.damage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Server-authoritative meters. Effects supply visibility and ordinary cure/expiry hooks. */
public final class SchoolCombatState implements INBTSerializable<CompoundTag> {
    public static final class Entry {
        public SchoolHitContext origin;
        public int levels;
        public float stored;
        public long expires;
        public long lastBuild;
        public long nextTick;

        public Entry(SchoolHitContext origin, long now, int duration) {
            this.origin = origin;
            levels = 1;
            expires = now + duration;
            lastBuild = now - 10;
            nextTick = now + 20;
        }
    }

    private final EnumMap<SchoolState, Entry> entries = new EnumMap<>(SchoolState.class);
    private final LinkedHashMap<UUID, Long> applications = new LinkedHashMap<>();
    private final LinkedHashMap<UUID, Long> payoffs = new LinkedHashMap<>();
    private boolean dirty;
    private long observedAt;
    private boolean loaded;

    public void observe(long tick) { observedAt = tick; }

    public void restore(net.minecraft.world.entity.LivingEntity target) {
        if (!loaded) return;
        long now = target.level().getGameTime();
        long paused = Math.max(0, now - observedAt);
        entries.entrySet().removeIf(state -> !target.hasEffect(state.getKey().effect()));
        entries.forEach((state, entry) -> {
            entry.expires = now + target.getEffect(state.effect()).getDuration();
            entry.lastBuild += paused;
            entry.nextTick += paused;
        });
        observedAt = now;
        loaded = false;
        dirty = true;
    }

    public Entry get(SchoolState state) { return entries.get(state); }
    public Map<SchoolState, Entry> entries() { return entries; }
    public void put(SchoolState state, Entry value) { entries.put(state, value); dirty = true; }
    public Entry remove(SchoolState state) {
        Entry value = entries.remove(state);
        if (value != null) dirty = true;
        return value;
    }
    public void changed() { dirty = true; }
    public boolean takeDirty() { boolean result = dirty; dirty = false; return result; }

    private static UUID applicationKey(UUID root, SchoolState state) {
        return UUID.nameUUIDFromBytes((root + ":" + state.name()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    public boolean hasApplied(UUID root, SchoolState state) { return applications.containsKey(applicationKey(root, state)); }
    public void recordApplication(UUID root, SchoolState state, long now) { record(applications, applicationKey(root, state), now); }

    public boolean hasApplied(UUID root) { return applications.containsKey(root); }
    public boolean hasPaid(UUID root) { return payoffs.containsKey(root); }
    public void recordApplication(UUID root, long now) { record(applications, root, now); }
    public void recordPayoff(UUID root, long now) { record(payoffs, root, now); }

    private void record(LinkedHashMap<UUID, Long> ledger, UUID root, long now) {
        ledger.entrySet().removeIf(entry -> entry.getValue() + 2400 < now);
        ledger.put(root, now);
        while (ledger.size() > 512) ledger.remove(ledger.keySet().iterator().next());
        dirty = true;
    }

    @Override public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("observedAt", observedAt);
        for (var state : entries.entrySet()) {
            Entry value = state.getValue();
            CompoundTag data = value.origin.save();
            data.putInt("stacks", value.levels);
            data.putFloat("stored", value.stored);
            data.putLong("expires", value.expires);
            data.putLong("lastBuild", value.lastBuild);
            data.putLong("nextTick", value.nextTick);
            tag.put(state.getKey().name(), data);
        }
        tag.put("applications", saveLedger(applications));
        tag.put("payoffs", saveLedger(payoffs));
        return tag;
    }

    private static ListTag saveLedger(Map<UUID, Long> ledger) {
        ListTag list = new ListTag();
        ledger.forEach((root, tick) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("root", root);
            entry.putLong("tick", tick);
            list.add(entry);
        });
        return list;
    }

    @Override public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        entries.clear();
        observedAt = tag.getLong("observedAt");
        loaded = true;
        for (SchoolState state : SchoolState.values()) {
            if (!tag.contains(state.name())) continue;
            CompoundTag data = tag.getCompound(state.name());
            SchoolHitContext origin = SchoolHitContext.load(data);
            if (origin == null) continue;
            Entry value = new Entry(origin, 0, 0);
            value.levels = Math.clamp(data.getInt("stacks"), 1, 3);
            value.stored = Math.clamp(data.getFloat("stored"), 0, 6);
            value.expires = data.getLong("expires");
            value.lastBuild = data.getLong("lastBuild");
            value.nextTick = data.getLong("nextTick");
            entries.put(state, value);
        }
        loadLedger(tag.getList("applications", Tag.TAG_COMPOUND), applications);
        loadLedger(tag.getList("payoffs", Tag.TAG_COMPOUND), payoffs);
        dirty = true;
    }

    private static void loadLedger(ListTag list, Map<UUID, Long> ledger) {
        ledger.clear();
        for (int i = Math.max(0, list.size() - 512); i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID("root")) ledger.put(entry.getUUID("root"), entry.getLong("tick"));
        }
    }
}
