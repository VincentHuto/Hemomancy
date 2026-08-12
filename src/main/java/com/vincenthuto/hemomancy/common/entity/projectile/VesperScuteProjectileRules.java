package com.vincenthuto.hemomancy.common.entity.projectile;

public final class VesperScuteProjectileRules {
	public static final int MAX_LIFETIME_TICKS = 60;
	public static final double MAX_RANGE = 48.0D;

	private VesperScuteProjectileRules() {
	}

	public static boolean shouldExpire(int tickCount, double distanceFromOriginSqr) {
		return tickCount >= MAX_LIFETIME_TICKS || distanceFromOriginSqr > MAX_RANGE * MAX_RANGE;
	}

	public static boolean mayHit(boolean owner, boolean vesperBoss, boolean bossOwnedPuppet,
			boolean alive, boolean attackable) {
		return !owner && !vesperBoss && !bossOwnedPuppet && alive && attackable;
	}
}
