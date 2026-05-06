package com.vincenthuto.hemomancy.common.rite.unstained;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * World-level persistence for active Lethe Covenant domains established by
 * the Rite of the Lethe Covenant.  Each entry tracks the caster, domain
 * center, dimension, chunk radius, and expiry tick.
 * <p>
 * While a domain is active:
 * <ul>
 *   <li>Natural mob spawns within it have a 50% chance of being denied.</li>
 *   <li>Players with Silver Ward active take zero magic (bleed) damage.</li>
 *   <li>Unstained players in the domain gain +0.2 purity per minute.</li>
 * </ul>
 */
public class LetheCovenantSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_lethe_covenant";
	private static final SavedData.Factory<LetheCovenantSavedData> FACTORY =
			new SavedData.Factory<>(LetheCovenantSavedData::new, LetheCovenantSavedData::load, null);

	private final List<CovenantEntry> entries = new ArrayList<>();

	public LetheCovenantSavedData() {}

	public static LetheCovenantSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static LetheCovenantSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		LetheCovenantSavedData data = new LetheCovenantSavedData();
		if (tag.contains("entries", Tag.TAG_LIST)) {
			ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int chunkRadius = entry.getInt("ChunkRadius");
				long expiryTick = entry.getLong("ExpiryTick");
				data.entries.add(new CovenantEntry(ownerUUID, center, dimension, chunkRadius, expiryTick));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (CovenantEntry entry : entries) {
			CompoundTag entryTag = new CompoundTag();
			entryTag.putUUID("Owner", entry.ownerUUID());
			entryTag.putLong("Center", entry.center().asLong());
			entryTag.putString("Dimension", entry.dimension());
			entryTag.putInt("ChunkRadius", entry.chunkRadius());
			entryTag.putLong("ExpiryTick", entry.expiryTick());
			list.add(entryTag);
		}
		tag.put("entries", list);
		return tag;
	}

	public void addEntry(CovenantEntry entry) {
		entries.add(entry);
		setDirty();
	}

	public List<CovenantEntry> getEntries() {
		return entries;
	}

	/** Removes all expired entries. Returns true if anything was removed. */
	public boolean removeExpired(long currentTick) {
		boolean changed = entries.removeIf(e -> currentTick >= e.expiryTick());
		if (changed) setDirty();
		return changed;
	}

	/** Returns true if the given block position in the given dimension is inside any active domain. */
	public boolean isInDomain(BlockPos pos, String dimension, long currentTick) {
		for (CovenantEntry entry : entries) {
			if (currentTick >= entry.expiryTick()) continue;
			if (!entry.dimension().equals(dimension)) continue;
			int blockRadius = entry.chunkRadius() * 16;
			BlockPos center = entry.center();
			if (Math.abs(pos.getX() - center.getX()) <= blockRadius
					&& Math.abs(pos.getZ() - center.getZ()) <= blockRadius) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A single Lethe Covenant domain entry.
	 *
	 * @param ownerUUID   UUID of the player who cast the rite
	 * @param center      center block position
	 * @param dimension   dimension registry-key string
	 * @param chunkRadius radius in chunks
	 * @param expiryTick  world game-tick at which this entry expires
	 */
	public record CovenantEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long expiryTick) {}
}
