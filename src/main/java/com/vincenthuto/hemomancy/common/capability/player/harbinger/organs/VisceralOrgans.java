package com.vincenthuto.hemomancy.common.capability.player.harbinger.organs;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class VisceralOrgans implements IVisceralOrgans, INBTSerializable<CompoundTag> {

	private final EnumMap<EnumOrgan, Integer> organLevels = new EnumMap<>(EnumOrgan.class);

	public VisceralOrgans() {
		for (EnumOrgan organ : EnumOrgan.values()) {
			organLevels.put(organ, 0);
		}
	}

	@Override
	public int getOrganLevel(EnumOrgan organ) {
		return organLevels.getOrDefault(organ, 0);
	}

	@Override
	public void setOrganLevel(EnumOrgan organ, int level) {
		organLevels.put(organ, Math.max(0, Math.min(3, level)));
	}

	@Override
	public boolean isExtracted(EnumOrgan organ) {
		return getOrganLevel(organ) >= 1;
	}

	@Override
	public boolean isHeartless() {
		return isExtracted(EnumOrgan.HEART);
	}

	@Override
	public Map<EnumOrgan, Integer> getAllOrgans() {
		return Collections.unmodifiableMap(organLevels);
	}

	@Override
	public void resetAll() {
		for (EnumOrgan organ : EnumOrgan.values()) {
			organLevels.put(organ, 0);
		}
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		for (EnumOrgan organ : EnumOrgan.values()) {
			tag.putInt(organ.name().toLowerCase(), getOrganLevel(organ));
		}
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		for (EnumOrgan organ : EnumOrgan.values()) {
			String key = organ.name().toLowerCase();
			if (nbt.contains(key)) {
				setOrganLevel(organ, nbt.getInt(key));
			}
		}
	}
}
