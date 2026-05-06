package com.vincenthuto.hemomancy.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Persists remaining purification capacity for placed white humor source blocks.
 */
public class WhiteHumorPoolSavedData extends SavedData {

	public static final int SOURCE_PURIFICATION_CAPACITY = 32;

	private static final String DATA_NAME = "hemomancy_white_humor_pools";
	private static final SavedData.Factory<WhiteHumorPoolSavedData> FACTORY =
			new SavedData.Factory<>(WhiteHumorPoolSavedData::new, WhiteHumorPoolSavedData::load, null);

	private final Map<BlockPos, Integer> sourceCharges = new HashMap<>();

	public WhiteHumorPoolSavedData() {}

	public static WhiteHumorPoolSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static WhiteHumorPoolSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		WhiteHumorPoolSavedData data = new WhiteHumorPoolSavedData();
		if (tag.contains("sources", Tag.TAG_LIST)) {
			ListTag list = tag.getList("sources", Tag.TAG_COMPOUND);
			for (int i = 0; i < list.size(); i++) {
				CompoundTag entry = list.getCompound(i);
				data.sourceCharges.put(BlockPos.of(entry.getLong("pos")), entry.getInt("charges"));
			}
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (Map.Entry<BlockPos, Integer> entry : sourceCharges.entrySet()) {
			CompoundTag source = new CompoundTag();
			source.putLong("pos", entry.getKey().asLong());
			source.putInt("charges", entry.getValue());
			list.add(source);
		}
		tag.put("sources", list);
		return tag;
	}

	public void resetSource(BlockPos pos) {
		sourceCharges.put(pos.immutable(), SOURCE_PURIFICATION_CAPACITY);
		setDirty();
	}

	public int getRemainingCharges(BlockPos pos) {
		return sourceCharges.getOrDefault(pos, SOURCE_PURIFICATION_CAPACITY);
	}

	public int consumeCharges(BlockPos pos, int requested) {
		if (requested <= 0) return 0;

		int remaining = getRemainingCharges(pos);
		int consumed = Math.min(remaining, requested);
		int newRemaining = remaining - consumed;
		if (newRemaining <= 0) {
			sourceCharges.remove(pos);
		} else {
			sourceCharges.put(pos.immutable(), newRemaining);
		}
		setDirty();
		return consumed;
	}

	public void clearSource(BlockPos pos) {
		if (sourceCharges.remove(pos) != null) {
			setDirty();
		}
	}
}
