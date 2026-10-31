package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.core.BlockPos;

import java.util.List;

/**
 * The shape {@link FaneGroundRenderer} draws: a Founding Fane's heart lobe and its Hematic Stake lobes as the server
 * holds them ({@code FaneFootprint}: circles across the ground, unbounded up and down), joined by a smooth union so a
 * stake grows out of the heart's bounds, or out of another stake's, through a rounded neck instead of meeting it in a
 * notch. Away from a join the depth is exactly the server's; in a join's crease the drawn edge sits at most
 * {@link #BLEND} / 4 blocks outside it.
 */
public final class FaneGroundGeometry {
	/** How wide a join's neck is, in blocks of depth: lobes whose depths differ by more than this do not blend. */
	public static final double BLEND = 8.0D;
	/** Quarter turns toward a lobe's centre: +x, +z, -x, -z. */
	public static final int EAST = 0, SOUTH = 1, WEST = 2, NORTH = 3;

	private FaneGroundGeometry() {
	}

	/** How deep {@code (x, z)} lies inside the fane, in blocks: negative outside it. */
	public static double depth(BlockPos heart, double heartRadius, List<BlockPos> stakes, double stakeRadius,
			double x, double z) {
		double depth = depthIn(heart, heartRadius, x, z);
		for (BlockPos stake : stakes) depth = smoothMax(depth, depthIn(stake, stakeRadius, x, z), BLEND);
		return depth;
	}

	/** Depth inside one lobe: its radius less the distance across the ground from its centre. */
	public static double depthIn(BlockPos centre, double radius, double x, double z) {
		double dx = x - (centre.getX() + 0.5D);
		double dz = z - (centre.getZ() + 0.5D);
		return radius - Math.sqrt(dx * dx + dz * dz);
	}

	/** The larger of a and b, rounded over where they are within {@code k} of each other (never below the max). */
	public static double smoothMax(double a, double b, double k) {
		double h = Math.max(k - Math.abs(a - b), 0.0D) / k;
		return Math.max(a, b) + h * h * k * 0.25D;
	}

	/**
	 * Which way a glyph at {@code (x, z)} turns its foot: toward the centre of the lobe it lies deepest in, so the ring
	 * along a stake's edge reads outward from that stake, and the heart's from the heart.
	 */
	public static int facing(BlockPos heart, double heartRadius, List<BlockPos> stakes, double stakeRadius,
			double x, double z) {
		BlockPos owner = heart;
		double deepest = depthIn(heart, heartRadius, x, z);
		for (BlockPos stake : stakes) {
			double depth = depthIn(stake, stakeRadius, x, z);
			if (depth > deepest) {
				deepest = depth;
				owner = stake;
			}
		}
		double dx = owner.getX() + 0.5D - x;
		double dz = owner.getZ() + 0.5D - z;
		if (Math.abs(dx) > Math.abs(dz)) return dx > 0.0D ? EAST : WEST;
		return dz > 0.0D ? SOUTH : NORTH;
	}
}
