package com.vincenthuto.hemomancy.common.entity.npc.circus;

public final class CircusCarouselRules {
	public static final double HORSE_RADIUS = 2.35D;
	public static final int WIND_UP_TICKS = 20;
	private static final float IDLE_SPEED = 0.5F;
	private static final float ACTIVE_SPEED = 3.0F;
	private static final float ACCELERATION = 0.1F;
	private static final double BOB_HEIGHT = 0.45D;

	private CircusCarouselRules() {
	}

	public static boolean shouldActivate(int alertPerformers) {
		return alertPerformers > 0;
	}

	public static boolean canStrike(int activeTicks) {
		return activeTicks >= WIND_UP_TICKS;
	}

	public static float targetSpeed(boolean active) {
		return active ? ACTIVE_SPEED : IDLE_SPEED;
	}

	public static float nextSpeed(float current, boolean active) {
		float target = targetSpeed(active);
		return current < target ? Math.min(target, current + ACCELERATION)
				: Math.max(target, current - ACCELERATION);
	}

	public static HorsePose horsePose(float rotationDegrees, int horseIndex) {
		float angle = rotationDegrees + Math.floorMod(horseIndex, 3) * 120.0F;
		double radians = Math.toRadians(angle);
		return new HorsePose(angle, Math.cos(radians) * HORSE_RADIUS,
				Math.sin(radians) * HORSE_RADIUS, Math.sin(radians) * BOB_HEIGHT);
	}

	public record HorsePose(float angleDegrees, double x, double z, double bob) {
	}
}
