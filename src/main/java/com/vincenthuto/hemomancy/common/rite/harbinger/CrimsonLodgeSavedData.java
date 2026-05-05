package com.vincenthuto.hemomancy.common.rite.harbinger;

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
 * World-level persistence for Crimson Lodges established via the Rite of the
 * Crimson Lodge (Illuminatus). Each lodge has an owner, center position,
 * dimension, chunk radius, creation timestamp, and a configurable recall point.
 * <p>
 * Only one lodge may exist within a given radius — competing bloodlines
 * cannot share territory. Players in the bloodline may recall to the lodge
 * via the ancestral ledger, and the leader may relocate the recall point.
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
	private static final SavedData.Factory<CrimsonLodgeSavedData> FACTORY =
			new SavedData.Factory<>(CrimsonLodgeSavedData::new, CrimsonLodgeSavedData::load, null);

	private final List<LodgeEntry> lodges = new ArrayList<>();

	public CrimsonLodgeSavedData() {}

	public static CrimsonLodgeSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static CrimsonLodgeSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
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
				BlockPos recallPoint = entry.contains("RecallPoint")
						? BlockPos.of(entry.getLong("RecallPoint"))
						: center;
				data.lodges.add(new LodgeEntry(ownerUUID, center, dimension,
						chunkRadius, createdTick, recallPoint));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (LodgeEntry entry : lodges) {
			CompoundTag lodgeTag = new CompoundTag();
			lodgeTag.putUUID("Owner", entry.ownerUUID);
			lodgeTag.putLong("Center", entry.center.asLong());
			lodgeTag.putString("Dimension", entry.dimension);
			lodgeTag.putInt("ChunkRadius", entry.chunkRadius);
			lodgeTag.putLong("CreatedTick", entry.createdTick);
			lodgeTag.putLong("RecallPoint", entry.recallPoint.asLong());
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
			if (!entry.dimension.equals(dimension)) continue;
			int blockRadius = entry.chunkRadius * 16;
			BlockPos center = entry.center;
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
	 * Find the lodge owned by the given player's bloodline leader.
	 * Returns null if no lodge is found for this owner.
	 */
	public LodgeEntry getLodgeForOwner(UUID ownerUUID) {
		for (LodgeEntry entry : lodges) {
			if (entry.ownerUUID.equals(ownerUUID)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * Check if placing a new lodge at the given position would overlap with any
	 * existing lodge's radius in the same dimension. Two lodge zones overlap
	 * when the distance between their centers is less than the sum of their
	 * radii on both the X and Z axes. Only one lodge may exist per territory —
	 * competing bloodlines cannot share space.
	 *
	 * @return the existing lodge that would overlap, or null if placement is clear
	 */
	public LodgeEntry getOverlappingLodge(BlockPos newCenter, String dimension, int newChunkRadius) {
		int newBlockRadius = newChunkRadius * 16;
		for (LodgeEntry existing : lodges) {
			if (!existing.dimension.equals(dimension)) continue;
			int existingBlockRadius = existing.chunkRadius * 16;
			int dx = Math.abs(newCenter.getX() - existing.center.getX());
			int dz = Math.abs(newCenter.getZ() - existing.center.getZ());
			int combinedRadius = existingBlockRadius + newBlockRadius;
			if (dx <= combinedRadius && dz <= combinedRadius) {
				return existing;
			}
		}
		return null;
	}

	/**
	 * Update the recall point for the lodge owned by the given player.
	 * The new recall point must be within the lodge's radius.
	 *
	 * @return {@code true} if the recall point was updated
	 */
	public boolean setRecallPoint(UUID ownerUUID, BlockPos newRecallPoint) {
		for (LodgeEntry entry : lodges) {
			if (!entry.ownerUUID.equals(ownerUUID)) continue;
			int blockRadius = entry.chunkRadius * 16;
			int dx = Math.abs(newRecallPoint.getX() - entry.center.getX());
			int dz = Math.abs(newRecallPoint.getZ() - entry.center.getZ());
			if (dx <= blockRadius && dz <= blockRadius) {
				entry.recallPoint = newRecallPoint;
				setDirty();
				return true;
			}
		}
		return false;
	}

	/**
	 * A persistent Crimson Lodge entry. The recall point defaults to the
	 * center (ritual location) and can be relocated by the bloodline leader.
	 */
	public static class LodgeEntry {
		private final UUID ownerUUID;
		private final BlockPos center;
		private final String dimension;
		private final int chunkRadius;
		private final long createdTick;
		private BlockPos recallPoint;

		public LodgeEntry(UUID ownerUUID, BlockPos center, String dimension,
				int chunkRadius, long createdTick, BlockPos recallPoint) {
			this.ownerUUID = ownerUUID;
			this.center = center;
			this.dimension = dimension;
			this.chunkRadius = chunkRadius;
			this.createdTick = createdTick;
			this.recallPoint = recallPoint;
		}

		public UUID ownerUUID()    { return ownerUUID; }
		public BlockPos center()   { return center; }
		public String dimension()  { return dimension; }
		public int chunkRadius()   { return chunkRadius; }
		public long createdTick()  { return createdTick; }
		public BlockPos recallPoint() { return recallPoint; }
	}
}
