package com.vincenthuto.hemomancy.common.capability.player.harbinger.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinition;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;

public class KnownSummons implements IKnownSummons, INBTSerializable<CompoundTag> {
	private List<String> knownSummonNames = new ArrayList<>();

	@Override
	public List<String> getKnownSummonNames() {
		return knownSummonNames;
	}

	@Override
	public void setKnownSummonNames(List<String> names) {
		knownSummonNames = new ArrayList<>();
		if (names == null) {
			return;
		}
		for (String name : names) {
			if (PuppeteerSummonDefinitions.byName(name).isPresent() && !knownSummonNames.contains(name)) {
				knownSummonNames.add(name);
			}
		}
	}

	@Override
	public boolean isKnown(PuppeteerSummonDefinition definition) {
		return definition != null && knownSummonNames.contains(definition.name());
	}

	@Override
	public boolean learn(PuppeteerSummonDefinition definition) {
		if (definition == null || knownSummonNames.contains(definition.name())) {
			return false;
		}
		knownSummonNames.add(definition.name());
		return true;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		ListTag knownTag = new ListTag();
		for (String name : knownSummonNames) {
			knownTag.add(StringTag.valueOf(name));
		}
		tag.put("known", knownTag);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		List<String> names = new ArrayList<>();
		ListTag knownTag = tag.getList("known", Tag.TAG_STRING);
		for (int i = 0; i < knownTag.size(); i++) {
			names.add(knownTag.getString(i));
		}
		setKnownSummonNames(names);
	}
}
