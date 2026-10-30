package com.vincenthuto.hemomancy.common.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * The chunks of one dimension the adipocere decoration pass has already run in. Only chunks that held a crust site are
 * recorded; once recorded, a chunk is never decorated again, whatever was or was not placed in it.
 */
public final class AdipocereCrustSavedData extends SavedData {
	private static final String NAME = "hemomancy_adipocere_crusts";

	private final LongOpenHashSet decorated = new LongOpenHashSet();

	public static AdipocereCrustSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				new SavedData.Factory<>(AdipocereCrustSavedData::new, AdipocereCrustSavedData::load, null), NAME);
	}

	public boolean isDecorated(ChunkPos chunk) {
		return decorated.contains(chunk.toLong());
	}

	/** Records the chunk; returns false when it was already recorded. */
	public boolean markDecorated(ChunkPos chunk) {
		if (!decorated.add(chunk.toLong())) return false;
		setDirty();
		return true;
	}

	public int size() {
		return decorated.size();
	}

	private static AdipocereCrustSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
		AdipocereCrustSavedData data = new AdipocereCrustSavedData();
		for (long chunk : tag.getLongArray("Decorated")) data.decorated.add(chunk);
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		tag.put("Decorated", new LongArrayTag(decorated.toLongArray()));
		return tag;
	}
}
