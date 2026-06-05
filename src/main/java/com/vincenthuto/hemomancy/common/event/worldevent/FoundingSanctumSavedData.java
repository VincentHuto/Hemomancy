package com.vincenthuto.hemomancy.common.event.worldevent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persists Founding Sanctum locations across server restarts.
 * Each bloodline may anchor one Founding Sanctum per dimension, keyed to its
 * owning player UUID. The sanctum stores both its consecrated center and a
 * configurable recall point used by the ancestral ledger.
 */
public class FoundingSanctumSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_founding_sanctums";
	private static final SavedData.Factory<FoundingSanctumSavedData> FACTORY =
			new SavedData.Factory<>(FoundingSanctumSavedData::new, FoundingSanctumSavedData::load, null);

	/** Block radius of the sanctum's effect — approximately a 5-chunk diameter. */
	public static final double SANCTUM_RADIUS = 40.0;

	/** Map of consecrating player UUID → sanctum center position. */
	private final Map<UUID, SanctumEntry> sanctums = new HashMap<>();

	public FoundingSanctumSavedData() {}

	public static FoundingSanctumSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static FoundingSanctumSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		FoundingSanctumSavedData data = new FoundingSanctumSavedData();
		ListTag list = tag.getList("sanctums", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			UUID uuid = entry.getUUID("player");
			BlockPos center = entry.contains("Center", Tag.TAG_LONG)
					? BlockPos.of(entry.getLong("Center"))
					: new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
			BlockPos recallPoint = entry.contains("RecallPoint", Tag.TAG_LONG)
					? BlockPos.of(entry.getLong("RecallPoint"))
					: center;
			data.sanctums.put(uuid, new SanctumEntry(center, recallPoint));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			CompoundTag e = new CompoundTag();
			e.putUUID("player", entry.getKey());
			e.putLong("Center", entry.getValue().center().asLong());
			e.putLong("RecallPoint", entry.getValue().recallPoint().asLong());
			list.add(e);
		}
		tag.put("sanctums", list);
		return tag;
	}

	public void consecrate(UUID playerUUID, BlockPos center) {
		sanctums.put(playerUUID, new SanctumEntry(center, center));
		setDirty();
	}

	public void remove(UUID playerUUID) {
		sanctums.remove(playerUUID);
		setDirty();
	}

	public boolean hasSanctum(UUID playerUUID) {
		return sanctums.containsKey(playerUUID);
	}

	public BlockPos getSanctumCenter(UUID playerUUID) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null ? entry.center() : null;
	}

	public BlockPos getRecallPoint(UUID playerUUID) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null ? entry.recallPoint() : null;
	}

	public boolean setRecallPoint(UUID playerUUID, BlockPos newRecallPoint) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry == null || !isWithinSanctum(playerUUID, newRecallPoint)) {
			return false;
		}
		sanctums.put(playerUUID, new SanctumEntry(entry.center(), newRecallPoint));
		setDirty();
		return true;
	}

	public boolean isWithinSanctum(UUID playerUUID, BlockPos pos) {
		BlockPos center = getSanctumCenter(playerUUID);
		if (center == null) {
			return false;
		}
		double dx = pos.getX() - center.getX();
		double dz = pos.getZ() - center.getZ();
		return dx * dx + dz * dz <= SANCTUM_RADIUS * SANCTUM_RADIUS;
	}

	/** Returns a snapshot of all active sanctum center positions. */
	public Map<UUID, BlockPos> getAllSanctums() {
		Map<UUID, BlockPos> centers = new LinkedHashMap<>();
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			centers.put(entry.getKey(), entry.getValue().center());
		}
		return centers;
	}

	private record SanctumEntry(BlockPos center, BlockPos recallPoint) {
	}
}
