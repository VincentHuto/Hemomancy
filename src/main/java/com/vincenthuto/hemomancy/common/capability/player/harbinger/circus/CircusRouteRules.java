package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import java.util.Locale;

public final class CircusRouteRules {
	public static final int ALL_CHALLENGES = 0b1_1111;
	/**
	 * What absorbing him at the kneel asks (2026-11-18): the attention act and the three Succession acts. Bit 1, the
	 * old pre-fight route choice, is retired with the choice.
	 */
	public static final int SUCCESSION_CHALLENGES = 0b1_1101;
	/** The Degree the pact asks of everyone but an Unstained Liberator. */
	public static final int PACT_DEGREE = 4;

	private CircusRouteRules() {
	}

	/**
	 * The finale opens once the attention act is done and the gate is open (2026-11-18: the route is no longer chosen
	 * before the fight; it is decided at his kneel).
	 */
	public static boolean canBeginFinale(int challenges, PactGate gate) {
		return (challenges & 1) != 0 && gate == PactGate.OPEN;
	}

	/**
	 * Whether a player may absorb him at the kneel and take his place: full acclimation, the attention act and the three
	 * Succession acts, and not Unstained (D-31 holds: the Unstained have only the kill).
	 */
	public static boolean mayAbsorb(int acclimation, int challenges, boolean unstained) {
		return !unstained && acclimation >= CircusProgressRules.MAX_ACCLIMATION
				&& (challenges & SUCCESSION_CHALLENGES) == SUCCESSION_CHALLENGES;
	}

	/**
	 * D-30, D-31, NQ-15-5, NQ-15-6: whether a visitor may begin the finale. The Unstained need The Missing Understudy
	 * report and no Degree. Everyone else needs Degree 4 and any one school report.
	 */
	public static PactGate pactGate(boolean pactDegree, boolean anyReport, boolean understudyReport, boolean unstained) {
		if (unstained) return understudyReport ? PactGate.OPEN : PactGate.NEEDS_UNDERSTUDY;
		if (!pactDegree) return anyReport ? PactGate.NEEDS_DEGREE : PactGate.NEEDS_DEGREE_AND_REPORT;
		return anyReport ? PactGate.OPEN : PactGate.NEEDS_REPORT;
	}

	/** What a pact or finale refusal names. {@link #OPEN} lets the visitor through. */
	public enum PactGate {
		OPEN, NEEDS_DEGREE, NEEDS_REPORT, NEEDS_DEGREE_AND_REPORT, NEEDS_UNDERSTUDY, UNSTAINED;

		public String serializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}

	/**
	 * Where a player stands with him: no ending yet, or the ending they chose at his kneel (2026-11-18). There is no
	 * route chosen before the fight any more; an old save's pre-fight choice reads as NEUTRAL.
	 */
	public enum Route {
		NEUTRAL, SUCCESSION_COMPLETE, LIBERATION_COMPLETE;

		public String serializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		public static Route fromSerializedName(String name) {
			if (name == null) return NEUTRAL;
			try {
				return valueOf(name.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				return NEUTRAL;
			}
		}
	}
}
