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
 * Active Pale Consecration zones from the Rites of Pale Consecration and
 * Antiseptic Ground. Hostile mobs inside one periodically take 1 damage and
 * Slowness I, the consecrated ground rejecting blood-touched creatures.
 */
public class PaleConsecrationSavedData extends RiteZoneSavedData {

	private static final String DATA_NAME = "hemomancy_pale_consecration";
	private static final SavedData.Factory<PaleConsecrationSavedData> FACTORY =
			new SavedData.Factory<>(PaleConsecrationSavedData::new, PaleConsecrationSavedData::load, null);

	public PaleConsecrationSavedData() {
		super(RiteZoneKind.PALE_CONSECRATION);
	}

	public static PaleConsecrationSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static PaleConsecrationSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		return loadInto(new PaleConsecrationSavedData(), tag);
	}

	public void addEntry(ConsecrationEntry entry) {
		addZone(entry.toZone());
	}

	public List<ConsecrationEntry> getEntries() {
		return zones().stream().map(ConsecrationEntry::of).toList();
	}

	/** Returns true if the position is inside at least one active zone in that dimension. */
	public boolean isInZone(BlockPos pos, String dimension, long currentTick) {
		ResourceKey<Level> key = RiteZone.dimensionKey(dimension);
		return key != null && contains(pos, key, currentTick);
	}

	/**
	 * A single Pale Consecration zone entry.
	 *
	 * @param ownerUUID   UUID of the player who cast the rite
	 * @param center      center block position
	 * @param dimension   dimension registry-key string
	 * @param blockRadius zone radius in blocks
	 * @param expiryTick  world game-tick at which this entry expires
	 */
	public record ConsecrationEntry(UUID ownerUUID, BlockPos center, String dimension,
			int blockRadius, long expiryTick) {
		RiteZone toZone() {
			return RiteZone.timed(RiteZoneKind.PALE_CONSECRATION, ownerUUID, center,
					RiteZone.requireDimensionKey(dimension), blockRadius, expiryTick);
		}

		static ConsecrationEntry of(RiteZone zone) {
			return new ConsecrationEntry(zone.owner(), zone.center(), zone.dimensionId(),
					zone.blockRadius(), zone.expiryTick());
		}
	}
}
