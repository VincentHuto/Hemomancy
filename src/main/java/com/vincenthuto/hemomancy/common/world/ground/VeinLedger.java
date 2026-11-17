package com.vincenthuto.hemomancy.common.world.ground;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Who grew each cell of living vein, across every dimension, so a player's total vein length can be held to their
 * degree ({@link GroundRunRules#veinAllowance}). Saved with the Overworld. A cell leaves the ledger when it is filled
 * in, withers back to a gouge or is broken.
 */
public final class VeinLedger extends SavedData {
	private static final String DATA_NAME = "hemomancy_vein_ledger";
	private static final SavedData.Factory<VeinLedger> FACTORY =
			new SavedData.Factory<>(VeinLedger::new, VeinLedger::load, null);

	private final Map<String, Long2ObjectOpenHashMap<UUID>> owners = new HashMap<>();
	private final Map<UUID, Integer> lengths = new HashMap<>();

	public static VeinLedger get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static VeinLedger get(ServerLevel level) {
		return get(level.getServer());
	}

	public static VeinLedger load(CompoundTag tag, HolderLookup.Provider provider) {
		VeinLedger ledger = new VeinLedger();
		ListTag list = tag.getList("cells", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag cell = list.getCompound(i);
			if (!cell.hasUUID("owner")) continue;
			ledger.add(cell.getString("dimension"), cell.getLong("pos"), cell.getUUID("owner"));
		}
		return ledger;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		owners.forEach((dimension, cells) -> cells.long2ObjectEntrySet().forEach(entry -> {
			CompoundTag cell = new CompoundTag();
			cell.putString("dimension", dimension);
			cell.putLong("pos", entry.getLongKey());
			cell.putUUID("owner", entry.getValue());
			list.add(cell);
		}));
		tag.put("cells", list);
		return tag;
	}

	private static String key(Level level) {
		return level.dimension().location().toString();
	}

	private void add(String dimension, long pos, UUID owner) {
		UUID before = owners.computeIfAbsent(dimension, ignored -> new Long2ObjectOpenHashMap<>()).put(pos, owner);
		if (before != null) lengths.merge(before, -1, Integer::sum);
		lengths.merge(owner, 1, Integer::sum);
	}

	/** Records {@code owner} as the grower of the vein cell at {@code pos}. */
	public void claim(Level level, BlockPos pos, UUID owner) {
		add(key(level), pos.asLong(), owner);
		setDirty();
	}

	/** Forgets the vein cell at {@code pos}. */
	public void release(Level level, BlockPos pos) {
		Long2ObjectOpenHashMap<UUID> cells = owners.get(key(level));
		if (cells == null) return;
		UUID owner = cells.remove(pos.asLong());
		if (owner == null) return;
		lengths.merge(owner, -1, Integer::sum);
		if (lengths.getOrDefault(owner, 0) <= 0) lengths.remove(owner);
		setDirty();
	}

	@Nullable
	public UUID owner(Level level, BlockPos pos) {
		Long2ObjectOpenHashMap<UUID> cells = owners.get(key(level));
		return cells == null ? null : cells.get(pos.asLong());
	}

	/** Cells of living vein {@code owner} keeps grown, in every dimension. */
	public int length(UUID owner) {
		return owner == null ? 0 : lengths.getOrDefault(owner, 0);
	}
}
