package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The Frozen Cruor a level is waiting to melt: where it stands, the game time it melts at, and whose cast (if anyone's)
 * raised it. Saved with the level, so ice placed before a restart still melts after it.
 */
public final class TemporaryIceSavedData extends SavedData {
	private static final String DATA_NAME = "hemomancy_temporary_ice";
	private static final SavedData.Factory<TemporaryIceSavedData> FACTORY =
			new SavedData.Factory<>(TemporaryIceSavedData::new, TemporaryIceSavedData::load, null);

	/**
	 * One block of ice: when it melts, the caster or encounter that owns it (null for unowned ice), and whether it froze
	 * a water source, which comes back when it melts ({@code water}; otherwise the cell melts to air).
	 */
	public record Entry(long meltsAt, @Nullable UUID owner, boolean water) {
		public Entry(long meltsAt, @Nullable UUID owner) {
			this(meltsAt, owner, false);
		}
	}

	private final Map<BlockPos, Entry> entries = new HashMap<>();

	public static TemporaryIceSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static TemporaryIceSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		TemporaryIceSavedData data = new TemporaryIceSavedData();
		ListTag list = tag.getList("ice", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			data.entries.put(BlockPos.of(entry.getLong("pos")),
					new Entry(entry.getLong("melts_at"), entry.hasUUID("owner") ? entry.getUUID("owner") : null,
							entry.getBoolean("water")));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		entries.forEach((pos, entry) -> {
			CompoundTag saved = new CompoundTag();
			saved.putLong("pos", pos.asLong());
			saved.putLong("melts_at", entry.meltsAt());
			if (entry.owner() != null) saved.putUUID("owner", entry.owner());
			if (entry.water()) saved.putBoolean("water", true);
			list.add(saved);
		});
		tag.put("ice", list);
		return tag;
	}

	public void put(BlockPos pos, long meltsAt, @Nullable UUID owner) {
		put(pos, meltsAt, owner, false);
	}

	public void put(BlockPos pos, long meltsAt, @Nullable UUID owner, boolean water) {
		entries.put(pos.immutable(), new Entry(meltsAt, owner, water));
		setDirty();
	}

	public void remove(BlockPos pos) {
		if (entries.remove(pos) != null) setDirty();
	}

	@Nullable
	public Entry get(BlockPos pos) {
		return entries.get(pos);
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public boolean hasIceOwnedBy(UUID owner) {
		return entries.values().stream().anyMatch(entry -> owner.equals(entry.owner()));
	}

	/** A read-only view; change entries through {@link #put} and {@link #remove}. */
	public Map<BlockPos, Entry> entries() {
		return Collections.unmodifiableMap(entries);
	}
}
