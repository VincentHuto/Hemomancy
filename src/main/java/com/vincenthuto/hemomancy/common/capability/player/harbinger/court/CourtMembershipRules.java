package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * Brined Court house membership (Track 11, L1, L5, L6): one free entry into a house and one free switch after it, then
 * membership is fixed. Joining needs the fifth Degree and guest standing with the house, and no strain. An Unstained
 * player is shunned and can't join; the Giving Light turns away a player with an outstanding transgression.
 */
public final class CourtMembershipRules {
	/** Joining a house starts at the fifth Degree; runtime checks go through {@code ProgressionGates}. */
	public static final int MINIMUM_DEGREE = 5;

	private CourtMembershipRules() {
	}

	/** The outcome of asking to join a house. Refusals carry a lang key. */
	public enum Outcome {
		JOINED(true),
		SWITCHED(true),
		ALREADY_MEMBER(false),
		REFUSED_SWITCH_USED(false),
		REFUSED_DEGREE(false),
		REFUSED_STANDING(false),
		REFUSED_SHUNNED(false),
		REFUSED_TRANSGRESSION(false);

		private final boolean changed;

		Outcome(boolean changed) {
			this.changed = changed;
		}

		/** True when membership changed. */
		public boolean changed() {
			return changed;
		}

		/** The message a player is shown: the refusal's reason, or the joining. */
		public String translationKey() {
			return "hemomancy.court.membership." + name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * What happens when a player asks to join {@code target}.
	 *
	 * @param current        the player's house, or null before their entry
	 * @param switchUsed     whether their one switch is spent
	 * @param hasDegree      whether they have reached {@link #MINIMUM_DEGREE}
	 * @param standing       their standing with {@code target}
	 * @param shunned        whether every house shuns them (the Unstained path)
	 * @param turnedAway     whether {@code target} is turning them away (an outstanding Giving Light transgression)
	 */
	public static Outcome join(@Nullable CourtHouse current, boolean switchUsed, CourtHouse target, boolean hasDegree,
			CourtStandingRules.Standing standing, boolean shunned, boolean turnedAway) {
		if (shunned) return Outcome.REFUSED_SHUNNED;
		if (current == target) return Outcome.ALREADY_MEMBER;
		if (current != null && switchUsed) return Outcome.REFUSED_SWITCH_USED;
		if (turnedAway) return Outcome.REFUSED_TRANSGRESSION;
		if (!hasDegree) return Outcome.REFUSED_DEGREE;
		if (!standing.atLeast(CourtStandingRules.Standing.GUEST)) return Outcome.REFUSED_STANDING;
		return current == null ? Outcome.JOINED : Outcome.SWITCHED;
	}

	/** Whether a house turns the player away over an outstanding transgression: only the Giving Light keeps that record. */
	public static boolean turnsAway(CourtHouse house, boolean outstandingTransgression) {
		return house == CourtHouse.GIVING_LIGHT && outstandingTransgression;
	}
}
