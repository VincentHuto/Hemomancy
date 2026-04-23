package com.vincenthuto.hemomancy.common.rite;

import java.util.HashMap;
import java.util.Map;
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
 * World-level persistence for Crimson Beacon waypoints. Each player can have
 * at most one active beacon. When the player takes fatal damage, they are
 * teleported to the beacon position before dying (so their corpse is easier
 * to retrieve).
 */
public class CrimsonBeaconSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_crimson_beacons";
	private static final SavedData.Factory<CrimsonBeaconSavedData> FACTORY =
			new SavedData.Factory<>(CrimsonBeaconSavedData::new, CrimsonBeaconSavedData::load, null);

	private final Map<UUID, BeaconEntry> beacons = new HashMap<>();

	public CrimsonBeaconSavedData() {}

	public static CrimsonBeaconSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static CrimsonBeaconSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		CrimsonBeaconSavedData data = new CrimsonBeaconSavedData();
		if (tag.contains("beacons", Tag.TAG_LIST)) {
			ListTag list = tag.getList("beacons", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				UUID playerUUID = entry.getUUID("Player");
				BlockPos pos = BlockPos.of(entry.getLong("Pos"));
				String dimension = entry.getString("Dimension");
				data.beacons.put(playerUUID, new BeaconEntry(pos, dimension));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, BeaconEntry> entry : beacons.entrySet()) {
			CompoundTag beaconTag = new CompoundTag();
			beaconTag.putUUID("Player", entry.getKey());
			beaconTag.putLong("Pos", entry.getValue().pos().asLong());
			beaconTag.putString("Dimension", entry.getValue().dimension());
			list.add(beaconTag);
		}
		tag.put("beacons", list);
		return tag;
	}

	public void setBeacon(UUID playerUUID, BlockPos pos, String dimension) {
		beacons.put(playerUUID, new BeaconEntry(pos, dimension));
		setDirty();
	}

	public BeaconEntry getBeacon(UUID playerUUID) {
		return beacons.get(playerUUID);
	}

	public boolean hasBeacon(UUID playerUUID) {
		return beacons.containsKey(playerUUID);
	}

	public void removeBeacon(UUID playerUUID) {
		if (beacons.remove(playerUUID) != null) {
			setDirty();
		}
	}

	/**
	 * A single beacon waypoint: a position and the dimension it was placed in.
	 */
	public record BeaconEntry(BlockPos pos, String dimension) {}
}
