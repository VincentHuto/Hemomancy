package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The pattern the Vein-Mason gave for each lesson slot (first, continuation, D5, D6), stored when he gave it, so a
 * later temper drift does not change what he replaces (PT-14). Copied on death.
 */
public final class IssuedScarLessons implements INBTSerializable<CompoundTag> {
	private final Map<String, ResourceLocation> issued = new LinkedHashMap<>();

	public Optional<ResourceLocation> get(String slot) {
		return Optional.ofNullable(issued.get(slot));
	}

	public void record(String slot, ResourceLocation patternScarId) {
		issued.put(slot, patternScarId);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		issued.forEach((slot, id) -> tag.putString(slot, id.toString()));
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		issued.clear();
		for (String slot : tag.getAllKeys()) {
			ResourceLocation id = ResourceLocation.tryParse(tag.getString(slot));
			if (id != null) issued.put(slot, id);
		}
	}
}
