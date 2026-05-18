package com.vincenthuto.hemomancy.common.entity.mob.animal;

public enum ScarletSerpentVariant {
	DEFAULT(0),
	DESERT_RED(1),
	SWAMP_BROWN(2);

	private final int id;

	ScarletSerpentVariant(int id) {
		this.id = id;
	}

	public int id() {
		return this.id;
	}

	public static ScarletSerpentVariant byId(int id) {
		for (ScarletSerpentVariant variant : values()) {
			if (variant.id == id) {
				return variant;
			}
		}
		return DEFAULT;
	}

	public static ScarletSerpentVariant fromBiomePath(String biomePath) {
		if (biomePath == null) {
			return DEFAULT;
		}
		if (biomePath.contains("desert") || biomePath.contains("badlands")) {
			return DESERT_RED;
		}
		if (biomePath.contains("swamp")) {
			return SWAMP_BROWN;
		}
		return DEFAULT;
	}
}
