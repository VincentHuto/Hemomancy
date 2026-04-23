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
 * World-level persistence for active Sanguine Fervor zones established by the
 * Rite of Sanguine Fervor. Each entry tracks who cast the rite, where it is
 * centred, the chunk radius of effect, the dimension, and the game-tick at
 * which the boost expires.
 * <p>
 * While a fervor zone is active, natural mob spawns within its radius are
 * force-allowed even when the global mob cap has been reached, effectively
 * increasing local spawn density.
 */
public class SanguineFervorSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_sanguine_fervor";
	private static final SavedData.Factory<SanguineFervorSavedData> FACTORY =
			new SavedData.Factory<>(SanguineFervorSavedData::new, SanguineFervorSavedData::load, null);

	private final List<FervorEntry> entries = new ArrayList<>();

	public SanguineFervorSavedData() {}

	public static SanguineFervorSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static SanguineFervorSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		SanguineFervorSavedData data = new SanguineFervorSavedData();
		if (tag.contains("entries", Tag.TAG_LIST)) {
			ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID ownerUUID = entry.getUUID("Owner");
				BlockPos center = BlockPos.of(entry.getLong("Center"));
				String dimension = entry.getString("Dimension");
				int chunkRadius = entry.getInt("ChunkRadius");
				long expiryTick = entry.getLong("ExpiryTick");
				data.entries.add(new FervorEntry(ownerUUID, center, dimension, chunkRadius, expiryTick));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (FervorEntry entry : entries) {
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

	public void addEntry(FervorEntry entry) {
		entries.add(entry);
		setDirty();
	}

	public List<FervorEntry> getEntries() {
		return entries;
	}

	/**
	 * Removes all entries whose expiry tick has passed. Returns {@code true} if
	 * any entries were removed (so the caller knows to persist the change).
	 */
	public boolean removeExpired(long currentTick) {
		boolean changed = entries.removeIf(e -> currentTick >= e.expiryTick());
		if (changed) setDirty();
		return changed;
	}

	/**
	 * Returns {@code true} if the given block position in the given dimension is
	 * inside at least one active (not yet expired) fervor zone.
	 */
	public boolean isInFervorRange(BlockPos pos, String dimension, long currentTick) {
		for (FervorEntry entry : entries) {
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
	 * A single Sanguine Fervor zone entry.
	 *
	 * @param ownerUUID   UUID of the player who cast the rite
	 * @param center      centre block position of the rite
	 * @param dimension   registry-key string of the dimension (e.g. {@code "minecraft:overworld"})
	 * @param chunkRadius radius in chunks for the effect
	 * @param expiryTick  world game-tick at which this entry expires
	 */
	public record FervorEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long expiryTick) {}
}
