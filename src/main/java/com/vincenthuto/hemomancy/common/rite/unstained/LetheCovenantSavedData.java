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
 * Active Lethe Covenant domains from the Rites of the Lethe Covenant and the
 * Lethean Font. While a domain is active:
 * <ul>
 *   <li>Natural mob spawns within it have a 50% chance of being denied
 *       (see {@link com.vincenthuto.hemomancy.common.rite.zone.RiteZoneSpawnRules}).</li>
 *   <li>Players with Silver Ward active take zero magic (bleed) damage.</li>
 *   <li>Unstained players in the domain gain +0.2 purity per minute.</li>
 * </ul>
 */
public class LetheCovenantSavedData extends RiteZoneSavedData {

	private static final String DATA_NAME = "hemomancy_lethe_covenant";
	private static final SavedData.Factory<LetheCovenantSavedData> FACTORY =
			new SavedData.Factory<>(LetheCovenantSavedData::new, LetheCovenantSavedData::load, null);

	public LetheCovenantSavedData() {
		super(RiteZoneKind.LETHE_COVENANT);
	}

	public static LetheCovenantSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static LetheCovenantSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		return loadInto(new LetheCovenantSavedData(), tag);
	}

	public void addEntry(CovenantEntry entry) {
		addZone(entry.toZone());
	}

	public List<CovenantEntry> getEntries() {
		return zones().stream().map(CovenantEntry::of).toList();
	}

	/** Returns true if the position is inside any active domain in that dimension. */
	public boolean isInDomain(BlockPos pos, String dimension, long currentTick) {
		ResourceKey<Level> key = RiteZone.dimensionKey(dimension);
		return key != null && contains(pos, key, currentTick);
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
			int chunkRadius, long expiryTick) {
		RiteZone toZone() {
			return RiteZone.timed(RiteZoneKind.LETHE_COVENANT, ownerUUID, center,
					RiteZone.requireDimensionKey(dimension), chunkRadius * 16, expiryTick);
		}

		static CovenantEntry of(RiteZone zone) {
			return new CovenantEntry(zone.owner(), zone.center(), zone.dimensionId(),
					zone.blockRadius() / 16, zone.expiryTick());
		}
	}
}
