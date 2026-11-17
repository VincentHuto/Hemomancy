package com.vincenthuto.hemomancy.common.world.farm;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * A chunk's fed ground (core-loop Track 7): for each soil position a drip or a worked gutter fed, the game time it
 * stays fed until. Read lazily: a stamp past its time simply no longer feeds, and is dropped on the next write or save.
 * The {@code hemomancy:fed_ground} chunk attachment, read through {@code HemoCapabilityAccess}.
 */
public final class FedGroundStamps implements INBTSerializable<CompoundTag> {
	private final Long2LongOpenHashMap until = new Long2LongOpenHashMap();

	/** The time {@code soil} stays fed until, or {@link Long#MIN_VALUE} when it was never stamped. */
	public long until(BlockPos soil) {
		return until.getOrDefault(soil.asLong(), Long.MIN_VALUE);
	}

	/** Stamps {@code soil} fed for {@code ticks} from {@code now}, never shortening a later stamp. */
	public void stamp(BlockPos soil, long now, int ticks) {
		prune(now);
		until.put(soil.asLong(), BloodFedGrowthRules.restamp(until(soil), now, ticks));
	}

	public int size() {
		return until.size();
	}

	private void prune(long now) {
		until.long2LongEntrySet().removeIf(entry -> !BloodFedGrowthRules.stampFeeds(entry.getLongValue(), now));
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		long[] positions = new long[until.size()];
		long[] times = new long[until.size()];
		int i = 0;
		for (var entry : until.long2LongEntrySet()) {
			positions[i] = entry.getLongKey();
			times[i++] = entry.getLongValue();
		}
		tag.putLongArray("Positions", positions);
		tag.putLongArray("Until", times);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		until.clear();
		long[] positions = tag.getLongArray("Positions");
		long[] times = tag.getLongArray("Until");
		for (int i = 0; i < Math.min(positions.length, times.length); i++) until.put(positions[i], times[i]);
	}
}
