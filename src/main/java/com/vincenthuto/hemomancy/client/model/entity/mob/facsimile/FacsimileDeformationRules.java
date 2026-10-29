package com.vincenthuto.hemomancy.client.model.entity.mob.facsimile;

import java.util.UUID;

/**
 * How a facsimile is wrong: a seeded, per-part scale. About two parts in five are swollen or shrunk; the rest keep the
 * shape the hive copied. Pure, so the same body is always wrong the same way.
 */
public final class FacsimileDeformationRules {
	public static final float MIN_SCALE = 0.8f;
	public static final float MAX_SCALE = 1.3f;
	private static final float DEFORM_CHANCE = 0.4f;
	private static final float[] UNCHANGED = {1f, 1f, 1f};

	private FacsimileDeformationRules() {}

	public static long seedOf(UUID uuid) {
		return uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits();
	}

	public static float[] scaleFor(long seed, int partIndex) {
		long state = mix(seed * 0x9E3779B97F4A7C15L + partIndex * 0xBF58476D1CE4E5B9L);
		if (unit(state) >= DEFORM_CHANCE) return UNCHANGED.clone();
		float[] scale = new float[3];
		for (int axis = 0; axis < 3; axis++) {
			state = mix(state + axis + 1);
			scale[axis] = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * unit(state);
		}
		return scale;
	}

	private static long mix(long z) {
		z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
		z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
		return z ^ (z >>> 31);
	}

	private static float unit(long state) {
		return (state >>> 40) / (float) (1L << 24);
	}
}
