package com.vincenthuto.hemomancy.common.rite.harbinger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * World-level persistence for Qliphoth Blooms established via the Bloom of the
 * Qliphoth rite. Each bloom has an owner, center position, dimension,
 * chunk radius, and creation timestamp.
 * <p>
 * Effects within a bloom's radius:
 * <ul>
 *   <li>All blood manipulations cost 25% less blood</li>
 *   <li>Passive health regeneration (Regeneration I) every 2 seconds</li>
 *   <li>Enhanced blood regeneration rate</li>
 * </ul>
 * <p>
 * Each bloom tracks how many Qliphoth Pomes have been dropped from it.
 * A tree's lifecycle produces exactly nine pomes (one per Qliphoth husk);
 * after the ninth drop the tree ceases producing fruit until re-summoned.
 */
public class QliphothBloomSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_qliphoth_blooms";
	private static final SavedData.Factory<QliphothBloomSavedData> FACTORY =
			new SavedData.Factory<>(QliphothBloomSavedData::new, QliphothBloomSavedData::load, null);
	/** Maximum pomes a single bloom produces before its lifecycle is exhausted. */
	public static final int MAX_POMES_PER_BLOOM = 9;

	private final List<BloomEntry> blooms = new ArrayList<>();

	/**
	 * Tracks how many pomes have been dropped by each bloom, keyed by the
	 * bloom center's {@link BlockPos#asLong()} value. Cleared when a bloom is
	 * pruned or removed.
	 */
	private final Map<Long, Integer> pomesDroppedByBloom = new HashMap<>();

	public QliphothBloomSavedData() {}

	public static QliphothBloomSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static QliphothBloomSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		QliphothBloomSavedData data = new QliphothBloomSavedData();
		if (tag.contains("blooms", Tag.TAG_LIST)) {
			ListTag list = tag.getList("blooms", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int chunkRadius = entry.getInt("ChunkRadius");
				long createdTick = entry.getLong("CreatedTick");
				data.blooms.add(new BloomEntry(ownerUUID, center, dimension, chunkRadius, createdTick));
			}
		}
		if (tag.contains("pomesDropped", Tag.TAG_LIST)) {
			ListTag pdList = tag.getList("pomesDropped", Tag.TAG_COMPOUND);
			for (int i = 0; i < pdList.size(); i++) {
				CompoundTag entry = pdList.getCompound(i);
				long centerLong = entry.getLong("Center");
				int count = entry.getInt("Count");
				data.pomesDroppedByBloom.put(centerLong, count);
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (BloomEntry entry : blooms) {
			CompoundTag bloomTag = new CompoundTag();
			bloomTag.putUUID("Owner", entry.ownerUUID());
			bloomTag.putLong("Center", entry.center().asLong());
			bloomTag.putString("Dimension", entry.dimension());
			bloomTag.putInt("ChunkRadius", entry.chunkRadius());
			bloomTag.putLong("CreatedTick", entry.createdTick());
			list.add(bloomTag);
		}
		tag.put("blooms", list);

		ListTag pdList = new ListTag();
		for (Map.Entry<Long, Integer> pd : pomesDroppedByBloom.entrySet()) {
			CompoundTag pdTag = new CompoundTag();
			pdTag.putLong("Center", pd.getKey());
			pdTag.putInt("Count", pd.getValue());
			pdList.add(pdTag);
		}
		tag.put("pomesDropped", pdList);

		return tag;
	}

	public void addBloom(BloomEntry entry) {
		blooms.add(entry);
		setDirty();
	}

	public List<BloomEntry> getBlooms() {
		return blooms;
	}

	/**
	 * Returns the number of pomes already dropped from the bloom at the given
	 * center position. Returns 0 if no pomes have been dropped yet.
	 */
	public int getPomesDropped(BlockPos center) {
		return pomesDroppedByBloom.getOrDefault(center.asLong(), 0);
	}

	/**
	 * Increments the pome-drop counter for the given bloom center and marks
	 * the data as dirty. Returns the new count after incrementing.
	 */
	public int incrementPomesDropped(BlockPos center) {
		long key = center.asLong();
		int next = pomesDroppedByBloom.getOrDefault(key, 0) + 1;
		pomesDroppedByBloom.put(key, next);
		setDirty();
		return next;
	}

	/**
	 * Returns the remaining number of pomes this bloom can still drop,
	 * i.e. {@link #MAX_POMES_PER_BLOOM} minus the number already dropped.
	 * Returns 0 once the lifecycle is exhausted.
	 */
	public int getRemainingPomes(BlockPos center) {
		return Math.max(0, MAX_POMES_PER_BLOOM - getPomesDropped(center));
	}

	/**
	 * Find the bloom that contains a given block position in a given dimension.
	 * Returns null if no bloom covers that location.
	 */
	public BloomEntry getBloomAt(BlockPos pos, String dimension) {
		for (BloomEntry entry : blooms) {
			if (!entry.dimension().equals(dimension)) continue;
			int blockRadius = entry.chunkRadius() * 16;
			BlockPos center = entry.center();
			if (Math.abs(pos.getX() - center.getX()) <= blockRadius
					&& Math.abs(pos.getZ() - center.getZ()) <= blockRadius) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Check if any bloom covers the given position in the given dimension.
	 */
	public boolean isInBloomRange(BlockPos pos, String dimension) {
		return getBloomAt(pos, dimension) != null;
	}

	/**
	 * Check if placing a new bloom at the given position would overlap with any
	 * existing bloom's radius in the same dimension. Two blooms overlap if
	 * either center falls within the other's chunk radius.
	 *
	 * @return the existing bloom that would overlap, or null if placement is clear
	 */
	public BloomEntry getOverlappingBloom(BlockPos newCenter, String dimension, int newChunkRadius) {
		int newBlockRadius = newChunkRadius * 16;
		for (BloomEntry existing : blooms) {
			if (!existing.dimension().equals(dimension)) continue;
			int existingBlockRadius = existing.chunkRadius() * 16;
			int dx = Math.abs(newCenter.getX() - existing.center().getX());
			int dz = Math.abs(newCenter.getZ() - existing.center().getZ());
			// Overlap if the new center is inside the existing radius
			// or the existing center is inside the new radius
			if ((dx <= existingBlockRadius && dz <= existingBlockRadius)
					|| (dx <= newBlockRadius && dz <= newBlockRadius)) {
				return existing;
			}
		}
		return null;
	}

	/**
	 * Remove the bloom whose center is in the same chunk as the given position
	 * in the given dimension. Returns the removed entry, or null if none found.
	 * Also clears the pomes-dropped counter for that bloom.
	 */
	public BloomEntry removeBloomInChunk(BlockPos pos, String dimension) {
		int chunkX = pos.getX() >> 4;
		int chunkZ = pos.getZ() >> 4;
		for (int i = 0; i < blooms.size(); i++) {
			BloomEntry entry = blooms.get(i);
			if (!entry.dimension().equals(dimension)) continue;
			int bloomChunkX = entry.center().getX() >> 4;
			int bloomChunkZ = entry.center().getZ() >> 4;
			if (bloomChunkX == chunkX && bloomChunkZ == chunkZ) {
				blooms.remove(i);
				pomesDroppedByBloom.remove(entry.center().asLong());
				setDirty();
				return entry;
			}
		}
		return null;
	}

	/**
	 * A persistent Qliphoth Bloom entry.
	 */
	public record BloomEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long createdTick) {}
}
