package com.vincenthuto.hemomancy.common.encounter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Mycophant wins taken at the killing blow and not yet settled (PT-81). The yield settles one when it ends with the owner
 * online; an owner who dies in the yield settles at once, and one who logs out settles at the next login.
 */
public final class MycophantWinSavedData extends SavedData {
	private static final Factory<MycophantWinSavedData> FACTORY =
			new Factory<>(MycophantWinSavedData::new, MycophantWinSavedData::load, null);
	private final Set<UUID> unsettled = new HashSet<>();

	public static MycophantWinSavedData get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, "hemomancy_mycophant_wins");
	}

	public void record(UUID owner) {
		if (unsettled.add(owner)) setDirty();
	}

	public boolean isRecorded(UUID owner) {
		return unsettled.contains(owner);
	}

	public void clear(UUID owner) {
		if (unsettled.remove(owner)) setDirty();
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag owners = new ListTag();
		for (UUID owner : unsettled) owners.add(NbtUtils.createUUID(owner));
		tag.put("unsettled", owners);
		return tag;
	}

	private static MycophantWinSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		MycophantWinSavedData data = new MycophantWinSavedData();
		for (Tag owner : tag.getList("unsettled", Tag.TAG_INT_ARRAY)) data.unsettled.add(NbtUtils.loadUUID(owner));
		return data;
	}
}
