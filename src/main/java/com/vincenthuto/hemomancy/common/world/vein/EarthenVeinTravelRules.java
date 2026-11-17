package com.vincenthuto.hemomancy.common.world.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class EarthenVeinTravelRules {
	public static final double MAX_SOURCE_DISTANCE_SQUARED = 64.0D;
	public static final int DISPLAY_TIMEOUT_TICKS = 600;
	public static final int FEED_TIMEOUT_TICKS = 300;
	public static final int READY_TIMEOUT_TICKS = 600;
	public static final int SWALLOW_TICKS = 24;
	public static final int EJECTION_TICKS = 20;
	/**
	 * The flat price of a crossing between two openings that one living vein joins (core-loop round two P8): the
	 * tissue carries the traveller, so distance no longer counts.
	 */
	public static final int LIVING_VEIN_COST = 125;

	private EarthenVeinTravelRules() {
	}

	public static int cost(BlockPos origin, ResourceLocation originDimension,
			BlockPos destination, ResourceLocation destinationDimension) {
		return cost(origin, originDimension, destination, destinationDimension, false);
	}

	/**
	 * The blood a crossing costs. {@code carried}: a living vein joins the two openings, so a crossing within one
	 * dimension costs {@link #LIVING_VEIN_COST}; across dimensions it never helps.
	 */
	public static int cost(BlockPos origin, ResourceLocation originDimension,
			BlockPos destination, ResourceLocation destinationDimension, boolean carried) {
		if (!originDimension.equals(destinationDimension)) return 4_000;
		if (carried) return LIVING_VEIN_COST;
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
