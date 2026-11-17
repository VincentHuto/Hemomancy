package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CR-7: the blood counted into a level's ichor pools toward a summons, and which Phlegethosoma each player last
 * summoned. A player owns one at a time: an older one that is still out there when a new one rises dives as soon as it
 * ticks. Saved with the level as {@code hemomancy_phlegethosoma_summons}.
 */
public final class PhlegethosomaSummons extends SavedData {
    private static final String DATA_NAME = "hemomancy_phlegethosoma_summons";
    private static final SavedData.Factory<PhlegethosomaSummons> FACTORY =
            new SavedData.Factory<>(PhlegethosomaSummons::new, PhlegethosomaSummons::load, null);

    private static final class Pour {
        private final Vec3 point;
        private double ml;
        private long settledAt;

        private Pour(Vec3 point, double ml, long settledAt) {
            this.point = point;
            this.ml = ml;
            this.settledAt = settledAt;
        }

        private boolean holds(Vec3 at) {
            double dx = at.x - point.x, dz = at.z - point.z;
            double reach = PhlegethosomaSummonRules.POOL_REACH;
            return dx * dx + dz * dz <= reach * reach && Math.abs(at.y - point.y) <= 2.0D;
        }
    }

    private final List<Pour> pours = new ArrayList<>();
    private final Map<UUID, UUID> summoned = new HashMap<>();

    public static PhlegethosomaSummons get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    @Nullable
    private Pour at(ServerLevel level, Vec3 point) {
        long now = level.getGameTime();
        Pour found = null;
        for (Iterator<Pour> it = pours.iterator(); it.hasNext(); ) {
            Pour pour = it.next();
            if (PhlegethosomaSummonRules.fade(pour.ml, now - pour.settledAt) <= 0.0D) {
                it.remove();
                setDirty();
            } else if (found == null && pour.holds(point)) {
                found = pour;
            }
        }
        return found;
    }

    /** Adds one tick's pour at {@code point} to the count of the pool there and returns the count. */
    public double pour(ServerLevel level, Vec3 point, double ml) {
        Pour pour = at(level, point);
        long now = level.getGameTime();
        if (pour == null) {
            pour = new Pour(point, 0.0D, now);
            pours.add(pour);
        }
        pour.ml = PhlegethosomaSummonRules.pour(pour.ml, now - pour.settledAt, ml);
        pour.settledAt = now;
        setDirty();
        return pour.ml;
    }

    /** The blood counted into the pool at {@code point} now, 0 when none. */
    public double counted(ServerLevel level, Vec3 point) {
        Pour pour = at(level, point);
        return pour == null ? 0.0D : PhlegethosomaSummonRules.fade(pour.ml, level.getGameTime() - pour.settledAt);
    }

    /** Moves the count at {@code point} back {@code ticks}, as if that long had passed (tests). */
    public void backdate(ServerLevel level, Vec3 point, long ticks) {
        Pour pour = at(level, point);
        if (pour != null) pour.settledAt -= Math.max(0L, ticks);
        setDirty();
    }

    public void clear(ServerLevel level, Vec3 point) {
        Pour pour = at(level, point);
        if (pour != null) pours.remove(pour);
        setDirty();
    }

    @Nullable
    public UUID summonedBy(UUID player) {
        return summoned.get(player);
    }

    public void record(UUID player, UUID boss) {
        summoned.put(player, boss);
        setDirty();
    }

    public boolean isCurrent(UUID player, UUID boss) {
        return boss.equals(summoned.get(player));
    }

    public void release(UUID player, UUID boss) {
        if (summoned.remove(player, boss)) setDirty();
    }

    public static PhlegethosomaSummons load(CompoundTag tag, HolderLookup.Provider provider) {
        PhlegethosomaSummons data = new PhlegethosomaSummons();
        ListTag list = tag.getList("pours", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag pour = list.getCompound(i);
            data.pours.add(new Pour(new Vec3(pour.getDouble("x"), pour.getDouble("y"), pour.getDouble("z")),
                    pour.getDouble("ml"), pour.getLong("settled_at")));
        }
        ListTag owners = tag.getList("summoned", Tag.TAG_COMPOUND);
        for (int i = 0; i < owners.size(); i++) {
            CompoundTag entry = owners.getCompound(i);
            if (entry.hasUUID("player") && entry.hasUUID("boss")) data.summoned.put(entry.getUUID("player"), entry.getUUID("boss"));
        }
        return data;
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (Pour pour : pours) {
            CompoundTag entry = new CompoundTag();
            entry.putDouble("x", pour.point.x);
            entry.putDouble("y", pour.point.y);
            entry.putDouble("z", pour.point.z);
            entry.putDouble("ml", pour.ml);
            entry.putLong("settled_at", pour.settledAt);
            list.add(entry);
        }
        tag.put("pours", list);
        ListTag owners = new ListTag();
        summoned.forEach((player, boss) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            entry.putUUID("boss", boss);
            owners.add(entry);
        });
        tag.put("summoned", owners);
        return tag;
    }
}
