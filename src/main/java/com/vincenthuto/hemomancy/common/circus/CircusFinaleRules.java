package com.vincenthuto.hemomancy.common.circus;

public final class CircusFinaleRules {
	public static final int RAFTER_TICKS = 160;

	private CircusFinaleRules() {
	}

	public static CircusPavilionStateRules.Phase nextPhase(CircusRouteRules.Route route,
			CircusPavilionStateRules.Phase phase, int phaseTicks, boolean troupeDowned,
			boolean anchorsBroken) {
		if (phase == CircusPavilionStateRules.Phase.RAFTERS && phaseTicks >= RAFTER_TICKS) {
			return CircusPavilionStateRules.Phase.CAROUSEL;
		}
		if (phase == CircusPavilionStateRules.Phase.CAROUSEL
				&& (route == CircusRouteRules.Route.SUCCESSION && troupeDowned
				|| route == CircusRouteRules.Route.LIBERATION && anchorsBroken)) {
			return CircusPavilionStateRules.Phase.DESCENT;
		}
		return phase;
	}
}
