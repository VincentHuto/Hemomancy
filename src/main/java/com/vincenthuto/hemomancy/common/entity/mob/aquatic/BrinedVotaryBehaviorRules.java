package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class BrinedVotaryBehaviorRules {
	public static final double WAKE_RANGE_BLOCKS = 6.0D;
	public static final double SWIM_SPEED = 0.75D;
	public static final double LAND_SPEED = 0.9D;
	public static final double ATTACK_DAMAGE = 3.0D;

	private BrinedVotaryBehaviorRules() {
	}

	public static boolean shouldWakeForTarget(boolean isPlayer, boolean targetAlive, double distanceBlocks) {
		return isPlayer && targetAlive && distanceBlocks <= WAKE_RANGE_BLOCKS;
	}
}
