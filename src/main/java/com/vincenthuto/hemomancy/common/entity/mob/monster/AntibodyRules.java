package com.vincenthuto.hemomancy.common.entity.mob.monster;

import java.util.UUID;

/** When an antibody's work is over, and whom it may strike: its summoner, and no one else. */
public final class AntibodyRules {
	public static final int LIFETIME_TICKS = 1200;
	public static final double REACH = 64.0;

	private AntibodyRules() {}

	public static boolean shouldDissolve(int life, boolean summonerPresent, boolean summonerAlive, boolean sameLevel,
			double distanceSqr) {
		return life > LIFETIME_TICKS || !summonerPresent || !summonerAlive || !sameLevel || distanceSqr > REACH * REACH;
	}

	public static boolean mayStrike(UUID summoner, UUID target) {
		return summoner != null && summoner.equals(target);
	}
}
