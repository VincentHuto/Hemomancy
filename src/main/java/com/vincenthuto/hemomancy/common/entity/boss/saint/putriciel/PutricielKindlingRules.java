package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintKit;

/**
 * The Kindling, Putriciel's signature meter (design section 4, resolutions B-5 and D-6): the buried purifier's will
 * catching. Pure, so it is JVM-tested.
 * <p>
 * Its maximum is 100 scaled by the kit's one rule (1 + 0.5 (n - 1)). The creep, the burn-down, the Communion's drain
 * and the Repose head start are fractions of that maximum, so their timing is the same for any group; damage-driven
 * fill is absolute, so a group fills a larger meter. It fills only in the rot and the Embering; it holds through the
 * Ignition and burns down 1 point in 3 ticks through the interval, pausing while an Offering is open or the Full Purge
 * runs.
 */
public final class PutricielKindlingRules {
	public static final float BASE_MAX = 100.0F;
	/** Raw damage (before armour) per point. */
	public static final float DAMAGE_PER_POINT = 5.0F;
	/** Fire and Flammeus casts stoke it this many times harder (fire fed to the rot is drunk). */
	public static final float FLAMMEUS = 2.5F;
	/** Ticks per point of burn-down in the interval. */
	public static final float BURN_TICKS = 3.0F;
	/** Points per tick a Mould Communion drains through its active span (30 over 60 ticks). */
	public static final float COMMUNION_DRAIN = 0.5F;
	/** Points a broken Communion adds. */
	public static final float COMMUNION_BREAK = 10.0F;
	/** Points per tick held Blood Absorption on spent sacs adds, and the most one opening gives. */
	public static final float ABSORB_PER_TICK = 1.5F;
	public static final float ABSORB_CAP = 40.0F;
	/** The Repose deed's head start: the fraction the next rot phase starts at, as the Embering would have built. */
	public static final float REPOSE_HEAD_START = 0.25F;
	/** Fractions: the first and second Offerings arm as it burns past these; the tremor shows above the third. */
	public static final float FIRST_OFFERING = 0.66F;
	public static final float SECOND_OFFERING = 0.33F;
	public static final float TREMOR = 0.75F;

	private PutricielKindlingRules() {
	}

	public static float max(int challengers) {
		return BASE_MAX * SaintKit.scale(challengers);
	}

	/** The natural creep a tick in a rot phase. */
	public static float creep(PutricielPhase phase, int challengers) {
		if (phase.creepTicks() <= 0) return 0.0F;
		return SaintKit.scale(challengers) / phase.creepTicks();
	}

	/** The burn-down a tick through the interval. */
	public static float burn(int challengers) {
		return SaintKit.scale(challengers) / BURN_TICKS;
	}

	/** The Communion's drain a tick. */
	public static float communionDrain(int challengers) {
		return COMMUNION_DRAIN * SaintKit.scale(challengers);
	}

	public static float reposeHeadStart(int challengers) {
		return REPOSE_HEAD_START * max(challengers);
	}

	/**
	 * Points a hit of {@code rawDamage} adds: one per 5, x2.5 for fire or a Flammeus cast, times the open window's
	 * multiplier. Only in the rot and the Embering; nothing in the fire.
	 */
	public static float fromDamage(float rawDamage, boolean flammeus, float openingMultiplier, PutricielStage stage) {
		if (rawDamage <= 0.0F || stage.fire() || stage == PutricielStage.IGNITING) return 0.0F;
		float points = rawDamage / DAMAGE_PER_POINT;
		if (flammeus) points *= FLAMMEUS;
		return points * Math.max(0.0F, openingMultiplier);
	}

	/**
	 * The change a tick: the creep (less the Communion's drain) in the rot, held through the Ignition and the Embering,
	 * the burn-down in the interval unless {@code paused}.
	 */
	public static float drift(PutricielStage stage, PutricielPhase phase, int challengers, boolean paused,
			boolean communion) {
		return switch (stage) {
			case ROT -> creep(phase, challengers) - (communion ? communionDrain(challengers) : 0.0F);
			case IGNITING, EMBERING -> 0.0F;
			case BURNING -> paused ? 0.0F : -burn(challengers);
		};
	}

	/** Full: the interval is due (at the next recovery boundary, never in an opening). */
	public static boolean full(float value, float max) {
		return value >= max - 1.0E-3F;
	}

	/** How many Offerings the burn-down has armed at {@code fraction}: 0, 1 past 66%, 2 past 33%. */
	public static int armed(float fraction) {
		if (fraction <= SECOND_OFFERING) return 2;
		return fraction <= FIRST_OFFERING ? 1 : 0;
	}

	/** The absorption one tick on spent sacs may still add, given what this opening already gave. */
	public static float absorb(float alreadyGiven) {
		return Math.max(0.0F, Math.min(ABSORB_PER_TICK, ABSORB_CAP - alreadyGiven));
	}
}
