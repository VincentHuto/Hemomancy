package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

/** Timing and reach of the Lodestone field a Living Axe critical drives into the ground. */
public final class LivingAxeLodestoneRules {
	public static final int LIFETIME_TICKS = 120;
	public static final int PULSE_INTERVAL_TICKS = 20;
	public static final double RADIUS = 2.5D;
	public static final float DAMAGE_PER_PULSE = 1.0F;
	public static final int LODESTONE_TICKS = 40;
	/** Horizontal speed each pulse drags a target toward the centre, before knockback resistance. */
	public static final double PULL_STRENGTH = 0.35D;

	private LivingAxeLodestoneRules() { }

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

	/**
	 * Horizontal pull toward the field's centre. It never overshoots the centre, and full knockback
	 * resistance holds a target in place.
	 */
	public static double[] pull(double centerX, double centerZ, double targetX, double targetZ,
			double knockbackResistance) {
		double dx = centerX - targetX;
		double dz = centerZ - targetZ;
		double distance = Math.sqrt(dx * dx + dz * dz);
		double resist = 1.0D - Math.max(0.0D, Math.min(1.0D, knockbackResistance));
		if (distance < 1.0E-4D || resist <= 0.0D) return new double[]{0.0D, 0.0D};
		double speed = Math.min(PULL_STRENGTH, distance * 0.5D) * resist;
		return new double[]{dx / distance * speed, dz / distance * speed};
	}
}
