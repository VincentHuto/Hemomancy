package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.rite.zone.RiteZone;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZoneKind;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZoneSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;
import java.util.UUID;

/**
 * Active Sanguine Fervor zones from the Rite of Sanguine Fervor. While one is
 * active, natural mob spawns within it are forced through even when the mob
 * cap has been reached, unless a Lethe Covenant domain covers the same spot
 * (see {@link com.vincenthuto.hemomancy.common.rite.zone.RiteZoneSpawnRules}).
 */
public class SanguineFervorSavedData extends RiteZoneSavedData {

	private static final String DATA_NAME = "hemomancy_sanguine_fervor";
	private static final SavedData.Factory<SanguineFervorSavedData> FACTORY =
			new SavedData.Factory<>(SanguineFervorSavedData::new, SanguineFervorSavedData::load, null);

	public SanguineFervorSavedData() {
		super(RiteZoneKind.SANGUINE_FERVOR);
	}

	public static SanguineFervorSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static SanguineFervorSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		return loadInto(new SanguineFervorSavedData(), tag);
	}

	public void addEntry(FervorEntry entry) {
		addZone(entry.toZone());
	}

	public List<FervorEntry> getEntries() {
		return zones().stream().map(FervorEntry::of).toList();
	}

	/** Returns true if the position is inside at least one active fervor zone in that dimension. */
	public boolean isInFervorRange(BlockPos pos, String dimension, long currentTick) {
		ResourceKey<Level> key = RiteZone.dimensionKey(dimension);
		return key != null && contains(pos, key, currentTick);
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
			int chunkRadius, long expiryTick) {
		RiteZone toZone() {
			return RiteZone.timed(RiteZoneKind.SANGUINE_FERVOR, ownerUUID, center,
					RiteZone.requireDimensionKey(dimension), chunkRadius * 16, expiryTick);
		}

		static FervorEntry of(RiteZone zone) {
			return new FervorEntry(zone.owner(), zone.center(), zone.dimensionId(),
					zone.blockRadius() / 16, zone.expiryTick());
		}
	}
}
