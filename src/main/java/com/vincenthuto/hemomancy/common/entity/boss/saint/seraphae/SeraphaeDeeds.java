package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;

/**
 * What each skipped Chapel lends a forced Seraphae (design section 10), read from the kit's synced deed mask so the
 * client draws what the server runs. Unwatched Hands (Beholding) hides the reach's tells; The Extraction (Downfall)
 * adds a move and a fragment; No Rest Given (Repose) shortens every pause. Pure, so it is JVM-tested.
 */
public final class SeraphaeDeeds {
	public static final float RECOVERY_FACTOR = 0.7F;
	public static final int RECOVERY_FLOOR = 10;
	public static final int DECISION_CUT = 4;

	private SeraphaeDeeds() {
	}

	public static boolean unwatchedHands(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.BEHOLDING);
	}

	public static boolean extraction(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.DOWNFALL);
	}

	public static boolean noRest(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.REPOSE);
	}

	/** A recovery under No Rest Given: 30% shorter, never under 10 ticks; one that bears an opening keeps its length. */
	public static int recovery(int mask, int recovery, boolean bearsOpening) {
		if (!noRest(mask) || bearsOpening) return recovery;
		return Math.max(RECOVERY_FLOOR, Math.round(recovery * RECOVERY_FACTOR));
	}

	/** The decision delay between moves, 4 ticks shorter under No Rest Given. */
	public static int decisionDelay(int mask, int base) {
		return noRest(mask) ? Math.max(1, base - DECISION_CUT) : base;
	}

	/** The extra fragment The Extraction sheds at each Fracture. */
	public static int fragmentBonus(int mask) {
		return extraction(mask) ? 1 : 0;
	}
}
