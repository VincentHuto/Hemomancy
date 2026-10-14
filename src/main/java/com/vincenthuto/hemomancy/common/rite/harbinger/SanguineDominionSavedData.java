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
 * Blood Domains established by the Rite of Sanguine Dominion. Each owner holds
 * at most one; a domain never expires.
 * <p>
 * Effects within a domain:
 * <ul>
 *   <li>The owner's manipulations cost 25% less blood ({@code ManipulationCostLedger})</li>
 *   <li>Non-allied entities take slow bleed damage (0.5/2s)</li>
 * </ul>
 */
public class SanguineDominionSavedData extends RiteZoneSavedData {

	private static final String DATA_NAME = "hemomancy_sanguine_dominions";
	private static final SavedData.Factory<SanguineDominionSavedData> FACTORY =
			new SavedData.Factory<>(SanguineDominionSavedData::new, SanguineDominionSavedData::load, null);

	public SanguineDominionSavedData() {
		super(RiteZoneKind.SANGUINE_DOMINION);
	}

	public static SanguineDominionSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static SanguineDominionSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		return loadInto(new SanguineDominionSavedData(), tag);
	}

	public void addDominion(DominionEntry entry) {
		addZone(entry.toZone());
	}

	/**
	 * Establishes the owner's single personal domain, returning the former
	 * location when the rite relocated one.
	 */
	public DominionEntry replaceDominionForOwner(DominionEntry entry) {
		RiteZone previous = replaceZoneForOwner(entry.toZone());
		return previous == null ? null : DominionEntry.of(previous);
	}

	public List<DominionEntry> getDominions() {
		return zones().stream().map(DominionEntry::of).toList();
	}

	/** True when one of the player's own domains covers the position. */
	public boolean isOwnerAt(UUID playerUUID, BlockPos pos, ResourceKey<Level> dimension) {
		for (RiteZone zone : zones()) {
			if (zone.owner().equals(playerUUID) && zone.dimension().equals(dimension) && zone.covers(pos)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * A persistent blood domain entry.
	 */
	public record DominionEntry(UUID ownerUUID, BlockPos center, String dimension,
			int chunkRadius, long createdTick) {
		RiteZone toZone() {
			return RiteZone.permanent(RiteZoneKind.SANGUINE_DOMINION, ownerUUID, center,
					RiteZone.requireDimensionKey(dimension), chunkRadius * 16, createdTick);
		}

		static DominionEntry of(RiteZone zone) {
			return new DominionEntry(zone.owner(), zone.center(), zone.dimensionId(),
					zone.blockRadius() / 16, zone.createdTick());
		}
	}
}
