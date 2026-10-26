package com.vincenthuto.hemomancy.common.encounter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** One encounter record per generated Vagrant Mind, including abandoned attempts' terrain recovery. */
public final class VagrantMindEncounterData extends SavedData {
    private static final Factory<VagrantMindEncounterData> FACTORY =
            new Factory<>(VagrantMindEncounterData::new, VagrantMindEncounterData::load, null);
    private final Map<Long, Encounter> encounters = new HashMap<>();

    public static VagrantMindEncounterData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "hemomancy_vagrant_mind_encounters");
    }

    public Encounter encounter(BlockPos origin) { return encounters.get(origin.asLong()); }
    public Map<Long, Encounter> encounters() { return Collections.unmodifiableMap(encounters); }

    public void site(BlockPos origin, BlockPos knot) {
        Encounter entry = encounters.computeIfAbsent(origin.asLong(), key -> new Encounter(null, knot, false));
        if (entry.defeated || entry.knot != null) return;
        entry.knot = knot.immutable();
        setDirty();
    }

    public void active(BlockPos origin, UUID entity, BlockPos position) {
        Encounter entry = encounters.computeIfAbsent(origin.asLong(), key -> new Encounter(null, position, false));
        if (entry.defeated || entry.entity != null && !entry.entity.equals(entity)) return;
        entry.entity = entity;
        entry.position = position.immutable();
        entry.emptySince = -1;
        setDirty();
    }

    public void position(BlockPos origin, UUID entity, BlockPos position) {
        Encounter entry = encounter(origin);
        if (entry == null || entry.defeated || !entity.equals(entry.entity) || entry.position.equals(position)) return;
        entry.position = position.immutable();
        setDirty();
    }

    /** Check expiry before accepting a returning occupant, including while the boss's chunk is unloaded. */
    public boolean abandoned(BlockPos origin, boolean occupied, long now) {
        Encounter entry = encounter(origin);
        if (entry == null || entry.defeated || entry.entity == null) return false;
        if (entry.emptySince >= 0 && now - entry.emptySince >= 600) return true;
        long since = occupied ? -1 : entry.emptySince < 0 ? now : entry.emptySince;
        if (since != entry.emptySince) {
            entry.emptySince = since;
            setDirty();
        }
        return false;
    }

    public void abandon(BlockPos origin) {
        Encounter entry = encounter(origin);
        if (entry == null || entry.defeated) return;
        entry.entity = null;
        entry.emptySince = -1;
        entry.pendingNodes.addAll(entry.destroyedNodes);
        entry.destroyedNodes.clear();
        setDirty();
    }

    public void destroyedNode(BlockPos origin, UUID entity, BlockPos node) {
        Encounter entry = encounter(origin);
        if (entry != null && !entry.defeated && entity.equals(entry.entity) && entry.destroyedNodes.add(node.immutable()))
            setDirty();
    }

    public void recoveredNode(BlockPos origin, BlockPos node) {
        Encounter entry = encounter(origin);
        if (entry != null && entry.pendingNodes.remove(node)) setDirty();
    }

    public void defeated(BlockPos origin, UUID entity) {
        Encounter entry = encounter(origin);
        if (entry == null || entry.defeated || !entity.equals(entry.entity)) return;
        entry.entity = null;
        entry.defeated = true;
        entry.emptySince = -1;
        entry.destroyedNodes.clear();
        setDirty();
    }

    public static final class Encounter {
        private UUID entity;
        private BlockPos position;
        private boolean defeated;
        private BlockPos knot;
        private long emptySince = -1;
        private final Set<BlockPos> destroyedNodes = new LinkedHashSet<>();
        private final Set<BlockPos> pendingNodes = new LinkedHashSet<>();

        private Encounter(UUID entity, BlockPos position, boolean defeated) {
            this.entity = entity;
            this.position = position.immutable();
            this.defeated = defeated;
        }

        public UUID entity() { return entity; }
        public BlockPos position() { return position; }
        public boolean defeated() { return defeated; }
        public BlockPos knot() { return knot; }
        public Set<BlockPos> destroyedNodes() { return Set.copyOf(destroyedNodes); }
        public Set<BlockPos> pendingNodes() { return Set.copyOf(pendingNodes); }
    }

    public static VagrantMindEncounterData load(CompoundTag tag, HolderLookup.Provider lookup) {
        VagrantMindEncounterData data = new VagrantMindEncounterData();
        ListTag list = tag.getList("Encounters", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            Encounter encounter = new Encounter(entry.hasUUID("Entity") ? entry.getUUID("Entity") : null,
                    BlockPos.of(entry.getLong("Position")), entry.getBoolean("Defeated"));
            if (entry.contains("Knot")) encounter.knot = BlockPos.of(entry.getLong("Knot"));
            encounter.emptySince = entry.contains("EmptySince") ? entry.getLong("EmptySince") : -1;
            for (long node : entry.getLongArray("DestroyedNodes")) encounter.destroyedNodes.add(BlockPos.of(node));
            for (long node : entry.getLongArray("PendingNodes")) encounter.pendingNodes.add(BlockPos.of(node));
            data.encounters.put(entry.getLong("Origin"), encounter);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        encounters.forEach((origin, encounter) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Origin", origin);
            entry.putLong("Position", encounter.position.asLong());
            entry.putBoolean("Defeated", encounter.defeated);
            if (encounter.entity != null) entry.putUUID("Entity", encounter.entity);
            if (encounter.knot != null) entry.putLong("Knot", encounter.knot.asLong());
            if (encounter.emptySince >= 0) entry.putLong("EmptySince", encounter.emptySince);
            if (!encounter.destroyedNodes.isEmpty())
                entry.putLongArray("DestroyedNodes", encounter.destroyedNodes.stream().mapToLong(BlockPos::asLong).toArray());
            if (!encounter.pendingNodes.isEmpty())
                entry.putLongArray("PendingNodes", encounter.pendingNodes.stream().mapToLong(BlockPos::asLong).toArray());
            list.add(entry);
        });
        tag.put("Encounters", list);
        return tag;
    }
}
