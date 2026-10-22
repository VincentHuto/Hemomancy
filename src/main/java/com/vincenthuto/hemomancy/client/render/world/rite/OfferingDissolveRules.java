package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Timing for a Working craft's daemon breaking an offering where it sits: the
 * offering is pulled up out of the bowl toward the hovering daemon, swells and
 * shudders harder as it strains, then breaks apart and is gone, all while the
 * daemon still hangs over the brazier. Pure maths so it can be tested without a client.
 */
public final class OfferingDissolveRules {
	/** The tick the offering breaks apart. */
	public static final int BREAK_TICKS = 6;
	/** How far toward the daemon it is pulled before it breaks, as a share of the gap. */
	private static final float PULL = 0.55F;

	private OfferingDissolveRules() {
	}

	/** Whether a break seen at {@code age} is fresh enough to show; later sightings stay silent. */
	public static boolean shattersNow(float age) {
		return age >= BREAK_TICKS && age < BREAK_TICKS + 4;
	}

	/** How far from the bowl toward the daemon the offering has been pulled. */
	public static float lift(float age) {
		float t = HemoEasing.clamp01(age / BREAK_TICKS);
		return PULL * (1.0F - (1.0F - t) * (1.0F - t));
	}

	/** How hard it shudders: barely at first, violently just before it breaks. */
	public static float strain(float age) {
		float t = HemoEasing.clamp01(age / BREAK_TICKS);
		return 0.01F + 0.07F * t * t;
	}

	/** Its size: swelling as it strains, then nothing once it has broken. */
	public static float scale(float age) {
		if (age >= BREAK_TICKS) return 0.0F;
		return 0.42F * (1.0F + 0.25F * HemoEasing.clamp01(age / BREAK_TICKS));
	}
}
