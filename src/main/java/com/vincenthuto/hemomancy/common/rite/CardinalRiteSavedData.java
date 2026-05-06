package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * World-level SavedData that tracks all active cardinal rites.
 * Persists across server restarts so in-progress rites are not lost.
 */
public class CardinalRiteSavedData extends SavedData {

	private static final String DATA_NAME = Hemomancy.MOD_ID + "_cardinal_rites";
	private static final SavedData.Factory<CardinalRiteSavedData> FACTORY =
			new SavedData.Factory<>(CardinalRiteSavedData::new, CardinalRiteSavedData::load, null);
	private final Map<UUID, ActiveCardinalRite> activeRites = new HashMap<>();

	public CardinalRiteSavedData() {
	}

	public static CardinalRiteSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public static CardinalRiteSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
		CardinalRiteSavedData data = new CardinalRiteSavedData();
		ListTag list = tag.getList("ActiveRites", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			ActiveCardinalRite rite = ActiveCardinalRite.deserialize(list.getCompound(i));
			data.activeRites.put(rite.getPlayerUUID(), rite);
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		for (ActiveCardinalRite rite : activeRites.values()) {
			list.add(rite.serialize());
		}
		tag.put("ActiveRites", list);
		return tag;
	}

	public void startRite(ActiveCardinalRite rite) {
		activeRites.put(rite.getPlayerUUID(), rite);
		setDirty();
	}

	public void removeRite(UUID playerUUID) {
		activeRites.remove(playerUUID);
		setDirty();
	}

	public ActiveCardinalRite getRite(UUID playerUUID) {
		return activeRites.get(playerUUID);
	}

	public boolean hasActiveRite(UUID playerUUID) {
		return activeRites.containsKey(playerUUID);
	}

	public Map<UUID, ActiveCardinalRite> getActiveRites() {
		return activeRites;
	}
}
