package com.vincenthuto.hemomancy.common.circus;

import java.util.Locale;

public final class CircusRouteRules {
	public static final int ALL_CHALLENGES = 0b1_1111;

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
