package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;

import javax.annotation.Nullable;

/**
 * Containment integrity, Seraphae's signature meter and her bar (design section 4): what raises it, what lowers it,
 * and the thresholds the fight turns on. Every gain from striking her runs through an opening; outside one a hit adds
 * nothing. Pure, so it is JVM-tested.
 */
public final class SeraphaeIntegrityRules {
	public static final float MAX = 100.0F;
	public static final float START = 30.0F;
	/** Phase thresholds as fractions of the maximum, and the contained mark. */
	public static final float PHASE_TWO = 0.40F;
	public static final float PHASE_THREE = 0.75F;
	public static final float CONTAINED = 1.0F;
	public static final float[] THRESHOLDS = {PHASE_TWO, PHASE_THREE, CONTAINED};
	public static final int CONTAINED_THRESHOLD = 2;

	public static final float BASE_DECAY = 0.010F;
	public static final float FRAGMENT_DECAY = 0.008F;

	/** Recoil and Seized: per melee or projectile hit, per school cast. */
	public static final float RESTRAINT_HIT = 2.0F;
	public static final float RESTRAINT_CAST = 3.0F;
	/** Condensing and Lapse. */
	public static final float GATHERED_HIT = 3.0F;
	public static final float GATHERED_CAST = 4.0F;
	public static final float STILLING_FACTOR = 1.5F;
	public static final float OWN_SCHOOL_FACTOR = 0.5F;

	/** The Condensing feed: one integrity per this much blood fed into a held anchor, at most CAP per player. */
	public static final float FEED_BLOOD_PER_POINT = 16.0F;
	public static final float FEED_CAP = 10.0F;

	public static final float REBIND = 8.0F;
	public static final float RESERVE_FIRST = 10.0F;
	public static final float RESERVE_AGAIN = 5.0F;
	public static final float FRAGMENT_ABSORBED = 3.0F;
	public static final float FRAGMENT_CAPTURED = 5.0F;
	public static final float FRAGMENT_STRUCK = 1.5F;
	public static final float FRAGMENT_GRAFTED = -2.0F;
	public static final float FRAGMENT_RETURNED = -4.0F;
	public static final float SLIP = -6.0F;
	public static final float BURST = -3.0F;
	/** At 0 the Radiance Flood fires once and integrity resets to this; it may fire again only above REARM. */
	public static final float FLOOD_RESET = 10.0F;
	public static final float FLOOD_REARM = 12.0F;

	/** How a fragment's run ended. */
	public enum FragmentEnd { ABSORBED, CAPTURED, STRUCK, GRAFTED, RETURNED, RECALLED }

	private SeraphaeIntegrityRules() {
	}

	/** Per-tick loss: the base scaled by challengers (1 + 0.5 (n - 1)), plus each living fragment's pull. */
	public static float decay(int challengers, int livingFragments) {
		return BASE_DECAY * SaintKit.scale(challengers) + FRAGMENT_DECAY * Math.max(0, livingFragments);
	}

	/** Whether an opening is one of the two restraint windows (Recoil, Seized) or a gathered one (Condensing, Lapse). */
	public static boolean restraintWindow(int openingId) {
		return openingId == SeraphaeOpenings.RECOIL_ID || openingId == SeraphaeOpenings.SEIZED_ID;
	}

	/** The integrity a counted opening hit adds, weighted by the hit's school and rounded to the nearest half. */
	public static float hitGain(int openingId, SaintOpening.Delivery delivery, @Nullable EnumBloodTemper school) {
		boolean cast = delivery == SaintOpening.Delivery.SCHOOL_CAST;
		float base = restraintWindow(openingId) ? (cast ? RESTRAINT_CAST : RESTRAINT_HIT)
				: (cast ? GATHERED_CAST : GATHERED_HIT);
		return roundHalf(base * (cast ? schoolFactor(school) : 1.0F));
	}

	/** The stilling schools (circulation, sensation, decay) count more; her own count less. */
	public static float schoolFactor(@Nullable EnumBloodTemper school) {
		if (school == null) return 1.0F;
		return switch (school) {
			case CONGEATIO, TENEBRIS, MORTEM -> STILLING_FACTOR;
			case LUX, DUCTILIS -> OWN_SCHOOL_FACTOR;
			default -> 1.0F;
		};
	}

	/** A cast that breaks a hold: one of the stilling schools of circulation or sensation. */
	public static boolean breaksHold(@Nullable EnumBloodTemper school) {
		return school == EnumBloodTemper.CONGEATIO || school == EnumBloodTemper.TENEBRIS;
	}

	public static float roundHalf(float value) {
		return Math.round(value * 2.0F) / 2.0F;
	}

	/** Integrity from {@code blood} fed in Condensing, given what this player has already earned this window. */
	public static float feed(double blood, float earnedThisWindow) {
		float gain = (float) (blood / FEED_BLOOD_PER_POINT);
		return Math.max(0.0F, Math.min(gain, FEED_CAP - earnedThisWindow));
	}

	public static float fragment(FragmentEnd end) {
		return switch (end) {
			case ABSORBED -> FRAGMENT_ABSORBED;
			case CAPTURED -> FRAGMENT_CAPTURED;
			case STRUCK -> FRAGMENT_STRUCK;
			case GRAFTED -> FRAGMENT_GRAFTED;
			case RETURNED -> FRAGMENT_RETURNED;
			case RECALLED -> 0.0F;
		};
	}

	/** A reserve anchor woken: the first time pays more than a re-wake after she tore it loose. */
	public static float reserveWoken(boolean first) {
		return first ? RESERVE_FIRST : RESERVE_AGAIN;
	}

	/** True when the flood may fire: integrity at 0 and armed (it rose past REARM since the last flood). */
	public static boolean floods(float value, boolean armed) {
		return armed && value <= 0.0F;
	}

	public static boolean rearms(float value) {
		return value >= FLOOD_REARM;
	}
}
