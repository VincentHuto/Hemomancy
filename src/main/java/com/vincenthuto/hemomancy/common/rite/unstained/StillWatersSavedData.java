package com.vincenthuto.hemomancy.common.rite.unstained;

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
 * Active Still Waters zones from the Rite of Still Waters. Players inside one
 * take 30% less magic damage, partially countering Sanguine Dominion's bleed.
 */
public class StillWatersSavedData extends RiteZoneSavedData {

	private static final String DATA_NAME = "hemomancy_still_waters";
	private static final SavedData.Factory<StillWatersSavedData> FACTORY =
			new SavedData.Factory<>(StillWatersSavedData::new, StillWatersSavedData::load, null);

	public StillWatersSavedData() {
		super(RiteZoneKind.STILL_WATERS);
	}

	public static StillWatersSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static StillWatersSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		return loadInto(new StillWatersSavedData(), tag);
	}

	public void addEntry(StillWatersEntry entry) {
		addZone(entry.toZone());
	}

	public List<StillWatersEntry> getEntries() {
		return zones().stream().map(StillWatersEntry::of).toList();
	}

	/** Returns true if the position is inside at least one active Still Waters zone in that dimension. */
	public boolean isInZone(BlockPos pos, String dimension, long currentTick) {
		ResourceKey<Level> key = RiteZone.dimensionKey(dimension);
		return key != null && contains(pos, key, currentTick);
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
			int blockRadius, long expiryTick) {
		RiteZone toZone() {
			return RiteZone.timed(RiteZoneKind.STILL_WATERS, ownerUUID, center,
					RiteZone.requireDimensionKey(dimension), blockRadius, expiryTick);
		}

		static StillWatersEntry of(RiteZone zone) {
			return new StillWatersEntry(zone.owner(), zone.center(), zone.dimensionId(),
					zone.blockRadius(), zone.expiryTick());
		}
	}
}
