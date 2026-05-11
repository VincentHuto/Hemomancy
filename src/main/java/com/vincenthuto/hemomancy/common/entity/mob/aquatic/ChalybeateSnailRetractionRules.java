package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class ChalybeateSnailRetractionRules {
	static final double THREAT_DETECTION_RADIUS = 2.25D;
	static final int THREAT_HOLD_TICKS = 60;
	static final int INTERACTION_TUCK_TICKS = 160;
	static final int HARVEST_TUCK_TICKS = 180;
	static final int HURT_TUCK_TICKS = 160;

	private ChalybeateSnailRetractionRules() {
	}

	static int refreshForThreat(int currentTicks, boolean hasThreat) {
		return hasThreat ? Math.max(currentTicks, THREAT_HOLD_TICKS) : currentTicks;
	}

	static int tickDown(int currentTicks) {
		return Math.max(0, currentTicks - 1);
	}

	static boolean shouldRenderRetracted(boolean syncedFlag, int localTicks) {
		return syncedFlag || localTicks > 0;
	}

	static boolean isThreat(boolean creativePlayer, boolean aquaticTag, boolean waterAnimal) {
		return !creativePlayer && !aquaticTag && !waterAnimal;
	}
}
