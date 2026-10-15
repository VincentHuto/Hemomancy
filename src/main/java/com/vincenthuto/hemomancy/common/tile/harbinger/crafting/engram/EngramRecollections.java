package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Which memories each Harbinger has woven. A bloodline remembers for its members: once one of them has recalled an
 * engram, the false fragments of that memory show as grey husks to the rest of their covenant.
 */
public final class EngramRecollections extends SavedData {
	private static final String DATA_NAME = "hemomancy_engram_recollections";
	private static final SavedData.Factory<EngramRecollections> FACTORY =
			new SavedData.Factory<>(EngramRecollections::new, EngramRecollections::load, null);

	private final Map<UUID, Set<String>> woven = new HashMap<>();

	public static EngramRecollections get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
	}

	public void record(UUID weaver, String recipe) {
		if (woven.computeIfAbsent(weaver, id -> new HashSet<>()).add(recipe)) setDirty();
	}

	public boolean hasWoven(UUID weaver, String recipe) {
		return woven.getOrDefault(weaver, Set.of()).contains(recipe);
	}

	/** Whether someone else in the weaver's bloodline has already woven this memory. */
	public boolean bloodlineRemembers(ServerLevel level, UUID weaver, String recipe) {
		Bloodline line = BloodlineSavedData.get(level.getServer().overworld()).getBloodlineForPlayer(weaver);
		if (line == null || !line.isValid()) return false;
		for (Map.Entry<UUID, Set<String>> entry : woven.entrySet())
			if (!entry.getKey().equals(weaver) && entry.getValue().contains(recipe) && line.hasMember(entry.getKey())) return true;
		return false;
	}

	public static EngramRecollections load(CompoundTag tag, HolderLookup.Provider provider) {
		EngramRecollections data = new EngramRecollections();
		CompoundTag players = tag.getCompound("woven");
		for (String key : players.getAllKeys()) {
			UUID id;
			try {
				id = UUID.fromString(key);
			} catch (IllegalArgumentException ignored) {
				continue;
			}
			Set<String> recipes = new HashSet<>();
			ListTag list = players.getList(key, Tag.TAG_STRING);
			for (int i = 0; i < list.size(); i++) recipes.add(list.getString(i));
			data.woven.put(id, recipes);
		}
		return data;
	}

	@Override
	@Nonnull
	public CompoundTag save(@Nonnull CompoundTag tag, HolderLookup.Provider provider) {
		CompoundTag players = new CompoundTag();
		for (Map.Entry<UUID, Set<String>> entry : woven.entrySet()) {
			ListTag list = new ListTag();
			entry.getValue().stream().sorted().forEach(recipe -> list.add(StringTag.valueOf(recipe)));
			players.put(entry.getKey().toString(), list);
		}
		tag.put("woven", players);
		return tag;
	}
}
