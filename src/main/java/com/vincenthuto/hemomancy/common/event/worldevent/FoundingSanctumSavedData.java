package com.vincenthuto.hemomancy.common.event.worldevent;

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
 * Persists Founding Sanctum locations across server restarts.
 * Each Illuminatus player (degree 5+) may consecrate one sanctum per dimension.
 * Harbingers standing within the sanctum radius receive passive buffs.
 */
public class FoundingSanctumSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_founding_sanctums";
	private static final SavedData.Factory<FoundingSanctumSavedData> FACTORY =
			new SavedData.Factory<>(FoundingSanctumSavedData::new, FoundingSanctumSavedData::load, null);

	/** Block radius of the sanctum's effect — approximately a 5-chunk diameter. */
	public static final double SANCTUM_RADIUS = 40.0;

	/** Map of consecrating player UUID → sanctum center position. */
	private final Map<UUID, BlockPos> sanctums = new HashMap<>();

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
			BlockPos pos = new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
			data.sanctums.put(uuid, pos);
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, BlockPos> entry : sanctums.entrySet()) {
			CompoundTag e = new CompoundTag();
			e.putUUID("player", entry.getKey());
			e.putInt("x", entry.getValue().getX());
			e.putInt("y", entry.getValue().getY());
			e.putInt("z", entry.getValue().getZ());
			list.add(e);
		}
		tag.put("sanctums", list);
		return tag;
	}

	public void consecrate(UUID playerUUID, BlockPos center) {
		sanctums.put(playerUUID, center);
		setDirty();
	}

	public void remove(UUID playerUUID) {
		sanctums.remove(playerUUID);
		setDirty();
	}

	public boolean hasSanctum(UUID playerUUID) {
		return sanctums.containsKey(playerUUID);
	}

	/** Returns a snapshot of all active sanctum positions; do not modify. */
	public Map<UUID, BlockPos> getAllSanctums() {
		return sanctums;
	}
}
