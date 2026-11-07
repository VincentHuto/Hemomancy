package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;

import java.util.EnumSet;

/**
 * Velorum's three openings (design section 6) and the rules that open, stretch and chain them: the Guttering Lantern
 * after every Long Night, the Overheld Invocation when an invocation is struck three times or for 4% of his maximum, and
 * the Bow at a phase floor, after 6% dealt inside another window, or when the Endless Hour breaks. Only one is open at a
 * time; a window runs into the Bow and nowhere else. JVM-tested.
 */
public final class VelorumOpeningRules {
	public static final int GUTTER_ID = 1;
	public static final int OVERHELD_ID = 2;
	public static final int BOW_ID = 3;

	private static final EnumSet<SaintOpening.Delivery> ANY = EnumSet.allOf(SaintOpening.Delivery.class);

	public static final SaintOpening.Spec GUTTER = new SaintOpening.Spec(GUTTER_ID, "guttering_lantern", 60, 1.0F, 0, 0, ANY);
	public static final SaintOpening.Spec OVERHELD = new SaintOpening.Spec(OVERHELD_ID, "overheld_invocation", 50, 1.0F, 0,
			0, ANY);
	public static final float BOW_TAKEN = 1.5F;
	public static final int BOW_TICKS = 70;
	public static final int BOW_BROKEN_HOUR_TICKS = 100;
	/** The Repose deed's Bow: 40 ticks at x2.625, so it admits what the 70-tick x1.5 Bow does (D-4). */
	public static final int BOW_REPOSE_TICKS = 40;
	public static final float BOW_REPOSE_TAKEN = BOW_TAKEN * BOW_TICKS / BOW_REPOSE_TICKS;
	public static final SaintOpening.Spec BOW = new SaintOpening.Spec(BOW_ID, "bow", BOW_TICKS, BOW_TAKEN, 0, 0, ANY);
	public static final SaintOpening.Spec BOW_REPOSE = new SaintOpening.Spec(BOW_ID, "bow", BOW_REPOSE_TICKS,
			BOW_REPOSE_TAKEN, 0, 0, ANY);

	public static final int[] GUTTER_TICKS = {60, 50, 40};
	public static final int OVERHELD_HITS = 3;
	public static final float OVERHELD_FRACTION = 0.04F;
	public static final float CHAIN_FRACTION = 0.06F;
	public static final float HOUR_BREAK_FRACTION = 0.08F;
	/** Searing blows count double toward breaking the Hour. */
	public static final float HOUR_SEARING_WEIGHT = 2.0F;

	/** Held Projection into him in the Gutter: blood a tick, the guard-free damage it deals, and its extension. */
	public static final float WARMING_RATE = 4.0F;
	public static final float WARMING_DAMAGE = 1.0F;
	public static final int FED_TICKS_PER_EXTENSION = 4;
	public static final int PROJECTION_EXTENSION_CAP = 20;
	public static final int SEARING_EXTENSION = 10;
	/** Held Absorption on the Bow's seam: damage a tick and blood the absorber gains a tick. */
	public static final float SEAM_DAMAGE = 1.5F;
	public static final float SEAM_CREDIT = 2.0F;

	private VelorumOpeningRules() {
	}

	/** The Guttering Lantern's length in a phase (60, 50, 40), halved after an unbroken Endless Hour. */
	public static int gutterTicks(int phase, boolean afterHour) {
		int ticks = GUTTER_TICKS[Math.max(0, Math.min(GUTTER_TICKS.length - 1, phase - 1))];
		return afterHour ? ticks / 2 : ticks;
	}

	/** The Guttering Lantern for {@code ticks}: the kit opens a recovery opening for its spec's own length. */
	public static SaintOpening.Spec gutter(int ticks) {
		return new SaintOpening.Spec(GUTTER_ID, GUTTER.key(), Math.max(1, ticks), GUTTER.damageTaken(), 0, 0, ANY);
	}

	/** The Bow for {@code ticks} at its factor (the Repose deed's own, else x1.5). */
	public static SaintOpening.Spec bow(int ticks, boolean repose) {
		SaintOpening.Spec base = bow(repose);
		return new SaintOpening.Spec(BOW_ID, base.key(), Math.max(1, ticks), base.damageTaken(), 0, 0, ANY);
	}

	/** The Bow he makes: the Repose deed's short one, else 100 after a broken Hour, else 70. */
	public static SaintOpening.Spec bow(boolean repose) {
		return repose ? BOW_REPOSE : BOW;
	}

	public static int bowTicks(boolean repose, boolean brokenHour) {
		if (repose) return BOW_REPOSE_TICKS;
		return brokenHour ? BOW_BROKEN_HOUR_TICKS : BOW_TICKS;
	}

	/** Whether an invocation's hold has been struck enough to overhold it. */
	public static boolean overheld(int hits, float damage, float maxHealth) {
		return hits >= OVERHELD_HITS || damage >= OVERHELD_FRACTION * maxHealth;
	}

	/** Whether a Gutter or Overheld window has been punished enough to run into the Bow. */
	public static boolean chainsToBow(int openingId, float damage, float maxHealth) {
		return (openingId == GUTTER_ID || openingId == OVERHELD_ID) && damage >= CHAIN_FRACTION * maxHealth;
	}

	/** Whether the Hour breaks: 8% of his maximum in its hold, Searing blows counted double. */
	public static boolean hourBreaks(float weightedDamage, float maxHealth) {
		return weightedDamage >= HOUR_BREAK_FRACTION * maxHealth;
	}

	public static float hourWeight(float damage, boolean searing) {
		return searing ? damage * HOUR_SEARING_WEIGHT : damage;
	}

	/** Ticks a held Projection adds to the Gutter after {@code fedTicks} fed ticks in all. */
	public static int projectionExtension(int fedTicks) {
		return Math.min(PROJECTION_EXTENSION_CAP, Math.max(0, fedTicks) / FED_TICKS_PER_EXTENSION);
	}

	/** Whether the opening is the Bow. */
	public static boolean isBow(int openingId) {
		return openingId == BOW_ID;
	}
}
