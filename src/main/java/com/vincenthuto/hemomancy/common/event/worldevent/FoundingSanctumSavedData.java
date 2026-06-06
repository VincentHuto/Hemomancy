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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FoundingSanctumSavedData extends SavedData {
	private static final String DATA_NAME = "hemomancy_founding_sanctums";
	private static final SavedData.Factory<FoundingSanctumSavedData> FACTORY =
			new SavedData.Factory<>(FoundingSanctumSavedData::new, FoundingSanctumSavedData::load, null);

	public static final double SANCTUM_RADIUS = SanctumFootprint.HEART_RADIUS;

	private final Map<UUID, SanctumEntry> sanctums = new HashMap<>();

	public FoundingSanctumSavedData() {
	}

	public static FoundingSanctumSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static FoundingSanctumSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		FoundingSanctumSavedData data = new FoundingSanctumSavedData();
		ListTag list = tag.getList("sanctums", Tag.TAG_COMPOUND);
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
			data.sanctums.put(uuid, new SanctumEntry(heart, stakes, center, recallPoint));
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			CompoundTag e = new CompoundTag();
			SanctumEntry value = entry.getValue();
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
		tag.put("sanctums", list);
		return tag;
	}

	public void consecrate(UUID playerUUID, BlockPos center) {
		consecrateHeart(playerUUID, center);
	}

	public void consecrateHeart(UUID playerUUID, BlockPos heart) {
		sanctums.put(playerUUID, new SanctumEntry(heart, List.of(), heart, heart));
		setDirty();
	}

	public void remove(UUID playerUUID) {
		sanctums.remove(playerUUID);
		setDirty();
	}

	public boolean hasSanctum(UUID playerUUID) {
		return sanctums.containsKey(playerUUID) && sanctums.get(playerUUID).heart() != null;
	}

	public BlockPos getSanctumCenter(UUID playerUUID) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null ? entry.center() : null;
	}

	public BlockPos getHeart(UUID playerUUID) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null ? entry.heart() : null;
	}

	public BlockPos getRecallPoint(UUID playerUUID) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null ? entry.recallPoint() : null;
	}

	public boolean setRecallPoint(UUID playerUUID, BlockPos newRecallPoint) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry == null || !isWithinSanctum(playerUUID, newRecallPoint)) {
			return false;
		}
		sanctums.put(playerUUID, entry.withRecallPoint(newRecallPoint));
		setDirty();
		return true;
	}

	public boolean isWithinSanctum(UUID playerUUID, BlockPos pos) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null && entry.footprint().contains(pos);
	}

	public double effectStrength(UUID playerUUID, BlockPos pos) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null ? entry.footprint().effectStrength(pos) : 0.0D;
	}

	public boolean canPlaceBloodwell(BlockPos pos) {
		for (SanctumEntry entry : sanctums.values()) {
			if (entry.heart() != null && entry.footprint().contains(pos)) {
				return false;
			}
		}
		return true;
	}

	public boolean addStake(UUID playerUUID, BlockPos pos, int budget) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry == null || !entry.footprint().canAddStake(pos, budget)) {
			return false;
		}
		sanctums.put(playerUUID, entry.withStake(pos));
		setDirty();
		return true;
	}

	public boolean canAddStake(UUID playerUUID, BlockPos pos, int budget) {
		SanctumEntry entry = sanctums.get(playerUUID);
		return entry != null && entry.footprint().canAddStake(pos, budget);
	}

	public void removeStake(UUID playerUUID, BlockPos pos) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry != null && entry.stakes().contains(pos)) {
			sanctums.put(playerUUID, entry.withoutStake(pos));
			setDirty();
		}
	}

	public void removeHeart(UUID playerUUID, BlockPos pos) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry != null && pos.equals(entry.heart())) {
			sanctums.put(playerUUID, entry.withoutHeart());
			setDirty();
		}
	}

	public List<BlockPos> removeHeartAndGetStakes(UUID playerUUID, BlockPos pos) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry != null && pos.equals(entry.heart())) {
			List<BlockPos> removedStakes = List.copyOf(entry.stakes());
			sanctums.put(playerUUID, entry.withoutHeart());
			setDirty();
			return removedStakes;
		}
		return List.of();
	}

	public List<BlockPos> removeStakesAndGet(UUID playerUUID) {
		SanctumEntry entry = sanctums.get(playerUUID);
		if (entry != null && !entry.stakes().isEmpty()) {
			List<BlockPos> removedStakes = List.copyOf(entry.stakes());
			sanctums.put(playerUUID, new SanctumEntry(entry.heart(), List.of(), entry.legacyCenter(), entry.recallPoint()));
			setDirty();
			return removedStakes;
		}
		return List.of();
	}

	public UUID findOwnerForStake(BlockPos pos) {
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			if (entry.getValue().stakes().contains(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public UUID findOwnerContaining(BlockPos pos) {
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			if (entry.getValue().footprint().contains(pos)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public Map<UUID, BlockPos> getAllSanctums() {
		Map<UUID, BlockPos> centers = new LinkedHashMap<>();
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			if (entry.getValue().heart() != null) {
				centers.put(entry.getKey(), entry.getValue().center());
			}
		}
		return centers;
	}

	public Map<UUID, SanctumFootprint> getAllFootprints() {
		Map<UUID, SanctumFootprint> footprints = new LinkedHashMap<>();
		for (Map.Entry<UUID, SanctumEntry> entry : sanctums.entrySet()) {
			if (entry.getValue().heart() != null) {
				footprints.put(entry.getKey(), entry.getValue().footprint());
			}
		}
		return footprints;
	}

	private record SanctumEntry(BlockPos heart, List<BlockPos> stakes, BlockPos legacyCenter, BlockPos recallPoint) {
		private BlockPos center() {
			return heart != null ? heart : legacyCenter;
		}

		private SanctumFootprint footprint() {
			return new SanctumFootprint(heart, stakes, legacyCenter);
		}

		private SanctumEntry withRecallPoint(BlockPos recallPoint) {
			return new SanctumEntry(heart, stakes, legacyCenter, recallPoint);
		}

		private SanctumEntry withStake(BlockPos pos) {
			SanctumFootprint footprint = footprint().withStake(pos);
			return new SanctumEntry(footprint.heart(), footprint.stakes(), legacyCenter, recallPoint);
		}

		private SanctumEntry withoutStake(BlockPos pos) {
			SanctumFootprint footprint = footprint().withoutStake(pos);
			return new SanctumEntry(footprint.heart(), footprint.stakes(), legacyCenter, recallPoint);
		}

		private SanctumEntry withoutHeart() {
			return new SanctumEntry(null, List.of(), legacyCenter, legacyCenter);
		}
	}
}
