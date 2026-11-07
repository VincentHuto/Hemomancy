package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening.Delivery;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Hemorath's eight punish windows (design section 6). Gorged and Strained are the overload road's doors (held
 * Projection into his chest feeds the meter at x1.5 and x2), Planted, Haloless, Clotted, Wrung and Broken grasp the
 * wear-down road's, and the Tithe is paid along his chain. Every hit counts; none is capped.
 */
public final class HemorathOpenings {
	private static final Set<Delivery> ANY = EnumSet.allOf(Delivery.class);

	public static final SaintOpening.Spec GORGED = spec(1, "gorged", 50, 1.25F);
	public static final SaintOpening.Spec STRAINED = spec(2, "strained", 40, 1.0F);
	public static final SaintOpening.Spec PLANTED = spec(3, "planted", 24, 1.15F);
	public static final SaintOpening.Spec HALOLESS = spec(4, "haloless", 56, 1.30F);
	public static final SaintOpening.Spec CLOTTED = spec(5, "clotted", 30, 1.20F);
	public static final SaintOpening.Spec WRUNG = spec(6, "wrung", 40, 1.40F);
	public static final SaintOpening.Spec BROKEN_GRASP = spec(7, "broken_grasp", 30, 1.20F);
	public static final SaintOpening.Spec TITHE = spec(8, "tithe", 36, 1.0F);

	public static final List<SaintOpening.Spec> ALL = List.of(GORGED, STRAINED, PLANTED, HALOLESS, CLOTTED, WRUNG,
			BROKEN_GRASP, TITHE);

	/** Held Projection into his chest converts at these rates (resolution B-4: a multiplier, not a faster drain). */
	public static final float GORGED_FEED = 1.5F;
	public static final float STRAINED_FEED = 2.0F;
	public static final float THIRST_FEED = 1.5F;
	public static final float TITHE_FEED = 1.0F;
	/** Blood paid a tick into an opening or the Tithe, never more than the item moves. */
	public static final float FEED_RATE = 20.0F;

	private HemorathOpenings() {
	}

	@Nullable
	public static SaintOpening.Spec byId(int id) {
		for (SaintOpening.Spec spec : ALL) if (spec.id() == id) return spec;
		return null;
	}

	/** The meter a tick of held Projection feeds in this opening, or 0 where the ribs are shut. */
	public static float feed(int openingId, boolean thirst) {
		if (openingId == GORGED.id()) return GORGED_FEED;
		if (openingId == STRAINED.id()) return STRAINED_FEED;
		return thirst ? THIRST_FEED : 0.0F;
	}

	/** Gorged and Strained light his vessels to full while open. */
	public static boolean feeds(int openingId) {
		return openingId == GORGED.id() || openingId == STRAINED.id();
	}

	private static SaintOpening.Spec spec(int id, String key, int ticks, float damageTaken) {
		return new SaintOpening.Spec(id, key, ticks, damageTaken, 0, 0, ANY);
	}
}
