package com.vincenthuto.hemomancy.common.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * The ground shape a boss move strikes, shared by the server hit test and the client telegraph. Every time-varying
 * shape exposes its motion as pure methods ({@link Lane#front}, {@link Sweep#angle}, {@link Ring#radius},
 * {@link Annulus#arcAngle}, {@link Path#walker}, {@link Union#live}) and {@code hits} is written only in terms of them,
 * so a drawn edge is a hit edge.
 * <p>
 * Points are world positions. Angles are degrees measured in the horizontal plane from the frame's aim direction,
 * positive toward the right hand (clockwise seen from above). {@code age} is ticks since the commit tick, negative
 * through the wind-up; a static shape ignores it.
 */
public sealed interface BossFootprint {
	/** Height band above the frame's floor; groundOnly also needs the target on the ground on the tested tick. */
	record Height(double min, double max, boolean groundOnly) {
		public static final Height STANDING = new Height(0.0D, 2.5D, false);
		/** The one jump clearance every jumpable wave uses (cross-Saint note B-9). */
		public static final Height JUMPABLE = new Height(0.0D, 0.9D, false);
		private static final double EPSILON = 1.0E-6D;

		/** Inclusive at both ends, with a hair of slack for the floor's float sum. */
		public boolean admits(double aboveFloor, boolean onGround) {
			return aboveFloor >= min - EPSILON && aboveFloor <= max + EPSILON && (!groundOnly || onGround);
		}
	}

	enum Anchor { ORIGIN, AIM, TARGET }

	enum Area { BOSS_FLOOR, CHAMBER, AROUND_ORIGIN }

	enum End { BODY, TARGET, ANCHOR }

	Height height();

	/** Planar test; age is ticks since the commit tick (negative through the wind-up). */
	boolean hits(FootprintFrame frame, Vec3 point, double age);

	/** Planar test plus the height band and ground flag. Occlusion needs the level: see {@link Occluded}. */
	default boolean hits(FootprintFrame frame, Vec3 feet, boolean onGround, double age) {
		return hits(frame, feet, age) && height().admits(feet.y - frame.floorY(), onGround);
	}

	/** The column key of a block cell, as {@link Region} exclusions store it. */
	static long cell(int x, int z) {
		return BlockPos.asLong(x, 0, z);
	}

	static long cellAt(Vec3 point) {
		return cell(Mth.floor(point.x), Mth.floor(point.z));
	}

	// ── the shapes ───────────────────────────────────────────────────

	/**
	 * Parallel lanes, or fanned when fanDegrees > 0; a front that runs out at frontSpeed blocks a tick (0 = all at
	 * once). Lane {@code i} runs from {@code -sideLanes} to {@code sideLanes}; a side lane starts {@code |i| * sideDelay}
	 * ticks after the centre. Everything between the feet and the front is live.
	 */
	record Lane(double length, double halfWidth, int sideLanes, double spacing, double fanDegrees,
			double frontSpeed, int sideDelay, Height height) implements BossFootprint {
		public int lanes() {
			return 2 * Math.max(0, sideLanes) + 1;
		}

		/** The lane's index from the centre for list position {@code n} in 0..lanes()-1. */
		public int index(int n) {
			return n - Math.max(0, sideLanes);
		}

		public int delay(int index) {
			return Math.abs(index) * Math.max(0, sideDelay);
		}

		/** Side offset of a parallel lane's centre line. */
		public double offset(int index) {
			return fanDegrees > 0.0D ? 0.0D : index * spacing;
		}

		/** Heading of a fanned lane, degrees from the aim. */
		public double angle(int index) {
			return fanDegrees > 0.0D ? index * fanDegrees : 0.0D;
		}

		/** How far lane {@code index}'s front has run at {@code age}; negative before it starts. */
		public double front(double age, int index) {
			double local = age - delay(index);
			if (local < 0.0D) return -1.0D;
			return frontSpeed <= 0.0D ? length : Math.min(length, frontSpeed * local);
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			for (int n = 0; n < lanes(); n++) {
				int index = index(n);
				double front = front(age, index);
				if (front < 0.0D) continue;
				double[] local = Shapes.local(frame, frame.origin(), point, angle(index));
				if (local[0] >= 0.0D && local[0] <= front && Math.abs(local[1] - offset(index)) <= halfWidth) return true;
			}
			return false;
		}
	}

	/** n radial spokes from the origin; the wedges between are safe. */
	record Spokes(int count, double halfWidth, double inner, double length, double offsetDegrees,
			double frontSpeed, Height height) implements BossFootprint {
		public double angle(int spoke) {
			return offsetDegrees + spoke * 360.0D / Math.max(1, count);
		}

		/** The spokes' outer reach at {@code age}: from {@code inner} out at frontSpeed, or all at once. */
		public double reach(double age) {
			if (frontSpeed <= 0.0D) return length;
			if (age < 0.0D) return inner;
			return Math.min(length, inner + frontSpeed * age);
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double reach = reach(age);
			for (int spoke = 0; spoke < count; spoke++) {
				double[] local = Shapes.local(frame, frame.origin(), point, angle(spoke));
				if (local[0] >= inner && local[0] <= reach && Math.abs(local[1]) <= halfWidth) return true;
			}
			return false;
		}
	}

	record Cone(double inner, double radius, double degrees, double offsetDegrees, Height height)
			implements BossFootprint {
		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double distance = Shapes.planar(frame.origin(), point);
			if (distance < inner || distance > radius) return false;
			if (distance < 1.0E-6D) return inner <= 0.0D;
			return Math.abs(Shapes.wrap(Shapes.bearing(frame, frame.origin(), point) - offsetDegrees)) <= degrees / 2.0D;
		}
	}

	/** A cone whose live band sweeps across it over sweepTicks of the active span. */
	record Sweep(double inner, double radius, double degrees, double bandDegrees, boolean clockwise,
			int sweepTicks, Height height) implements BossFootprint {
		public double progress(double age) {
			if (sweepTicks <= 0) return age < 0.0D ? 0.0D : 1.0D;
			return Mth.clamp(age / sweepTicks, 0.0D, 1.0D);
		}

		/** The live band's centre, degrees from the aim: from one edge of the cone to the other. */
		public double angle(double age) {
			double half = degrees / 2.0D;
			double travelled = progress(age) * degrees;
			return clockwise ? -half + travelled : half - travelled;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double distance = Shapes.planar(frame.origin(), point);
			if (distance < inner || distance > radius || distance < 1.0E-6D) return false;
			double bearing = Shapes.bearing(frame, frame.origin(), point);
			return Math.abs(bearing) <= degrees / 2.0D && Math.abs(Shapes.wrap(bearing - angle(age))) <= bandDegrees / 2.0D;
		}
	}

	/** waves expanding rings, waveGap ticks apart, each from {@code from} to {@code to} over travelTicks; band is the wave's thickness. */
	record Ring(double from, double to, double band, int waves, int waveGap, int travelTicks, Height height)
			implements BossFootprint {
		/** The wave's radius at {@code age}, or -1 while it has not started or has finished. */
		public double radius(double age, int wave) {
			double local = age - wave * (double) waveGap;
			if (local < 0.0D || local > travelTicks) return -1.0D;
			double t = travelTicks <= 0 ? 1.0D : local / travelTicks;
			return from + (to - from) * t;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double distance = Shapes.planar(frame.origin(), point);
			for (int wave = 0; wave < Math.max(1, waves); wave++) {
				double radius = radius(age, wave);
				if (radius >= 0.0D && Math.abs(distance - radius) <= band / 2.0D) return true;
			}
			return false;
		}
	}

	record Disc(Anchor at, double radius, Height height) implements BossFootprint {
		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			return Shapes.planar(frame.anchor(at), point) <= radius;
		}
	}

	/** A full annulus when arcs == 0, else that many arcs of arcDegrees turning {@code revolutions} times over activeTicks. */
	record Annulus(double inner, double outer, int arcs, double arcDegrees, double revolutions, int activeTicks,
			Height height) implements BossFootprint {
		/** Arc {@code arc}'s centre, degrees from the aim, at {@code age}. */
		public double arcAngle(double age, int arc) {
			double turn = activeTicks <= 0 ? 0.0D : Mth.clamp(age / activeTicks, 0.0D, 1.0D) * revolutions * 360.0D;
			return arc * 360.0D / Math.max(1, arcs) + turn;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double distance = Shapes.planar(frame.origin(), point);
			if (distance < inner || distance > outer) return false;
			if (arcs <= 0) return true;
			double bearing = Shapes.bearing(frame, frame.origin(), point);
			for (int arc = 0; arc < arcs; arc++) {
				if (Math.abs(Shapes.wrap(bearing - arcAngle(age, arc))) <= arcDegrees / 2.0D) return true;
			}
			return false;
		}
	}

	/** Two bars crossing at the anchor, one along the aim and one across it. */
	record Cross(Anchor at, double arm, double halfWidth, Height height) implements BossFootprint {
		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double[] local = Shapes.local(frame, frame.anchor(at), point, 0.0D);
			double along = Math.abs(local[0]), across = Math.abs(local[1]);
			return along <= arm && across <= halfWidth || across <= arm && along <= halfWidth;
		}
	}

	/** Discs at frame.points(), landing landingGap ticks apart in list order; a landed disc stays live. */
	record Discs(double radius, int landingGap, Height height) implements BossFootprint {
		public boolean landed(double age, int index) {
			return age >= index * (double) Math.max(0, landingGap);
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			List<Vec3> points = frame.points();
			for (int i = 0; i < points.size(); i++) {
				if (landed(age, i) && Shapes.planar(points.get(i), point) <= radius) return true;
			}
			return false;
		}
	}

	/**
	 * Segments through frame.points(). With walkers == 0 the whole path is live; otherwise that many fronts of
	 * frontLength travel along it at frontSpeed blocks a tick, spacing blocks apart.
	 */
	record Path(double halfWidth, double frontSpeed, double frontLength, int walkers, double spacing,
			Height height) implements BossFootprint {
		/** Distance along the path of walker {@code index}'s head at {@code age}; it may be negative or past the end. */
		public double walker(double age, int index) {
			return frontSpeed * Math.max(0.0D, age) - index * spacing;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			double[] along = Shapes.alongPath(frame.points(), point);
			if (along == null || along[1] > halfWidth) return false;
			if (walkers <= 0) return true;
			for (int index = 0; index < walkers; index++) {
				double head = walker(age, index);
				if (head < 0.0D) continue;
				if (along[0] <= head && along[0] >= head - frontLength) return true;
			}
			return false;
		}
	}

	/** The arena area named, minus the cells in frame.cells(); radius is used by AROUND_ORIGIN. */
	record Region(Area area, double radius, Height height) implements BossFootprint {
		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			boolean inside = switch (area) {
				case BOSS_FLOOR -> Shapes.planarInside(frame.bossFloor(), point);
				case CHAMBER -> Shapes.planarInside(frame.chamber(), point);
				case AROUND_ORIGIN -> Shapes.planar(frame.origin(), point) <= radius;
			};
			return inside && !frame.cells().contains(cellAt(point));
		}
	}

	/**
	 * A line between two ends re-read every tick (a chain to an anchor, a thread to the target). halfWidth 0 strikes
	 * only the target end; range breaks it (0 = never).
	 */
	record Tether(End from, End to, int anchor, double halfWidth, double range, Height height)
			implements BossFootprint {
		/** How close to the {@code to} end a point must be when halfWidth is 0. */
		public static final double END_REACH = 0.75D;

		public Vec3 end(FootprintFrame frame, End end) {
			return switch (end) {
				case BODY -> frame.origin();
				case TARGET -> frame.target();
				case ANCHOR -> frame.anchors().isEmpty() ? frame.origin()
						: frame.anchors().get(Mth.clamp(anchor, 0, frame.anchors().size() - 1));
			};
		}

		public boolean broken(FootprintFrame frame) {
			return range > 0.0D && Shapes.planar(end(frame, from), end(frame, to)) > range;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			if (broken(frame)) return false;
			Vec3 a = end(frame, from), b = end(frame, to);
			if (halfWidth <= 0.0D) return Shapes.planar(b, point) <= END_REACH;
			return Shapes.segmentDistance(a, b, point) <= halfWidth;
		}
	}

	/** Parts with their own delay after the commit; each part strikes a target separately. */
	record Union(List<Part> parts) implements BossFootprint {
		public record Part(BossFootprint shape, int delay) {
		}

		public Union {
			parts = List.copyOf(parts);
		}

		/** The parts that have started by {@code age}. */
		public List<Part> live(double age) {
			List<Part> live = new ArrayList<>();
			for (Part part : parts) if (age >= part.delay()) live.add(part);
			return live;
		}

		@Override
		public Height height() {
			return Height.STANDING;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			for (Part part : live(age)) if (part.shape().hits(frame, point, age - part.delay())) return true;
			return false;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 feet, boolean onGround, double age) {
			for (Part part : live(age)) if (part.shape().hits(frame, feet, onGround, age - part.delay())) return true;
			return false;
		}
	}

	/** Blocked when a block ray from the frame origin (at eye height) to the target's body centre is not clear. */
	record Occluded(BossFootprint shape) implements BossFootprint {
		@Override
		public Height height() {
			return shape.height();
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			return shape.hits(frame, point, age);
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 feet, boolean onGround, double age) {
			return shape.hits(frame, feet, onGround, age);
		}
	}

	/** No ground shape: wells, communions, the move's own logic decides who it touches. */
	record None() implements BossFootprint {
		public static final None INSTANCE = new None();

		@Override
		public Height height() {
			return Height.STANDING;
		}

		@Override
		public boolean hits(FootprintFrame frame, Vec3 point, double age) {
			return false;
		}
	}

	/** The plane geometry the shapes share. */
	final class Shapes {
		private Shapes() {
		}

		/** Horizontal distance between two points. */
		public static double planar(Vec3 a, Vec3 b) {
			double dx = b.x - a.x, dz = b.z - a.z;
			return Math.sqrt(dx * dx + dz * dz);
		}

		/** The frame's aim as a horizontal unit vector {x, z}; straight south when the aim sits on the origin. */
		public static double[] forward(FootprintFrame frame) {
			double dx = frame.aim().x - frame.origin().x, dz = frame.aim().z - frame.origin().z;
			double length = Math.sqrt(dx * dx + dz * dz);
			if (length < 1.0E-6D) return new double[]{0.0D, 1.0D};
			return new double[]{dx / length, dz / length};
		}

		/** The aim's heading in degrees, Minecraft yaw style: 0 south, 90 west. */
		public static double aimYaw(FootprintFrame frame) {
			double[] f = forward(frame);
			return Math.toDegrees(Math.atan2(-f[0], f[1]));
		}

		/** {along, right} of {@code point} from {@code centre} in the aim's frame turned by {@code turnDegrees}. */
		public static double[] local(FootprintFrame frame, Vec3 centre, Vec3 point, double turnDegrees) {
			double[] f = rotate(forward(frame), turnDegrees);
			double rx = -f[1], rz = f[0];
			double dx = point.x - centre.x, dz = point.z - centre.z;
			return new double[]{dx * f[0] + dz * f[1], dx * rx + dz * rz};
		}

		/** A horizontal unit vector turned toward the right hand by {@code degrees}. */
		public static double[] rotate(double[] forward, double degrees) {
			if (degrees == 0.0D) return forward;
			double rad = Math.toRadians(degrees), c = Math.cos(rad), s = Math.sin(rad);
			double rx = -forward[1], rz = forward[0];
			return new double[]{forward[0] * c + rx * s, forward[1] * c + rz * s};
		}

		/** Degrees from the aim to {@code point} seen from {@code centre}, in (-180, 180], positive to the right. */
		public static double bearing(FootprintFrame frame, Vec3 centre, Vec3 point) {
			double[] local = local(frame, centre, point, 0.0D);
			return Math.toDegrees(Math.atan2(local[1], local[0]));
		}

		/** The point on the plane at {@code distance} from {@code centre}, {@code degrees} from the aim. */
		public static Vec3 at(FootprintFrame frame, Vec3 centre, double degrees, double distance) {
			double[] f = rotate(forward(frame), degrees);
			return new Vec3(centre.x + f[0] * distance, centre.y, centre.z + f[1] * distance);
		}

		public static double wrap(double degrees) {
			double wrapped = degrees % 360.0D;
			if (wrapped > 180.0D) wrapped -= 360.0D;
			if (wrapped <= -180.0D) wrapped += 360.0D;
			return wrapped;
		}

		public static double segmentDistance(Vec3 a, Vec3 b, Vec3 point) {
			double dx = b.x - a.x, dz = b.z - a.z;
			double lengthSq = dx * dx + dz * dz;
			double t = lengthSq < 1.0E-9D ? 0.0D
					: Mth.clamp(((point.x - a.x) * dx + (point.z - a.z) * dz) / lengthSq, 0.0D, 1.0D);
			double cx = a.x + dx * t - point.x, cz = a.z + dz * t - point.z;
			return Math.sqrt(cx * cx + cz * cz);
		}

		/** {distance along the polyline to the nearest point, distance off it}, or null with fewer than two points. */
		public static double[] alongPath(List<Vec3> points, Vec3 point) {
			if (points.size() < 2) return null;
			double best = Double.MAX_VALUE, bestAlong = 0.0D, travelled = 0.0D;
			for (int i = 0; i + 1 < points.size(); i++) {
				Vec3 a = points.get(i), b = points.get(i + 1);
				double dx = b.x - a.x, dz = b.z - a.z;
				double length = Math.sqrt(dx * dx + dz * dz);
				double t = length < 1.0E-9D ? 0.0D
						: Mth.clamp(((point.x - a.x) * dx + (point.z - a.z) * dz) / (length * length), 0.0D, 1.0D);
				double cx = a.x + dx * t - point.x, cz = a.z + dz * t - point.z;
				double off = Math.sqrt(cx * cx + cz * cz);
				if (off < best) {
					best = off;
					bestAlong = travelled + t * length;
				}
				travelled += length;
			}
			return new double[]{bestAlong, best};
		}

		public static double pathLength(List<Vec3> points) {
			double total = 0.0D;
			for (int i = 0; i + 1 < points.size(); i++) total += planar(points.get(i), points.get(i + 1));
			return total;
		}

		/** The point {@code distance} along the polyline, clamped to its ends. */
		public static Vec3 pointAlong(List<Vec3> points, double distance) {
			if (points.isEmpty()) return Vec3.ZERO;
			double left = Math.max(0.0D, distance);
			for (int i = 0; i + 1 < points.size(); i++) {
				Vec3 a = points.get(i), b = points.get(i + 1);
				double length = planar(a, b);
				if (left <= length || i + 2 == points.size()) {
					double t = length < 1.0E-9D ? 0.0D : Math.min(1.0D, left / length);
					return a.lerp(b, t);
				}
				left -= length;
			}
			return points.get(points.size() - 1);
		}

		public static boolean planarInside(AABB box, Vec3 point) {
			return point.x >= box.minX && point.x <= box.maxX && point.z >= box.minZ && point.z <= box.maxZ;
		}
	}
}
