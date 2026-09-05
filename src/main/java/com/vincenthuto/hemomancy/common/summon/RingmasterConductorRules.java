package com.vincenthuto.hemomancy.common.summon;

import java.util.UUID;

public final class RingmasterConductorRules {
	public static final double RELAY_RANGE = 32.0D;

	private RingmasterConductorRules() {
	}

	public static int activeCap(int baseCap, boolean ringmasterActive) {
		return Math.max(0, baseCap) + (ringmasterActive ? 1 : 0);
	}

	public static boolean canRelay(UUID conductorOwner, UUID summonOwner, UUID conductorSession,
			UUID summonSession, double distanceSqr) {
		return conductorOwner != null && conductorOwner.equals(summonOwner)
				&& conductorSession != null && conductorSession.equals(summonSession)
				&& distanceSqr <= RELAY_RANGE * RELAY_RANGE;
	}
}
