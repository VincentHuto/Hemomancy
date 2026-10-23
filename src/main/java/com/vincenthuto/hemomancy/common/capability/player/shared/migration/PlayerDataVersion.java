package com.vincenthuto.hemomancy.common.capability.player.shared.migration;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * The newest {@link PlayerDataMigrations} entry this player's saved data has been through. A player saved before
 * the attachment existed reads 0, so every migration runs for them once.
 */
public class PlayerDataVersion implements INBTSerializable<CompoundTag> {
	private static final String VERSION_KEY = "Version";

	private int version;

	public int version() {
		return version;
	}

	void setVersion(int version) {
		this.version = Math.max(0, version);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putInt(VERSION_KEY, version);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		setVersion(nbt == null ? 0 : nbt.getInt(VERSION_KEY));
	}
}
