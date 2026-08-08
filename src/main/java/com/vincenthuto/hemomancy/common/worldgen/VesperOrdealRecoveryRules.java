package com.vincenthuto.hemomancy.common.worldgen;

final class VesperOrdealRecoveryRules {
	enum Action {
		NONE,
		ABANDON,
		RETARGET,
		RESPAWN
	}

	private VesperOrdealRecoveryRules() {
	}

	static Action reconnectAction(boolean active, boolean inChamber, boolean ownedBossPresent) {
		if (!active) return Action.NONE;
		if (!inChamber) return Action.ABANDON;
		return ownedBossPresent ? Action.RETARGET : Action.RESPAWN;
	}
}
