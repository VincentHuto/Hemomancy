package com.vincenthuto.hemomancy.common.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** One encounter record per generated Vagrant Mind. */
public final class VagrantMindEncounterData extends SavedData {
	private static final Factory<VagrantMindEncounterData> FACTORY =
			new Factory<>(VagrantMindEncounterData::new, VagrantMindEncounterData::load, null);
	private final Map<Long, Encounter> encounters = new HashMap<>();

	public static VagrantMindEncounterData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(FACTORY, "hemomancy_vagrant_mind_encounters");
	}

	public Encounter encounter(BlockPos origin) {
		return encounters.get(origin.asLong());
	}

	public void active(BlockPos origin, UUID entity, BlockPos position) {
		encounters.put(origin.asLong(), new Encounter(entity, position.immutable(), false));
		setDirty();
	}

	public void position(BlockPos origin, UUID entity, BlockPos position) {
		Encounter old = encounter(origin);
		if (old == null || old.defeated || !entity.equals(old.entity) || old.position.equals(position)) return;
		encounters.put(origin.asLong(), new Encounter(entity, position.immutable(), false));
		setDirty();
	}

	public void defeated(BlockPos origin, UUID entity) {
		Encounter old = encounter(origin);
		if (old == null || old.defeated || !entity.equals(old.entity)) return;
		encounters.put(origin.asLong(), new Encounter(null, old.position, true));
		setDirty();
	}

	public record Encounter(UUID entity, BlockPos position, boolean defeated) {}

	private static VagrantMindEncounterData load(CompoundTag tag, HolderLookup.Provider lookup) {
		VagrantMindEncounterData data = new VagrantMindEncounterData();
		ListTag list = tag.getList("Encounters", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			UUID entity = entry.hasUUID("Entity") ? entry.getUUID("Entity") : null;
			data.encounters.put(entry.getLong("Origin"), new Encounter(entity,
					BlockPos.of(entry.getLong("Position")), entry.getBoolean("Defeated")));
		}
		return data;
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookup) {
		ListTag list = new ListTag();
		encounters.forEach((origin, encounter) -> {
			CompoundTag entry = new CompoundTag();
			entry.putLong("Origin", origin);
			entry.putLong("Position", encounter.position.asLong());
			entry.putBoolean("Defeated", encounter.defeated);
			if (encounter.entity != null) entry.putUUID("Entity", encounter.entity);
			list.add(entry);
		});
		tag.put("Encounters", list);
		return tag;
	}
}
