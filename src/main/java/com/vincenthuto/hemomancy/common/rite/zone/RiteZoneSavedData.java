package com.vincenthuto.hemomancy.common.rite.zone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Overworld-stored zones of one kind. Every dimension's zones live in the
 * Overworld's data storage; each zone records its own dimension.
 */
public abstract class RiteZoneSavedData extends SavedData {
	protected final RiteZoneList store;

	protected RiteZoneSavedData(RiteZoneKind kind) {
		this.store = new RiteZoneList(kind);
	}

	protected static <T extends RiteZoneSavedData> T loadInto(T data, CompoundTag tag) {
		data.store.load(tag);
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, @Nullable HolderLookup.Provider provider) {
		return store.save(tag);
	}

	public RiteZoneKind kind() {
		return store.kind();
	}

	public List<RiteZone> zones() {
		return store.zones();
	}

	public List<RiteZone> activeIn(ResourceKey<Level> dimension, long gameTime) {
		return store.activeIn(dimension, gameTime);
	}

	public boolean contains(BlockPos pos, ResourceKey<Level> dimension, long gameTime) {
		return store.contains(pos, dimension, gameTime);
	}

	@Nullable
	public RiteZone zoneAt(BlockPos pos, ResourceKey<Level> dimension, long gameTime) {
		return store.zoneAt(pos, dimension, gameTime);
	}

	public void addZone(RiteZone zone) {
		store.add(zone);
		changed();
	}

	@Nullable
	public RiteZone replaceZoneForOwner(RiteZone zone) {
		RiteZone previous = store.replaceForOwner(zone);
		changed();
		return previous;
	}

	/** Removes all expired zones. Returns true if anything was removed. */
	public boolean removeExpired(long currentTick) {
		boolean removed = store.pruneExpired(currentTick);
		if (removed) changed();
		return removed;
	}

	protected final void changed() {
		setDirty();
		if (kind().shownToClients()) RiteZoneSync.markChanged();
	}
}
