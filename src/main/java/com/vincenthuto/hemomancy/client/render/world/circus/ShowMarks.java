package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Where the phantom show stands, read from the pavilion's own blocks round the player every two seconds: seats (seating
 * tiers and gallery decks), trapezes, the pole collars the string lights hang from, the gallery rail tops, and the ring.
 * Nearest first. Client-only.
 */
public record ShowMarks(List<Seat> seats, List<BlockPos> trapezes, List<Vec3> poleTops, List<List<Vec3>> railLines,
		Ring ring) {
	public static final ShowMarks EMPTY = new ShowMarks(List.of(), List.of(), List.of(), List.of(), Ring.NONE);

	/** A seat a phantom can take: its block, the yaw it looks along (radians, as {@code atan2(z, x)}), and if a deck. */
	public record Seat(BlockPos pos, float yaw, boolean deck) {
	}

	/** The ring: the mean of its curb blocks (on the curb's own level) and their mean distance from it. */
	public record Ring(Vec3 centre, double radius) {
		static final Ring NONE = new Ring(Vec3.ZERO, 0.0D);
	}

	public boolean hasRing() {
		return ring.radius() > 0.0D;
	}

	/** A fixed order for a pavilion's trapezes, by their positions alone, so who swings where never follows the player. */
	public static List<BlockPos> stableOrder(List<BlockPos> positions) {
		return positions.stream().sorted(java.util.Comparator.comparingLong(
				pos -> pos.asLong() * 0x9E3779B97F4A7C15L)).toList();
	}

	public static Ring ring(List<BlockPos> curb) {
		if (curb.isEmpty()) return Ring.NONE;
		double x = 0, y = 0, z = 0;
		for (BlockPos pos : curb) {
			x += pos.getX() + 0.5D;
			y += pos.getY();
			z += pos.getZ() + 0.5D;
		}
		Vec3 centre = new Vec3(x / curb.size(), y / curb.size(), z / curb.size());
		double radius = 0;
		for (BlockPos pos : curb) radius += Math.hypot(pos.getX() + 0.5D - centre.x, pos.getZ() + 0.5D - centre.z);
		return new Ring(centre, radius / curb.size());
	}
}
