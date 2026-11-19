package com.vincenthuto.hemomancy.common.encounter.ordeal;

import net.minecraft.world.phys.Vec3;

/**
 * The Strongman's interventions from outside his ring (2026-11-17), as pure geometry: what each one hits, so it can be
 * read and dodged. Positions are feet; {@code stage} is the stage origin's centre on the ring floor.
 */
public final class OrdealMoves {
	public static final double WEIGHT_RADIUS = 2.0D;
	/** A heave's wave starts at the rim and runs inward this fast; it hits feet within half a block of it. */
	public static final double HEAVE_START = 9.0D, HEAVE_SPEED = 0.25D, HEAVE_WIDTH = 0.5D;
	/** Feet this far above the floor clear the heave and the bar: a jump. */
	public static final double CLEARANCE = 1.1D;
	/** The bar sweeps the ring along z, from one rim to the other, over this many ticks; it hits within this of it. */
	public static final int SWEEP_TICKS = 50;
	public static final double SWEEP_REACH = 9.5D, BAR_HALF_WIDTH = 0.6D;
	/** How far the floor's tilt drags a player toward the puppet each tick while he holds the line. */
	public static final double PULL = 0.06D;
	public static final float WEIGHT_DAMAGE = 6.0F, HEAVE_DAMAGE = 4.0F, SWEEP_DAMAGE = 5.0F;

	private OrdealMoves() {
	}

	public static boolean droppedWeightHits(Vec3 mark, Vec3 feet) {
		return horizontal(mark, feet) <= WEIGHT_RADIUS && feet.y - mark.y < 3.0D && feet.y >= mark.y - 0.5D;
	}

	public static double heaveRadius(int ticks) {
		return Math.max(0.0D, HEAVE_START - ticks * HEAVE_SPEED);
	}

	public static boolean groundHeaveHits(Vec3 stage, double wave, Vec3 feet) {
		return Math.abs(horizontal(stage, feet) - wave) <= HEAVE_WIDTH && feet.y - stage.y < CLEARANCE;
	}

	/** Where the bar is along x, from -reach to +reach across the sweep. */
	public static double sweepOffset(int ticks) {
		return -SWEEP_REACH + 2.0D * SWEEP_REACH * Math.min(1.0D, ticks / (double) SWEEP_TICKS);
	}

	public static boolean barSweepHits(Vec3 stage, double along, Vec3 feet) {
		return Math.abs(feet.x - (stage.x + along)) <= BAR_HALF_WIDTH && feet.y - stage.y < CLEARANCE
				&& horizontal(stage, feet) <= SWEEP_REACH;
	}

	public static Vec3 holdTheLinePull(Vec3 feet, Vec3 puppet) {
		Vec3 toward = new Vec3(puppet.x - feet.x, 0.0D, puppet.z - feet.z);
		return toward.lengthSqr() < 1.0E-6D ? Vec3.ZERO : toward.normalize().scale(PULL);
	}

	private static double horizontal(Vec3 a, Vec3 b) {
		return Math.hypot(a.x - b.x, a.z - b.z);
	}
}
