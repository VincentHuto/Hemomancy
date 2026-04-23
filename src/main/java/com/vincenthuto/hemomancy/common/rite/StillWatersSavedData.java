package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-level persistence for active Still Waters zones established by the
 * Rite of Still Waters. Each entry tracks the caster, zone center, dimension,
 * and the game-tick at which the zone expires.
 * <p>
 * While a Still Waters zone is active, players within it take 30% less magic
 * damage, partially countering Sanguine Dominion's bleed effect.
 */
public class StillWatersSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_still_waters";
	private static final SavedData.Factory<StillWatersSavedData> FACTORY =
			new SavedData.Factory<>(StillWatersSavedData::new, StillWatersSavedData::load, null);

	private final List<StillWatersEntry> entries = new ArrayList<>();

	public StillWatersSavedData() {}

	public static StillWatersSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static StillWatersSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		StillWatersSavedData data = new StillWatersSavedData();
		if (tag.contains("entries", Tag.TAG_LIST)) {
			ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int blockRadius = entry.getInt("BlockRadius");
				long expiryTick = entry.getLong("ExpiryTick");
				data.entries.add(new StillWatersEntry(ownerUUID, center, dimension, blockRadius, expiryTick));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (StillWatersEntry entry : entries) {
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

	public void addEntry(StillWatersEntry entry) {
		entries.add(entry);
		setDirty();
	}

	public List<StillWatersEntry> getEntries() {
		return entries;
	}

	/** Removes all expired entries. Returns true if anything was removed. */
	public boolean removeExpired(long currentTick) {
		boolean changed = entries.removeIf(e -> currentTick >= e.expiryTick());
		if (changed) setDirty();
		return changed;
	}

	/**
	 * Returns true if the given block position in the given dimension is inside
	 * at least one active (non-expired) Still Waters zone.
	 */
	public boolean isInZone(BlockPos pos, String dimension, long currentTick) {
		for (StillWatersEntry entry : entries) {
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
	 * A single Still Waters zone entry.
	 *
	 * @param ownerUUID   UUID of the player who cast the rite
	 * @param center      center block position
	 * @param dimension   dimension registry-key string
	 * @param blockRadius zone radius in blocks
	 * @param expiryTick  world game-tick at which this entry expires
	 */
	public record StillWatersEntry(UUID ownerUUID, BlockPos center, String dimension,
			int blockRadius, long expiryTick) {}
}
