package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import java.util.Locale;

/**
 * The Kindled Champion title's state rules (Track 11 holds the state; Track 10 builds the fight). A player may offer to
 * usurp the title once the Court is known, a strain of theirs is at Apex and the Kindling count them a friend; a lost
 * attempt waits {@link #USURP_RETRY_TICKS} before the next. The title gives recognition only (T3).
 */
public final class CourtChampionRules {
	/** One in-game day between a lost usurpation and the next attempt (Track 10, PROPOSED). */
	public static final long USURP_RETRY_TICKS = 24000L;

	private CourtChampionRules() {
	}

	/** The outcome of asking to challenge for the title. Refusals carry a lang key. */
	public enum Gate {
		OPEN, REFUSED_UNKNOWN, REFUSED_SHUNNED, REFUSED_CHAMPION, REFUSED_STRAIN, REFUSED_STANDING, REFUSED_WAIT;

		public boolean open() {
			return this == OPEN;
		}

		public String translationKey() {
			return "hemomancy.court.champion." + name().toLowerCase(Locale.ROOT);
		}
	}

	public static Gate challenge(boolean known, boolean shunned, boolean alreadyChampion, boolean apexStrain,
			CourtStandingRules.Standing kindlingStanding, long now, long retryAt) {
		if (shunned) return Gate.REFUSED_SHUNNED;
		if (!known) return Gate.REFUSED_UNKNOWN;
		if (alreadyChampion) return Gate.REFUSED_CHAMPION;
		if (!apexStrain) return Gate.REFUSED_STRAIN;
		if (!kindlingStanding.atLeast(CourtStandingRules.Standing.FRIEND)) return Gate.REFUSED_STANDING;
		if (!retryElapsed(now, retryAt)) return Gate.REFUSED_WAIT;
		return Gate.OPEN;
	}

	public static boolean retryElapsed(long now, long retryAt) {
		return retryAt <= 0L || now >= retryAt;
	}

	/** The retry time after losing at {@code now}. */
	public static long retryAfterLoss(long now) {
		return now + USURP_RETRY_TICKS;
	}
}
