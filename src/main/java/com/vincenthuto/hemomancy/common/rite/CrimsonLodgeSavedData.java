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
 * World-level persistence for Crimson Lodges established via the Rite of the
 * Crimson Lodge (Illuminatus). Each lodge has an owner, center position,
 * dimension, chunk radius, and creation timestamp.
 * <p>
 * Effects within a lodge's radius:
 * <ul>
 *   <li>Strength I (melee damage bonus)</li>
 *   <li>Enhanced blood regeneration (+5 blood/tick)</li>
 * </ul>
 * Additionally, the player may summon recruited NPC Harbingers to any
 * position within the lodge radius via the ancestral ledger.
 */
public class CrimsonLodgeSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_crimson_lodges";

	private final List<LodgeEntry> lodges = new ArrayList<>();

	public CrimsonLodgeSavedData() {}

	public static CrimsonLodgeSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(
				CrimsonLodgeSavedData::load, CrimsonLodgeSavedData::new, DATA_NAME);
	}

	public static CrimsonLodgeSavedData load(CompoundTag tag) {
		CrimsonLodgeSavedData data = new CrimsonLodgeSavedData();
		if (tag.contains("lodges", Tag.TAG_LIST)) {
			ListTag list = tag.getList("lodges", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int chunkRadius = entry.getInt("ChunkRadius");
				long createdTick = entry.getLong("CreatedTick");
				data.lodges.add(new LodgeEntry(ownerUUID, center, dimension, chunkRadius, createdTick));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag) {
		ListTag list = new ListTag();
		for (LodgeEntry entry : lodges) {
			CompoundTag lodgeTag = new CompoundTag();
			lodgeTag.putUUID("Owner", entry.ownerUUID());
			lodgeTag.putLong("Center", entry.center().asLong());
			lodgeTag.putString("Dimension", entry.dimension());
			lodgeTag.putInt("ChunkRadius", entry.chunkRadius());
			lodgeTag.putLong("CreatedTick", entry.createdTick());
			list.add(lodgeTag);
		}
		tag.put("lodges", list);
		return tag;
	}

	public void addLodge(LodgeEntry entry) {
		lodges.add(entry);
		setDirty();
	}

	public List<LodgeEntry> getLodges() {
		return lodges;
	}

	/**
	 * Find the lodge that contains a given block position in a given dimension.
	 * Returns null if no lodge covers that location.
	 */
	public LodgeEntry getLodgeAt(BlockPos pos, String dimension) {
		for (LodgeEntry entry : lodges) {
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
	 * Check if any lodge covers the given position in the given dimension.
	 */
	public boolean isInLodgeRange(BlockPos pos, String dimension) {
		return getLodgeAt(pos, dimension) != null;
	}

	/**
	 * Check if placing a new lodge at the given position would overlap with any
	 * existing lodge's radius in the same dimension.
	 *
	 * @return the existing lodge that would overlap, or null if placement is clear
	 */
	public LodgeEntry getOverlappingLodge(BlockPos newCenter, String dimension, int newChunkRadius) {
		int newBlockRadius = newChunkRadius * 16;
		for (LodgeEntry existing : lodges) {
			if (!existing.dimension().equals(dimension)) continue;
			int existingBlockRadius = existing.chunkRadius() * 16;
			int dx = Math.abs(newCenter.getX() - existing.center().getX());
			int dz = Math.abs(newCenter.getZ() - existing.center().getZ());
			if ((dx <= existingBlockRadius && dz <= existingBlockRadius)
					|| (dx <= newBlockRadius && dz <= newBlockRadius)) {
				return existing;
			}
		}
		return null;
	}

	/**
	 * A persistent Crimson Lodge entry.
	 */
	public record LodgeEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long createdTick) {}
}
