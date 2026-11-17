package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import java.util.Locale;

/**
 * The Kindled Champion title's state rules (Track 11 holds the state; Track 10 builds the fight). A player may offer to
 * usurp the title from the fifth Degree once the Court is known, a strain of theirs is at Apex and the Kindling count
 * them a friend; a lost or abandoned attempt waits {@link #USURP_RETRY_TICKS} before the next. The title gives
 * recognition only (T3).
 */
public final class CourtChampionRules {
	/** One in-game day between a lost usurpation and the next attempt (Track 10, PROPOSED). */
	public static final long USURP_RETRY_TICKS = 24000L;
	/** The challenge needs the fifth Degree, as joining a house and the lesson do. */
	public static final int MINIMUM_DEGREE = 5;

	private CourtChampionRules() {
	}

	/** The outcome of asking to challenge for the title. Refusals carry a lang key. */
	public enum Gate {
		OPEN, REFUSED_UNKNOWN, REFUSED_SHUNNED, REFUSED_CHAMPION, REFUSED_STRAIN, REFUSED_STANDING, REFUSED_WAIT, REFUSED_DEGREE;

		public boolean open() {
			return this == OPEN;
		}

		public String translationKey() {
			return "hemomancy.court.champion." + name().toLowerCase(Locale.ROOT);
		}
	}

	public static Gate challenge(boolean known, boolean shunned, boolean alreadyChampion, boolean degreeReached,
			boolean apexStrain, CourtStandingRules.Standing kindlingStanding, long now, long retryAt) {
		if (shunned) return Gate.REFUSED_SHUNNED;
		if (!known) return Gate.REFUSED_UNKNOWN;
		if (alreadyChampion) return Gate.REFUSED_CHAMPION;
		if (!degreeReached) return Gate.REFUSED_DEGREE;
		// The wait comes first: losing moves the worn strain into the Jar, and the player should hear about the day.
		if (!retryElapsed(now, retryAt)) return Gate.REFUSED_WAIT;
		if (!apexStrain) return Gate.REFUSED_STRAIN;
		if (!kindlingStanding.atLeast(CourtStandingRules.Standing.FRIEND)) return Gate.REFUSED_STANDING;
		return Gate.OPEN;
	}

	public static boolean retryElapsed(long now, long retryAt) {
		return retryAt <= 0L || now >= retryAt;
	}

	/** The retry time after losing or leaving at {@code now}. */
	public static long retryAfterLoss(long now) {
		return now + USURP_RETRY_TICKS;
	}
}
