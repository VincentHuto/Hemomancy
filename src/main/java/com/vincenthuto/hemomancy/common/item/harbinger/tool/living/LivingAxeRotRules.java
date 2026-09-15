package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingAxeRotRules {
	public static final int LIFETIME_TICKS = 120;
	public static final int PULSE_INTERVAL_TICKS = 20;
	public static final double RADIUS = 2.5D;
	public static final float DAMAGE_PER_PULSE = 1.0F;
	public static final int NECROSIS_TICKS = 40;

	private LivingAxeRotRules() { }

	public static boolean isActive(long createdAt, long now) {
		return now - createdAt < LIFETIME_TICKS;
	}

	public static boolean shouldPulse(long createdAt, long now) {
		return isActive(createdAt, now) && (now - createdAt) % PULSE_INTERVAL_TICKS == 0L;
	}

	public static boolean contains(double centerX, double centerZ, double targetX, double targetZ) {
		double dx = targetX - centerX;
		double dz = targetZ - centerZ;
		return dx * dx + dz * dz <= RADIUS * RADIUS;
	}

	public static boolean overlaps(double firstX, double firstZ, double secondX, double secondZ) {
		double dx = firstX - secondX;
		double dz = firstZ - secondZ;
		return dx * dx + dz * dz <= 4.0D * RADIUS * RADIUS;
	}

	public static long mergedExpiry(long now, long existingExpiry) {
		return Math.max(existingExpiry, now + LIFETIME_TICKS);
	}
}
