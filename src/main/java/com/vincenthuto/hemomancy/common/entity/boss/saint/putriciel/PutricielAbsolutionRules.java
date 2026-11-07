package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

/**
 * The five absolutions (design sections 6 and 10, resolution D-4). An Offering takes one act and closes: a hit that
 * swings at least 4 (before armour), or 100 blood of held Blood Projection poured into the palm, at no more than the
 * held item's rate a tick (5 at most). Nothing absolves outside an Offering. Under the Litany deed a glyph over the palm
 * names the act it will take. Pure, so it is JVM-tested.
 */
public final class PutricielAbsolutionRules {
	public static final int NEEDED = 5;
	public static final float MIN_HIT = 4.0F;
	/** Blood a projection must pour into the palm, and the most it takes a tick. */
	public static final double PROJECT_BLOOD = 100.0D;
	public static final double PROJECT_MAX_RATE = 5.0D;
	public static final int OFFERING_TICKS = 50;
	/** After the Full Purge, and under the Litany deed (the glyph costs reading time, not absolutions). */
	public static final int LONG_OFFERING_TICKS = 60;
	/** The recoil "beneath an accepted offering". */
	public static final int RECOIL_TICKS = 14;
	/** Colonisation the projector's offered blood burns off. */
	public static final float PROJECTOR_CLEANSE = 10.0F;
	/** How far the palm takes a projection: the projection's own reach is passed by the router; this caps the cone. */
	public static final double PALM_CONE = 50.0D;

	/** What a player offers. */
	public enum Act { STRIKE, POUR }

	/** The Litany deed's glyph over the palm. NONE: any act. */
	public enum Glyph {
		NONE, STRIKE, POUR;

		public boolean accepts(Act act) {
			return this == NONE || (this == STRIKE) == (act == Act.STRIKE);
		}

		public static Glyph byId(int id) {
			Glyph[] all = values();
			return id >= 0 && id < all.length ? all[id] : NONE;
		}
	}

	private PutricielAbsolutionRules() {
	}

	public static int offeringTicks(boolean litany, boolean afterPurge) {
		return litany || afterPurge ? LONG_OFFERING_TICKS : OFFERING_TICKS;
	}

	public static boolean hitQualifies(float rawDamage) {
		return rawDamage >= MIN_HIT;
	}

	/** Whether this Offering is the fifth's: the Full Purge runs before it, once an interval. */
	public static boolean fullPurgeDue(int absolutions, boolean purgedThisInterval) {
		return absolutions == NEEDED - 1 && !purgedThisInterval;
	}

	public static boolean done(int absolutions) {
		return absolutions >= NEEDED;
	}

	public static float progress(int absolutions) {
		return Math.max(0.0F, Math.min(1.0F, absolutions / (float) NEEDED));
	}

	/** Blood the palm takes this tick from a projection at {@code rate}, given what it already holds. */
	public static double pourStep(double rate, double poured) {
		return Math.max(0.0D, Math.min(Math.min(rate, PROJECT_MAX_RATE), PROJECT_BLOOD - poured));
	}

	public static boolean pourComplete(double poured) {
		return poured >= PROJECT_BLOOD - 1.0E-6D;
	}

	/**
	 * The next glyph: random, but never the same three times running ({@code last} and {@code beforeLast} are the
	 * two before it). {@code roll} is any int; its parity picks.
	 */
	public static Glyph nextGlyph(Glyph last, Glyph beforeLast, int roll) {
		if (last != Glyph.NONE && last == beforeLast) return last == Glyph.STRIKE ? Glyph.POUR : Glyph.STRIKE;
		return Math.floorMod(roll, 2) == 0 ? Glyph.STRIKE : Glyph.POUR;
	}
}
