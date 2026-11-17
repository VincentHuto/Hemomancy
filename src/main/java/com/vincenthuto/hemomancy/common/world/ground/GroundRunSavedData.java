package com.vincenthuto.hemomancy.common.world.ground;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The lit cells of a level's gouge runs: when each lights (a pulse runs outward a few cells a tick), when it goes dark,
 * the cell it was reached from (a pulling run carries items that way), its function, and whether it is the flaring end
 * of a pointing run, and which furnaces beside it a hot cell burns. Saved with the level, with the cells' scheduled ticks, so a lit run outlives a restart and still
 * goes dark on time.
 */
public final class GroundRunSavedData extends SavedData {
	private static final String DATA_NAME = "hemomancy_ground_runs";
	private static final SavedData.Factory<GroundRunSavedData> FACTORY =
			new SavedData.Factory<>(GroundRunSavedData::new, GroundRunSavedData::load, null);

	/**
	 * One lit cell. {@code owner} is the player whose pulse (or whose run, kept by a feeder) lit it: the hot and cold
	 * runs light and freeze blocks for that player only, through {@code PlayerBlockEdits}. Null for a run nobody owns.
	 * {@code furnaceSides} is a mask of the faces ({@code 1 << Direction#get3DDataValue}) where a hot cell may burn the
	 * furnace beside it: the run's pulse gives its four nearest furnaces out among its cells (CQ-43).
	 */
	public record Lit(long litAt, long until, long toward, GroundFunction function, boolean flare, @Nullable UUID owner,
			int furnaceSides) {
		public BlockPos towardPos() {
			return BlockPos.of(toward);
		}
	}

	private final Long2ObjectOpenHashMap<Lit> cells = new Long2ObjectOpenHashMap<>();

	public static GroundRunSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static GroundRunSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		GroundRunSavedData data = new GroundRunSavedData();
		ListTag list = tag.getList("cells", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag cell = list.getCompound(i);
			GroundFunction function = GroundFunction.byName(cell.getString("function"));
			data.cells.put(cell.getLong("pos"), new Lit(cell.getLong("lit_at"), cell.getLong("until"),
					cell.getLong("toward"), function == null ? GroundFunction.NONE : function, cell.getBoolean("flare"),
					cell.hasUUID("owner") ? cell.getUUID("owner") : null, cell.getInt("furnace_sides")));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		cells.long2ObjectEntrySet().forEach(entry -> {
			CompoundTag cell = new CompoundTag();
			Lit lit = entry.getValue();
			cell.putLong("pos", entry.getLongKey());
			cell.putLong("lit_at", lit.litAt());
			cell.putLong("until", lit.until());
			cell.putLong("toward", lit.toward());
			cell.putString("function", lit.function().getSerializedName());
			if (lit.flare()) cell.putBoolean("flare", true);
			if (lit.owner() != null) cell.putUUID("owner", lit.owner());
			if (lit.furnaceSides() != 0) cell.putInt("furnace_sides", lit.furnaceSides());
			list.add(cell);
		});
		tag.put("cells", list);
		return tag;
	}

	public void put(BlockPos pos, Lit lit) {
		cells.put(pos.asLong(), lit);
		setDirty();
	}

	public void remove(BlockPos pos) {
		if (cells.remove(pos.asLong()) != null) setDirty();
	}

	@Nullable
	public Lit get(BlockPos pos) {
		return cells.get(pos.asLong());
	}

	public int size() {
		return cells.size();
	}
}
