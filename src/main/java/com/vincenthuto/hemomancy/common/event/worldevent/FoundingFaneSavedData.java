package com.vincenthuto.hemomancy.common.event.worldevent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.*;

public class FoundingFaneSavedData extends SavedData {
	private static final String DATA_NAME = "hemomancy_founding_fanes";
	private static final SavedData.Factory<FoundingFaneSavedData> FACTORY =
			new SavedData.Factory<>(FoundingFaneSavedData::new, FoundingFaneSavedData::load, null);

	public static final double FANE_RADIUS = FaneFootprint.HEART_RADIUS;

	private final Map<UUID, FaneEntry> fanes = new HashMap<>();

	public FoundingFaneSavedData() {
	}

	public static FoundingFaneSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static FoundingFaneSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		FoundingFaneSavedData data = new FoundingFaneSavedData();
		ListTag list = tag.getList("fanes", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			UUID uuid = entry.getUUID("player");
			BlockPos center = entry.contains("Center", Tag.TAG_LONG)
					? BlockPos.of(entry.getLong("Center"))
					: new BlockPos(entry.getInt("x"), entry.getInt("y"), entry.getInt("z"));
			BlockPos heart = entry.contains("Heart", Tag.TAG_LONG) ? BlockPos.of(entry.getLong("Heart")) : null;
			BlockPos recallPoint = entry.contains("RecallPoint", Tag.TAG_LONG)
					? BlockPos.of(entry.getLong("RecallPoint"))
					: center;
			List<BlockPos> stakes = new ArrayList<>();
			ListTag stakeTag = entry.getList("Stakes", Tag.TAG_LONG);
			for (int s = 0; s < stakeTag.size(); s++) {
				if (stakeTag.get(s) instanceof LongTag stakePos) {
					stakes.add(BlockPos.of(stakePos.getAsLong()));
				}
			}
			data.fanes.put(uuid, new FaneEntry(heart, stakes, center, recallPoint));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, FaneEntry> entry : fanes.entrySet()) {
			CompoundTag e = new CompoundTag();
			FaneEntry value = entry.getValue();
			e.putUUID("player", entry.getKey());
			e.putLong("Center", value.legacyCenter().asLong());
			if (value.heart() != null) {
				e.putLong("Heart", value.heart().asLong());
			}
			ListTag stakes = new ListTag();
			for (BlockPos stake : value.stakes()) {
				stakes.add(LongTag.valueOf(stake.asLong()));
			}
			e.put("Stakes", stakes);
			e.putLong("RecallPoint", value.recallPoint().asLong());
			list.add(e);
		}
		tag.put("fanes", list);
		return tag;
	}

	public void consecrate(UUID playerUUID, BlockPos center) {
		consecrateHeart(playerUUID, center);
	}

	public void consecrateHeart(UUID playerUUID, BlockPos heart) {
		fanes.put(playerUUID, new FaneEntry(heart, List.of(), heart, heart));
		setDirty();
	}

	public void remove(UUID playerUUID) {
		fanes.remove(playerUUID);
		setDirty();
	}

	public boolean hasFane(UUID playerUUID) {
		return fanes.containsKey(playerUUID) && fanes.get(playerUUID).heart() != null;
	}

	public BlockPos getFaneCenter(UUID playerUUID) {
		FaneEntry entry = fanes.get(playerUUID);
		return entry != null ? entry.center() : null;
	}

	public BlockPos getHeart(UUID playerUUID) {
		FaneEntry entry = fanes.get(playerUUID);
		return entry != null ? entry.heart() : null;
	}

	public BlockPos getRecallPoint(UUID playerUUID) {
		FaneEntry entry = fanes.get(playerUUID);
		return entry != null ? entry.recallPoint() : null;
	}

	public boolean setRecallPoint(UUID playerUUID, BlockPos newRecallPoint) {
		FaneEntry entry = fanes.get(playerUUID);
		if (entry == null || !isWithinFane(playerUUID, newRecallPoint)) {
			return false;
		}
		fanes.put(playerUUID, entry.withRecallPoint(newRecallPoint));
		setDirty();
		return true;
	}

	public boolean isWithinFane(UUID playerUUID, BlockPos pos) {
		FaneEntry entry = fanes.get(playerUUID);
		return entry != null && entry.footprint().contains(pos);
	}

	public double effectStrength(UUID playerUUID, BlockPos pos) {
		FaneEntry entry = fanes.get(playerUUID);
		return entry != null ? entry.footprint().effectStrength(pos) : 0.0D;
	}

	public boolean canPlaceBloodwell(BlockPos pos) {
		for (FaneEntry entry : fanes.values()) {
			if (entry.heart() != null && entry.footprint().contains(pos)) {
				return false;
			}
		}
		return true;
	}

	public boolean addStake(UUID playerUUID, BlockPos pos, int budget) {
		FaneEntry entry = fanes.get(playerUUID);
		if (entry == null || !entry.footprint().canAddStake(pos, budget)) {
			return false;
		}
		fanes.put(playerUUID, entry.withStake(pos));
		setDirty();
		return true;
	}

	public boolean canAddStake(UUID playerUUID, BlockPos pos, int budget) {
		FaneEntry entry = fanes.get(playerUUID);
		return entry != null && entry.footprint().canAddStake(pos, budget);
	}

	public void removeStake(UUID playerUUID, BlockPos pos) {
		FaneEntry entry = fanes.get(playerUUID);
		if (entry != null && entry.stakes().contains(pos)) {
			fanes.put(playerUUID, entry.withoutStake(pos));
			setDirty();
		}
	}

	public void removeHeart(UUID playerUUID, BlockPos pos) {
		FaneEntry entry = fanes.get(playerUUID);
		if (entry != null && pos.equals(entry.heart())) {
			fanes.put(playerUUID, entry.withoutHeart());
			setDirty();
		}
	}

	public List<BlockPos> removeHeartAndGetStakes(UUID playerUUID, BlockPos pos) {
		FaneEntry entry = fanes.get(playerUUID);
		if (entry != null && pos.equals(entry.heart())) {
			List<BlockPos> removedStakes = List.copyOf(entry.stakes());
			fanes.put(playerUUID, entry.withoutHeart());
			setDirty();
			return removedStakes;
		}
		return List.of();
	}

	public List<BlockPos> removeStakesAndGet(UUID playerUUID) {
		FaneEntry entry = fanes.get(playerUUID);
		if (entry != null && !entry.stakes().isEmpty()) {
			List<BlockPos> removedStakes = List.copyOf(entry.stakes());
			fanes.put(playerUUID, new FaneEntry(entry.heart(), List.of(), entry.legacyCenter(), entry.recallPoint()));
			setDirty();
			return removedStakes;
		}
		return List.of();
	}

	public UUID findOwnerForStake(BlockPos pos) {
		for (Map.Entry<UUID, FaneEntry> entry : fanes.entrySet()) {
			if (entry.getValue().stakes().contains(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public UUID findOwnerForHeart(BlockPos pos) {
		for (Map.Entry<UUID, FaneEntry> entry : fanes.entrySet()) {
			if (pos.equals(entry.getValue().heart())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public UUID findOwnerContaining(BlockPos pos) {
		for (Map.Entry<UUID, FaneEntry> entry : fanes.entrySet()) {
			if (entry.getValue().footprint().contains(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Map<UUID, BlockPos> getAllFanes() {
		Map<UUID, BlockPos> centers = new LinkedHashMap<>();
		for (Map.Entry<UUID, FaneEntry> entry : fanes.entrySet()) {
			if (entry.getValue().heart() != null) {
				centers.put(entry.getKey(), entry.getValue().center());
			}
		}
		return centers;
	}

	public Map<UUID, FaneFootprint> getAllFootprints() {
		Map<UUID, FaneFootprint> footprints = new LinkedHashMap<>();
		for (Map.Entry<UUID, FaneEntry> entry : fanes.entrySet()) {
			if (entry.getValue().heart() != null) {
				footprints.put(entry.getKey(), entry.getValue().footprint());
			}
		}
		return footprints;
	}

	private record FaneEntry(BlockPos heart, List<BlockPos> stakes, BlockPos legacyCenter, BlockPos recallPoint) {
		private BlockPos center() {
			return heart != null ? heart : legacyCenter;
		}

		private FaneFootprint footprint() {
			return new FaneFootprint(heart, stakes, legacyCenter);
		}

		private FaneEntry withRecallPoint(BlockPos recallPoint) {
			return new FaneEntry(heart, stakes, legacyCenter, recallPoint);
		}

		private FaneEntry withStake(BlockPos pos) {
			FaneFootprint footprint = footprint().withStake(pos);
			return new FaneEntry(footprint.heart(), footprint.stakes(), legacyCenter, recallPoint);
		}

		private FaneEntry withoutStake(BlockPos pos) {
			FaneFootprint footprint = footprint().withoutStake(pos);
			return new FaneEntry(footprint.heart(), footprint.stakes(), legacyCenter, recallPoint);
		}

		private FaneEntry withoutHeart() {
			return new FaneEntry(null, List.of(), legacyCenter, legacyCenter);
		}
	}
}
