package com.vincenthuto.hemomancy.common.world;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A Soft Envelope's geometry: a heart lobe and the smaller lobes chained off it (a Founding Fane's stakes, a
 * heart-node's synapses). Distances run between block centres across the ground; a vertical band, when it is not
 * {@link #UNBOUNDED}, limits each lobe to that many blocks above or below its own centre.
 */
public final class LobeEnvelope {
	public static final int UNBOUNDED = Integer.MAX_VALUE;

	private LobeEnvelope() {}

	public static boolean contains(@Nullable BlockPos heart, List<BlockPos> lobes, BlockPos pos, double heartRadius,
			double lobeRadius, int verticalBand) {
		if (heart != null && inLobe(pos, heart, heartRadius, verticalBand)) return true;
		for (BlockPos lobe : lobes) {
			if (inLobe(pos, lobe, lobeRadius, verticalBand)) return true;
		}
		return false;
	}

	/**
	 * Whether a lobe may be raised at {@code pos}: there is a heart, the budget is not spent, no lobe stands there yet,
	 * and its lobe would overlap the heart's or an existing lobe's, inside the band of the lobe it chains to.
	 */
	public static boolean canAddLobe(@Nullable BlockPos heart, List<BlockPos> lobes, BlockPos pos, double heartRadius,
			double lobeRadius, int budget, int verticalBand) {
		if (heart == null || lobes.size() >= budget || lobes.contains(pos)) return false;
		if (inLobe(pos, heart, heartRadius + lobeRadius, verticalBand)) return true;
		for (BlockPos lobe : lobes) {
			if (inLobe(pos, lobe, lobeRadius * 2.0D, verticalBand)) return true;
		}
		return false;
	}

	/**
	 * How strongly {@code pos} is held, from 1 at {@code centre} falling by {@code falloff} at the envelope's widest
	 * reach. Containment is the caller's to check.
	 */
	public static double strength(BlockPos centre, List<BlockPos> lobes, BlockPos pos, double heartRadius,
			double lobeRadius, double falloff) {
		double max = maxRadius(centre, lobes, heartRadius, lobeRadius);
		if (max <= 0.0D) return 1.0D;
		double normalized = Math.max(0.0D, Math.min(1.0D, Math.sqrt(distanceSqr(pos, centre)) / max));
		return 1.0D - normalized * falloff;
	}

	/** The widest reach from {@code centre}: the heart's radius or the far edge of its farthest lobe. */
	public static double maxRadius(@Nullable BlockPos centre, List<BlockPos> lobes, double heartRadius,
			double lobeRadius) {
		if (centre == null) return 0.0D;
		double max = heartRadius;
		for (BlockPos lobe : lobes) {
			max = Math.max(max, Math.sqrt(distanceSqr(lobe, centre)) + lobeRadius);
		}
		return max;
	}

	/**
	 * The centre a lobe's chain runs from: the heart when the lobe touches the heart lobe, otherwise the earliest-raised
	 * lobe it touches that itself chains back to the heart. Null for a lobe cut off from the heart, or not in the list.
	 */
	@Nullable
	public static BlockPos chainedFrom(@Nullable BlockPos heart, List<BlockPos> lobes, BlockPos lobe,
			double heartRadius, double lobeRadius, int verticalBand) {
		if (heart == null || !lobes.contains(lobe)) return null;
		List<BlockPos> reached = new java.util.ArrayList<>();
		List<BlockPos> frontier = new java.util.ArrayList<>();
		for (BlockPos next : lobes) {
			if (inLobe(next, heart, heartRadius + lobeRadius, verticalBand)) frontier.add(next);
		}
		while (!frontier.isEmpty()) {
			BlockPos next = frontier.remove(frontier.size() - 1);
			if (reached.contains(next)) continue;
			reached.add(next);
			for (BlockPos other : lobes) {
				if (!reached.contains(other) && inLobe(other, next, lobeRadius * 2.0D, verticalBand)) frontier.add(other);
			}
		}
		if (!reached.contains(lobe)) return null;
		if (inLobe(lobe, heart, heartRadius + lobeRadius, verticalBand)) return heart;
		for (BlockPos other : lobes) {
			if (!other.equals(lobe) && reached.contains(other) && inLobe(lobe, other, lobeRadius * 2.0D, verticalBand)) {
				return other;
			}
		}
		return null;
	}

	/** Whether two lobes share a cell: their circles meet and their bands overlap. */
	public static boolean lobesTouch(BlockPos a, double radiusA, BlockPos b, double radiusB, int verticalBand) {
		double reach = radiusA + radiusB;
		return distanceSqr(a, b) <= reach * reach && withinBand(a, b, verticalBand == UNBOUNDED
				? UNBOUNDED : verticalBand * 2);
	}

	public static boolean inLobe(BlockPos pos, BlockPos centre, double radius, int verticalBand) {
		return distanceSqr(pos, centre) <= radius * radius && withinBand(pos, centre, verticalBand);
	}

	public static double distanceSqr(BlockPos pos, BlockPos centre) {
		double dx = pos.getX() + 0.5D - (centre.getX() + 0.5D);
		double dz = pos.getZ() + 0.5D - (centre.getZ() + 0.5D);
		return dx * dx + dz * dz;
	}

	private static boolean withinBand(BlockPos pos, BlockPos centre, int verticalBand) {
		return verticalBand == UNBOUNDED || Math.abs(pos.getY() - centre.getY()) <= verticalBand;
	}
}
