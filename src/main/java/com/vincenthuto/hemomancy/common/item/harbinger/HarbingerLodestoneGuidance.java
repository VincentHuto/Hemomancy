package com.vincenthuto.hemomancy.common.item.harbinger;

public final class HarbingerLodestoneGuidance {
	private HarbingerLodestoneGuidance() {
	}

	public static double alignment(float playerYaw, double dx, double dz) {
		if (dx == 0.0D && dz == 0.0D) return 1.0D;
		double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
		double difference = Math.toRadians(Math.IEEEremainder(targetYaw - playerYaw, 360.0D));
		return Math.max(0.0D, Math.cos(difference));
	}
}
