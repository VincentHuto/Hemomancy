package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

final class ChalybeateSnailMovementRules {
	static final int CRAWL_START_CHANCE = 36;
	static final int CRAWL_BASE_TICKS = 55;
	static final int CRAWL_RANDOM_TICKS = 70;
	static final double CRAWL_HORIZONTAL_SPEED = 0.032D;
	static final double CRAWL_SINK_SPEED = -0.006D;
	static final double CLIMB_UPWARD_SPEED = 0.075D;
	static final float VENT_STEP_HEIGHT = 1.0F;

	private ChalybeateSnailMovementRules() {
	}

	static boolean canUseVentClimb(boolean inWater, boolean retracted, boolean touchingClimbableVent) {
		return inWater && !retracted && touchingClimbableVent;
	}

	static double climbAdjustedY(double currentY, boolean climbingVent) {
		return climbingVent ? Math.max(currentY, CLIMB_UPWARD_SPEED) : currentY;
	}
}
