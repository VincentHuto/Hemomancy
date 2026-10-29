package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Where Seraphae's fight anchors stand. In her chamber they wake from the dormant anchor blocks round the tomb (the
 * nearest whole, unsnapped ones within reach); anywhere else, and for any the chamber lacks, they ring her as before.
 */
public final class SeraphaeAnchorRules {
	public static final int ANCHOR_COUNT = 4;
	/** How far from the tomb, across the floor, a block anchor can be and still wake. */
	public static final int HORIZONTAL_REACH = 8;
	/** How far above or below the tomb a block anchor can stand and still wake. */
	public static final int VERTICAL_REACH = 4;
	/** The fallback ring's radius round Seraphae, at her height. */
	public static final double RING_RADIUS = 10.0;

	/** One fight anchor: where it stands, and the block anchor it woke from ({@code null} for a ring anchor). */
	public record Placement(Vec3 position, @Nullable BlockPos home) {
	}

	private SeraphaeAnchorRules() {
	}

	/** True when a block anchor's lower half at {@code pos} is close enough to {@code centre} to wake. */
	public static boolean inReach(BlockPos centre, BlockPos pos) {
		int dx = pos.getX() - centre.getX(), dz = pos.getZ() - centre.getZ();
		return dx * dx + dz * dz <= HORIZONTAL_REACH * HORIZONTAL_REACH
				&& Math.abs(pos.getY() - centre.getY()) <= VERTICAL_REACH;
	}

	/**
	 * Up to {@link #ANCHOR_COUNT} of {@code candidates} (lower halves of whole, unsnapped anchor blocks) in reach of
	 * {@code centre}: nearest across the floor first, then nearest in height, then by position.
	 */
	public static List<BlockPos> wakeable(BlockPos centre, Collection<BlockPos> candidates) {
		Comparator<BlockPos> order = Comparator.<BlockPos>comparingInt(pos -> {
			int dx = pos.getX() - centre.getX(), dz = pos.getZ() - centre.getZ();
			return dx * dx + dz * dz;
		}).thenComparingInt(pos -> Math.abs(pos.getY() - centre.getY())).thenComparing(Comparator.naturalOrder());
		return candidates.stream().filter(pos -> inReach(centre, pos)).distinct().sorted(order).limit(ANCHOR_COUNT)
				.map(BlockPos::immutable).toList();
	}

	/**
	 * The fight's anchors: each woken block anchor stands on its block (centred on the cell, on its floor), and any
	 * shortfall takes the old ring slots round {@code seraphae} in order, so a Seraphae with no chamber gets the ring.
	 */
	public static List<Placement> placements(BlockPos centre, Vec3 seraphae, Collection<BlockPos> candidates) {
		List<Placement> placements = new ArrayList<>(ANCHOR_COUNT);
		for (BlockPos home : wakeable(centre, candidates)) {
			placements.add(new Placement(Vec3.atBottomCenterOf(home), home));
		}
		for (int slot = placements.size(); slot < ANCHOR_COUNT; slot++) {
			placements.add(new Placement(ringSlot(seraphae, slot), null));
		}
		return placements;
	}

	/** The old ring's {@code slot}th position: a quarter turn apart, {@link #RING_RADIUS} out, at her height. */
	public static Vec3 ringSlot(Vec3 seraphae, int slot) {
		double angle = slot * 2.0 * Math.PI / ANCHOR_COUNT;
		return new Vec3(seraphae.x + Math.cos(angle) * RING_RADIUS, seraphae.y, seraphae.z + Math.sin(angle) * RING_RADIUS);
	}
}
