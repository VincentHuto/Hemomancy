package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The Lightkeeper's first lesson (Track 11, L2, L5): cultivation as partnership. Once taught, feeding a strain inside its
 * own house's Seat or outpost multiplies the bonding blood that feeding credits toward its next maturity. The house's
 * own strain is needed; membership is not.
 */
public final class CourtLessonRules {
	/** The lesson is taught from the fifth Degree; runtime checks go through {@code ProgressionGates}. */
	public static final int MINIMUM_DEGREE = 5;
	/** First-pass multiplier on bonding credit, to be tuned in play. */
	public static final double MULTIPLIER = 1.5D;

	private CourtLessonRules() {
	}

	/** The outcome of the Lightkeeper's lesson. Refusals carry a lang key. */
	public enum Outcome {
		TAUGHT, ALREADY_TAUGHT, REFUSED_DEGREE, REFUSED_SHUNNED, REFUSED_TRANSGRESSION;

		public boolean taught() {
			return this == TAUGHT;
		}

		public String translationKey() {
			return "hemomancy.court.lesson." + name().toLowerCase(Locale.ROOT);
		}
	}

	public static Outcome teach(boolean alreadyTaught, boolean hasDegree, boolean shunned, boolean turnedAway) {
		if (shunned) return Outcome.REFUSED_SHUNNED;
		if (alreadyTaught) return Outcome.ALREADY_TAUGHT;
		if (turnedAway) return Outcome.REFUSED_TRANSGRESSION;
		if (!hasDegree) return Outcome.REFUSED_DEGREE;
		return Outcome.TAUGHT;
	}

	/**
	 * The bonding multiplier for feeding a strain of {@code strainHouse} while standing in {@code siteHouse}'s
	 * structure (null outside every Court site).
	 */
	public static double multiplier(boolean lessonTaught, @Nullable CourtHouse strainHouse, @Nullable CourtHouse siteHouse) {
		return lessonTaught && strainHouse != null && strainHouse == siteHouse ? MULTIPLIER : 1.0D;
	}
}
