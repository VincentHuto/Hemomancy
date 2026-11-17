package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import java.util.Locale;

/**
 * Standing with a Brined Court house (Track 11): stranger, guest, friend. It is raised by acts, never bought, and each
 * act counts once per house. Visiting a house's site makes a stranger its guest; the house's own custom raises a step;
 * a custom and a visit together make a friend in either order.
 * A Giving Light transgression drops that house to stranger; leaving a house drops it to guest, never lower.
 */
public final class CourtStandingRules {
	private CourtStandingRules() {
	}

	/** Saved by {@link #level()}; append, never reorder. */
	public enum Standing {
		STRANGER(0), GUEST(1), FRIEND(2);

		private final int level;

		Standing(int level) {
			this.level = level;
		}

		public int level() {
			return level;
		}

		public boolean atLeast(Standing other) {
			return level >= other.level;
		}

		/** The standing for a saved level, clamped into range. */
		public static Standing ofLevel(int level) {
			if (level <= STRANGER.level) return STRANGER;
			if (level >= FRIEND.level) return FRIEND;
			return GUEST;
		}

		/** Lang key for the standing's name. */
		public String translationKey() {
			return "court_standing.hemomancy." + name().toLowerCase(Locale.ROOT);
		}
	}

	/** What an act does to standing. */
	public enum ActKind {
		/**
		 * Being at the house's site, or taking its welcome gift: a stranger becomes a guest. The first one after the
		 * house's custom made the guest makes a friend, so the order of the two never matters.
		 */
		VISIT,
		/** The house's own custom (food to the Garden, a promise kept, an offering at a vent): one step up. */
		CUSTOM
	}

	/** The prefix of every custom act id ({@code custom_food}, {@code custom_vent_offering}, ...). */
	public static final String CUSTOM_PREFIX = "custom";

	/** The kind of a counted act, read from its id: a custom by its prefix, anything else a visit. */
	public static ActKind kindOf(String actId) {
		return actId.startsWith(CUSTOM_PREFIX) ? ActKind.CUSTOM : ActKind.VISIT;
	}

	/**
	 * The standing after an act that hasn't been counted with this house before. {@code customCounted} and
	 * {@code visitCounted} say whether an act of either kind was counted with the house earlier.
	 */
	public static Standing afterAct(Standing current, ActKind kind, boolean customCounted, boolean visitCounted) {
		return switch (kind) {
		case VISIT -> {
			if (!current.atLeast(Standing.GUEST)) yield Standing.GUEST;
			yield current == Standing.GUEST && customCounted && !visitCounted ? Standing.FRIEND : current;
		}
		case CUSTOM -> Standing.ofLevel(current.level() + 1);
		};
	}

	/**
	 * Whether an act may change standing: it hasn't been counted with this house, the player isn't shunned, and the
	 * Giving Light isn't turning them away over an outstanding transgression.
	 */
	public static boolean actCounts(boolean alreadyCounted, boolean shunned, boolean turnedAway) {
		return !alreadyCounted && !shunned && !turnedAway;
	}

	/** Standing with the house a member leaves: down to guest, never to stranger or below. */
	public static Standing afterLeaving(Standing current) {
		return current.atLeast(Standing.GUEST) ? Standing.GUEST : current;
	}

	/** Standing with the Giving Light after a transgression against it. */
	public static Standing afterTransgression() {
		return Standing.STRANGER;
	}

	/** Standing with the Giving Light after amends: restored to guest. */
	public static Standing afterAmends(Standing current) {
		return current.atLeast(Standing.GUEST) ? current : Standing.GUEST;
	}
}
