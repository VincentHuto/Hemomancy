package com.vincenthuto.hemomancy.common.capability.player.visceral;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class VisceralOrgans implements IVisceralOrgans {

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
}
