package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import java.util.Locale;

public final class CircusRouteRules {
	public static final int ALL_CHALLENGES = 0b1_1111;
	/** The Degree the pact asks of everyone but an Unstained Liberator. */
	public static final int PACT_DEGREE = 4;

	private CircusRouteRules() {
	}

	public static boolean canChoose(Route current) {
		return current == Route.NEUTRAL;
	}

	public static Route choose(Route current, Route choice) {
		if (!canChoose(current)) return current;
		return choice == Route.SUCCESSION || choice == Route.LIBERATION ? choice : current;
	}

	public static boolean canRepair(Route route, boolean alreadyRepaired) {
		return !alreadyRepaired && (route == Route.SUCCESSION || route == Route.LIBERATION);
	}

	public static boolean canBeginFinale(Route route, int acclimation, int challenges) {
		if (route == Route.LIBERATION) return true;
		return route == Route.SUCCESSION && acclimation >= CircusProgressRules.MAX_ACCLIMATION
				&& (challenges & ALL_CHALLENGES) == ALL_CHALLENGES;
	}

	/**
	 * D-30, D-31, NQ-15-5, NQ-15-6: whether a visitor may take {@code route} at the pact, or begin its finale. The Unstained
	 * may run Liberation only, and need The Missing Understudy report and no Degree. Everyone else needs Degree 4 and
	 * any one school report.
	 */
	public static PactGate pactGate(Route route, boolean pactDegree, boolean anyReport, boolean understudyReport,
			boolean unstained) {
		if (unstained) {
			if (route != Route.LIBERATION) return PactGate.UNSTAINED;
			return understudyReport ? PactGate.OPEN : PactGate.NEEDS_UNDERSTUDY;
		}
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

	public enum Route {
		NEUTRAL, SUCCESSION, LIBERATION, SUCCESSION_COMPLETE, LIBERATION_COMPLETE;

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
