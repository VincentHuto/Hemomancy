package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;

/**
 * What each skipped Chapel lends a forced Putriciel (design section 10, resolution D-4), read from the kit's deed
 * mask so the client sees the same. Beholding lends nothing here. Pure, so it is JVM-tested.
 * <ul>
 *   <li><b>Litany</b>, <i>The Unread Rite</i>: a glyph over the palm names the act it takes; Offerings stay 60 ticks.</li>
 *   <li><b>Downfall</b>, <i>The Faithful Rise</i>: Root Snare and the Bloom's first pulse raise a bog body.</li>
 *   <li><b>Repose</b>, <i>It Never Pauses</i>: no Embering (25% Kindling instead), rot recoveries a quarter shorter,
 *       only a Flammeus hit breaks a Communion.</li>
 * </ul>
 */
public final class PutricielDeeds {
	/** The Repose deed's recovery cut: a quarter, rounded down. */
	public static final float REPOSE_RECOVERY_CUT = 0.25F;

	private PutricielDeeds() {
	}

	public static boolean litany(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.LITANY);
	}

	public static boolean downfall(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.DOWNFALL);
	}

	public static boolean repose(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.REPOSE);
	}

	/** A rot move's recovery under the mask; an opening that feeds an Offering is not cut. */
	public static int recovery(int base, int mask, boolean rot) {
		if (!rot || !repose(mask)) return base;
		return base - (int) Math.floor(base * REPOSE_RECOVERY_CUT);
	}

	/** Whether a hit may break a Mould Communion: any qualifying hit, or under Repose only a Flammeus one. */
	public static boolean communionBreaks(int mask, boolean flammeus) {
		return flammeus || !repose(mask);
	}
}
