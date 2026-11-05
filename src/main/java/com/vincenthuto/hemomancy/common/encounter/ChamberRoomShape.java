package com.vincenthuto.hemomancy.common.encounter;

import java.util.ArrayList;
import java.util.List;

/**
 * The Chamber of Will's room: a keyhole. A circular platform with the Arbor of Will at its centre, and one corridor
 * running out along +Z to the gate. Everything that asks how big the room is (the floor, block placement, movement,
 * the rescue check, the border aura) reads this one shape. Offsets are from the cell centre; a block offset names the
 * block whose centre lies there.
 */
public final class ChamberRoomShape {
	private ChamberRoomShape() {
	}

	/** How far the corridor's walkway reaches either side of its centre line, in blocks. */
	public static int halfWidth(int radius) {
		return 1 + Math.round((radius - 3) * 3.0F / 7.0F);
	}

	/** How far the corridor runs beyond the circle's edge. */
	public static int length(int radius) {
		return radius + 4;
	}

	/** The block row of the corridor's far end, where the gate stands. */
	public static int reach(int radius) {
		return radius + length(radius);
	}

	private static boolean inCircle(double dx, double dz, int radius) {
		double limit = radius + 0.5D;
		return dx * dx + dz * dz <= limit * limit;
	}

	/** Whether the floor of a room of this radius holds this block. */
	public static boolean contains(int dx, int dz, int radius) {
		if (radius < 0) return false;
		return inCircle(dx, dz, radius) || (Math.abs(dx) <= halfWidth(radius) && dz >= 0 && dz <= reach(radius));
	}

	/** The floor block offset under a point: block {@code k} covers {@code k - 0.5} to {@code k + 0.5}. */
	private static int blockOf(double offset) {
		return (int) Math.floor(offset + 0.5D);
	}

	/**
	 * The nearest point a body may stand: unchanged over the floor, else on the edge of the nearest floor block, so the
	 * bound follows the floor's stepped outline and never holds anyone over a gap in it. The gate's plane is the far end.
	 */
	public static Point clamp(double dx, double dz, int radius) {
		int reach = reach(radius);
		double heldZ = Math.min(dz, reach);
		if (contains(blockOf(dx), blockOf(heldZ), radius)) return new Point(dx, heldZ);
		Point best = new Point(0.0D, 0.0D);
		double bestDistance = Double.MAX_VALUE;
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= reach; z++) {
				if (!contains(x, z, radius)) continue;
				Point onBlock = new Point(Math.max(x - 0.5D, Math.min(x + 0.5D, dx)),
						Math.min(reach, Math.max(z - 0.5D, Math.min(z + 0.5D, dz))));
				double distance = distanceSquared(onBlock, dx, dz);
				if (distance < bestDistance) {
					bestDistance = distance;
					best = onBlock;
				}
			}
		}
		return best;
	}

	/** Whether a body is within {@code margin} of the room; beyond that it has fallen out of it. */
	public static boolean near(double dx, double dz, int radius, double margin) {
		return distanceSquared(clamp(dx, dz, radius), dx, dz) <= margin * margin;
	}

	private static double distanceSquared(Point point, double dx, double dz) {
		double x = point.dx() - dx;
		double z = point.dz() - dz;
		return x * x + z * z;
	}

	/**
	 * The room's edge as one line: from the far end of one corridor wall, up it, around the circle's north side, and
	 * back down the other wall. It is open at the gate.
	 */
	public static List<Point> outline(int radius, int arcSegments) {
		double wall = halfWidth(radius) + 0.5D;
		double limit = radius + 0.5D;
		double join = Math.sqrt(limit * limit - wall * wall);
		double startAngle = Math.atan2(join, wall);
		double sweep = Math.PI * 2.0D - (Math.PI - 2.0D * startAngle);
		List<Point> points = new ArrayList<>();
		points.add(new Point(wall, reach(radius)));
		for (int i = 0; i <= arcSegments; i++) {
			double angle = startAngle - sweep * i / arcSegments;
			points.add(new Point(Math.cos(angle) * limit, Math.sin(angle) * limit));
		}
		points.add(new Point(-wall, reach(radius)));
		return points;
	}

	public record Point(double dx, double dz) {
	}
}
