package com.vincenthuto.hemomancy.common.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Shared axis pick for the Cortical Drift's pillar-shaped cables: nerve fiber and nerve bundle are
 * {@code RotatedPillarBlock}s, so every segment is oriented along the axis it travels furthest on
 * and the texture runs with the cable instead of across it. Both the nerve bridges and the Vagrant
 * Mind's interior web need this, so it lives in one place rather than once per placer.
 */
public final class CorticalAxis {
	private CorticalAxis() {
	}

	/** Axis of the largest component, ties resolved Y then X so a vertical drop never reads sideways. */
	public static Direction.Axis dominant(double dx, double dy, double dz) {
		double ax = Math.abs(dx);
		double ay = Math.abs(dy);
		double az = Math.abs(dz);
		if (ay >= ax && ay >= az) {
			return Direction.Axis.Y;
		}
		return ax >= az ? Direction.Axis.X : Direction.Axis.Z;
	}

	/** Axis a segment between two sampled points travels along. */
	public static Direction.Axis between(double[] from, double[] to) {
		return dominant(to[0] - from[0], to[1] - from[1], to[2] - from[2]);
	}

	/** Axis a segment between two block positions travels along. */
	public static Direction.Axis between(BlockPos from, BlockPos to) {
		return dominant(to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ());
	}
}
