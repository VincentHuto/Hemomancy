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
 */
public class QliphothBloomSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_qliphoth_blooms";

	private final List<BloomEntry> blooms = new ArrayList<>();

	public QliphothBloomSavedData() {}

	public static QliphothBloomSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(
				QliphothBloomSavedData::load, QliphothBloomSavedData::new, DATA_NAME);
	}

	public static QliphothBloomSavedData load(CompoundTag tag) {
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
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag) {
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
	 * Remove the bloom whose center is in the same chunk as the given position
	 * in the given dimension. Returns the removed entry, or null if none found.
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
