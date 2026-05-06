package com.vincenthuto.hemomancy.common.event.worldevent;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;

/**
 * Persists blood moon state across server restarts.
 * Stored on the overworld level's data storage.
 */
public class BloodMoonSavedData extends SavedData {

	private static final String DATA_NAME = "hemomancy_blood_moon";
	private static final SavedData.Factory<BloodMoonSavedData> FACTORY =
			new SavedData.Factory<>(BloodMoonSavedData::new, BloodMoonSavedData::load, null);

	private boolean active = false;
	private long endTick = 0L;

	public BloodMoonSavedData() {}

	public static BloodMoonSavedData get(ServerLevel overworld) {
		return overworld.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static BloodMoonSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		BloodMoonSavedData data = new BloodMoonSavedData();
		data.active = tag.getBoolean("Active");
		data.endTick = tag.getLong("EndTick");
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		tag.putBoolean("Active", active);
		tag.putLong("EndTick", endTick);
		return tag;
	}

	public boolean isActive() {
		return active;
	}

	public long getEndTick() {
		return endTick;
	}

	public void start(long endTick) {
		this.active = true;
		this.endTick = endTick;
		setDirty();
	}

	public void stop() {
		this.active = false;
		this.endTick = 0L;
		setDirty();
	}
}
