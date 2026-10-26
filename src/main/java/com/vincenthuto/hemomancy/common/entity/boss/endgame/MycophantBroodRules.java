package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * Her brood: nursing a fungling into a grown brood, the brood tumors the Spore Mortar plants, and how long a death
 * makes her grieve. Tumors stand only in owned fights, so a summoned Mycophant never changes the world.
 */
public final class MycophantBroodRules {
	/** She reaches for a living brood fungling no further than this many blocks away. */
	public static final double NURSE_RANGE = 14.0;
	/** Ticks of feeding before the fungling becomes grown. */
	public static final int NURSE_TICKS = 80;
	/** Damage to her during one feeding that breaks it off. */
	public static final float NURSE_BREAK_DAMAGE = 8.0F;
	/** Most grown brood alive at once. */
	public static final int GROWN_CAP = 2;
	/** A grown brood is drawn and sized at this multiple of a fungling. */
	public static final float GROWN_SCALE = 1.8F;
	public static final double GROWN_HEALTH = 30.0;
	public static final double GROWN_BITE = 8.0;
	/** Her grief stagger when a grown brood dies. */
	public static final int GROWN_GRIEF_TICKS = 50;
	/** Most brood tumors standing at once. */
	public static final int TUMOR_CAP = 6;
	/** A tumor hatches a fungling this many ticks after it is planted (20 seconds). */
	public static final int TUMOR_HATCH_TICKS = 400;
	/** Most living brood funglings, grown included; a tumor that would pass it breaks without hatching. */
	public static final int BROOD_CAP = 4;

	private MycophantBroodRules() {
	}

	/**
	 * She may nurse when at least one of her living brood (grown included in {@code livingBrood}) is not yet grown and
	 * fewer than {@link #GROWN_CAP} are grown.
	 */
	public static boolean mayNurse(int livingBrood, int grown) {
		return livingBrood > grown && grown < GROWN_CAP;
	}

	/** A mortar egg lands as a tumor only in an owned fight and only while fewer than {@link #TUMOR_CAP} stand. */
	public static boolean mayPlantTumor(boolean owned, int standing) {
		return owned && standing < TUMOR_CAP;
	}

	public static boolean hatchDue(int plantedTick, int now) {
		return now - plantedTick >= TUMOR_HATCH_TICKS;
	}

	/** Grief stagger ticks for a brood death: longer for a grown brood, the ordinary grief otherwise. */
	public static int griefTicks(boolean grown) {
		return grown ? GROWN_GRIEF_TICKS : MycophantControllerRules.griefTicks();
	}
}
