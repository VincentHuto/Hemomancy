package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;

import java.util.List;

/**
 * What each skipped Chapel lends a forced awakening, as a mask a move table and the client can read. A proper or
 * inferred-proper awakening lends nothing, whatever was skipped. The stat floor (+30% attack, +15% speed and the
 * Chapels' own bonuses) stays in {@code SaintTrialEntity.setAwakening} and {@link SaintEnrageRules}.
 */
public final class SaintDeeds {
	public static final SaintDeeds NONE = new SaintDeeds(false, 0);
	/** Set in {@link #mask()} for every forced awakening, so a forced fight with no Chapel skipped still reads forced. */
	public static final int FORCED = 1 << 30;

	private final boolean forced;
	private final int mask;

	private SaintDeeds(boolean forced, int mask) {
		this.forced = forced;
		this.mask = mask;
	}

	public static SaintDeeds of(String awakening, List<String> skippedChapels) {
		boolean forced = SaintEnrageRules.enraged(awakening);
		if (!forced) return NONE;
		int mask = FORCED;
		for (String name : skippedChapels) {
			try {
				mask |= bit(ChapelRules.Kind.valueOf(name));
			} catch (IllegalArgumentException unknown) {
				// an unknown Chapel lends nothing
			}
		}
		return new SaintDeeds(true, mask);
	}

	/** The deeds a synced mask describes (the client's view). */
	public static SaintDeeds ofMask(int mask) {
		return (mask & FORCED) == 0 ? NONE : new SaintDeeds(true, mask);
	}

	public boolean forced() {
		return forced;
	}

	public boolean skipped(ChapelRules.Kind kind) {
		return skipped(mask, kind);
	}

	public int mask() {
		return mask;
	}

	public static boolean skipped(int mask, ChapelRules.Kind kind) {
		return (mask & bit(kind)) != 0;
	}

	private static int bit(ChapelRules.Kind kind) {
		return 1 << kind.ordinal();
	}
}
