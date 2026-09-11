package com.vincenthuto.hemomancy.common.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class EarthenVeinTravelRules {
	public static final double MAX_SOURCE_DISTANCE_SQUARED = 64.0D;
	public static final int DISPLAY_TIMEOUT_TICKS = 600;
	public static final int FEED_TIMEOUT_TICKS = 300;
	public static final int READY_TIMEOUT_TICKS = 600;
	public static final int SWALLOW_TICKS = 24;
	public static final int EJECTION_TICKS = 20;

	private EarthenVeinTravelRules() {
	}

	public static int cost(BlockPos origin, ResourceLocation originDimension,
			BlockPos destination, ResourceLocation destinationDimension) {
		if (!originDimension.equals(destinationDimension)) return 4_000;
		long dx = (long) destination.getX() - origin.getX();
		long dz = (long) destination.getZ() - origin.getZ();
		long distanceSquared = dx * dx + dz * dz;
		if (distanceSquared <= 256L * 256L) return 500;
		if (distanceSquared <= 1_024L * 1_024L) return 1_000;
		if (distanceSquared <= 4_096L * 4_096L) return 2_000;
		return 3_000;
	}

	public static double acceptedBlood(double current, double required, double offered, double available) {
		if (current >= required || required <= 0.0D || offered <= 0.0D || available <= 0.0D) return 0.0D;
		return Math.min(required - current, Math.min(offered, available));
	}

	public static boolean feedExpired(long lastFed, long now) {
		return now - lastFed > FEED_TIMEOUT_TICKS;
	}

	public static boolean readyExpired(long readySince, long now) {
		return now - readySince > READY_TIMEOUT_TICKS;
	}

	public static long temporaryExpiry(EarthenVeinTravelPhase phase, long now) {
		return now + switch (phase) {
			case SELECTING -> DISPLAY_TIMEOUT_TICKS + 1L;
			case FEEDING -> FEED_TIMEOUT_TICKS + 1L;
			case READY -> READY_TIMEOUT_TICKS + SWALLOW_TICKS + 1L;
			case SWALLOWING -> SWALLOW_TICKS + 1L;
			default -> 1L;
		};
	}

	public static boolean hasFallProtection(long protectedUntil, long now) {
		return now <= protectedUntil;
	}
}
