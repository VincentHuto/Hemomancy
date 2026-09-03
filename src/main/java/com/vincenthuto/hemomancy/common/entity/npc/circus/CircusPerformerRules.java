package com.vincenthuto.hemomancy.common.entity.npc.circus;

public final class CircusPerformerRules {
	public static final int WARNING_TICKS = 20;
	public static final int THREAT_CLEAR_TICKS = 200;
	public static final double PERFORMANCE_RANGE_SQR = 24.0D * 24.0D;
	public static final double THREAT_RANGE_SQR = 48.0D * 48.0D;
	private static final double FIRE_RANGE_SQR = 25.0D;
	private static final double FIRE_CONE_COSINE = Math.cos(Math.toRadians(27.5D));

	private CircusPerformerRules() {
	}

	public static boolean shouldPerform(double nearestPlayerDistanceSqr, boolean alert, boolean downed) {
		return nearestPlayerDistanceSqr <= PERFORMANCE_RANGE_SQR && !alert && !downed;
	}

	public static boolean warningComplete(int warningTicks) {
		return warningTicks >= WARNING_TICKS;
	}

	public static int dollCount(int randomValue) {
		return 2 + Math.floorMod(randomValue, 3);
	}

	public static boolean shouldClearThreat(boolean alive, boolean sameDimension, double distanceSqr,
			int outsideTicks) {
		return !alive || !sameDimension || distanceSqr > THREAT_RANGE_SQR && outsideTicks >= THREAT_CLEAR_TICKS;
	}

	public static boolean shouldEnterDowned(float health, float damage, boolean administrativeDamage) {
		return !administrativeDamage && damage >= health;
	}

	public static boolean isSafeVault(boolean loaded, boolean insideBorder, boolean supported,
			boolean collisionFree) {
		return loaded && insideBorder && supported && collisionFree;
	}

	public static boolean insideCone(double facingX, double facingZ, double offsetX, double offsetZ) {
		double distanceSqr = offsetX * offsetX + offsetZ * offsetZ;
		if (distanceSqr <= 0.0D || distanceSqr > FIRE_RANGE_SQR) return false;
		double facingLength = Math.sqrt(facingX * facingX + facingZ * facingZ);
		return facingLength > 0.0D
				&& (facingX * offsetX + facingZ * offsetZ) / (facingLength * Math.sqrt(distanceSqr))
				>= FIRE_CONE_COSINE;
	}
}
