package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-level persistence for active Pale Consecration zones established by
 * the Rite of Pale Consecration. Each entry tracks the caster, zone centre,
 * dimension, radius, and expiry tick.
 * <p>
 * While active, hostile mobs inside the zone periodically take 1 damage and
 * receive Slowness I, simulating the consecrated ground's rejection of
 * blood-touched creatures.
 */
public class PaleConsecrationSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_pale_consecration";

	private final List<ConsecrationEntry> entries = new ArrayList<>();

	public PaleConsecrationSavedData() {}

	public static PaleConsecrationSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(
				PaleConsecrationSavedData::load, PaleConsecrationSavedData::new, DATA_NAME);
	}

	public static PaleConsecrationSavedData load(CompoundTag tag) {
		PaleConsecrationSavedData data = new PaleConsecrationSavedData();
		if (tag.contains("entries", Tag.TAG_LIST)) {
			ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int blockRadius = entry.getInt("BlockRadius");
				long expiryTick = entry.getLong("ExpiryTick");
				data.entries.add(new ConsecrationEntry(ownerUUID, center, dimension, blockRadius, expiryTick));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag) {
		ListTag list = new ListTag();
		for (ConsecrationEntry entry : entries) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putUUID("Owner", entry.ownerUUID());
			entryTag.putLong("Center", entry.center().asLong());
			entryTag.putString("Dimension", entry.dimension());
			entryTag.putInt("BlockRadius", entry.blockRadius());
			entryTag.putLong("ExpiryTick", entry.expiryTick());
			list.add(entryTag);
		}
		tag.put("entries", list);
		return tag;
	}

	public void addEntry(ConsecrationEntry entry) {
		entries.add(entry);
		setDirty();
	}

	public List<ConsecrationEntry> getEntries() {
		return entries;
	}

	/** Removes all expired entries. Returns true if anything was removed. */
	public boolean removeExpired(long currentTick) {
		boolean changed = entries.removeIf(e -> currentTick >= e.expiryTick());
		if (changed) setDirty();
		return changed;
	}

	/** Returns true if the given position is inside at least one active zone. */
	public boolean isInZone(BlockPos pos, String dimension, long currentTick) {
		for (ConsecrationEntry entry : entries) {
			if (currentTick >= entry.expiryTick()) continue;
			if (!entry.dimension().equals(dimension)) continue;
			BlockPos center = entry.center();
			double dx = Math.abs(pos.getX() - center.getX());
			double dz = Math.abs(pos.getZ() - center.getZ());
			if (dx <= entry.blockRadius() && dz <= entry.blockRadius()) return true;
		}
		return false;
	}

	/**
	 * A single Pale Consecration zone entry.
	 *
	 * @param ownerUUID   UUID of the player who cast the rite
	 * @param center      centre block position
	 * @param dimension   dimension registry-key string
	 * @param blockRadius zone radius in blocks
	 * @param expiryTick  world game-tick at which this entry expires
	 */
	public record ConsecrationEntry(UUID ownerUUID, BlockPos center, String dimension,
			int blockRadius, long expiryTick) {}
}
