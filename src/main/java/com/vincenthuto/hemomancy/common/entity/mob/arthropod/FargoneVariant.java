package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.util.RandomSource;

/** Stable visual and lineage roles used by dynasty populations. */
public enum FargoneVariant {
	SCOUT,
	ATTENDANT,
	GUARD,
	ELDER,
	RECLAIMED;

	public static FargoneVariant fromId(int id) {
		FargoneVariant[] values = values();
		return id >= 0 && id < values.length ? values[id] : SCOUT;
	}

	public static FargoneVariant random(RandomSource random) {
		return values()[random.nextInt(values().length)];
	}

	public int id() {
		return ordinal();
	}
}
