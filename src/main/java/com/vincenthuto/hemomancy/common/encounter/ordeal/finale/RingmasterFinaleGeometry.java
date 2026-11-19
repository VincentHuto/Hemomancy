package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * What the Ringmaster's rafter moves and the living carousel's hit (2026-11-18), as pure geometry: lanes, the string
 * of lights' curve, the horses' lanes and spiral, and the slam's rings. Read by the server for the hits and by the
 * client for where the horses and the phantom audience are drawn, so a drawn blow is a hit blow. Positions are feet;
 * every footprint lies on the ring's floor plane, and a standing body is hit up to {@link #STANDING} over it.
 */
public final class RingmasterFinaleGeometry {
	/** How far over the floor a footprint still reaches a body. */
	public static final double STANDING = 2.5D;
	/** The knives' lanes: 14 long, 0.6 either side. */
	public static final double VOLLEY_LANE = 14.0D, VOLLEY_HALF = 0.6D;
	/** The horses' volley lanes from the hub: 9 long, 1.4 either side. */
	public static final double HORSE_LANE = 9.0D, HORSE_HALF = 1.4D;
	/** The spiral: each horse runs from 2.4 out to 9 over 40 ticks, turning this many degrees a tick, hitting 1.2 round it. */
	public static final double SPIRAL_FROM = 2.4D, SPIRAL_TO = 9.0D, SPIRAL_DISC = 1.2D;
	public static final int SPIRAL_TICKS = 40;
	public static final float SPIRAL_TURN = 6.0F;
	/** The slam's two rings: each from 2 to 9 over 18 ticks, the second 9 ticks behind, a band 1 wide. */
	public static final double SLAM_FROM = 2.0D, SLAM_TO = 9.0D, SLAM_BAND = 1.0D;
	public static final int SLAM_TRAVEL = 18, SLAM_GAP = 9;

	private RingmasterFinaleGeometry() {
	}

	/** Whether feet stand in the floor lane from {@code from} to {@code to}, within {@code halfWidth} of its line. */
	public static boolean laneHits(Vec3 from, Vec3 to, double halfWidth, Vec3 feet) {
		double lx = to.x - from.x, lz = to.z - from.z;
		double length = Math.hypot(lx, lz);
		if (length < 1.0E-6D || !standing(from, feet)) return false;
		double dx = feet.x - from.x, dz = feet.z - from.z;
		double along = (dx * lx + dz * lz) / length, across = Math.abs(dx * lz - dz * lx) / length;
		return along >= 0.0D && along <= length && across <= halfWidth;
	}

	/** Whether feet stand on the curve: a run of floor lanes through its points, rounded at each joint. */
	public static boolean curveHits(List<Vec3> curve, double halfWidth, Vec3 feet) {
		for (int i = 0; i < curve.size(); i++) {
			if (i > 0 && laneHits(curve.get(i - 1), curve.get(i), halfWidth, feet)) return true;
			// The joints are round, so the outside of a bend has no gap between its two lanes.
			if (discHits(curve.get(i), halfWidth, feet)) return true;
		}
		return false;
	}

	/**
	 * A string of lights' path from {@code from} to {@code to}: a quadratic curve bowed {@code bend} blocks to the left
	 * of the straight line at its middle (negative bows right), {@code segments} pieces long.
	 */
	public static List<Vec3> curve(Vec3 from, Vec3 to, double bend, int segments) {
		Vec3 line = new Vec3(to.x - from.x, 0.0D, to.z - from.z);
		Vec3 side = line.lengthSqr() < 1.0E-6D ? Vec3.ZERO : new Vec3(-line.z, 0.0D, line.x).normalize();
		// The control point sits at twice the bow, so the curve's middle is bowed by exactly {@code bend}.
		Vec3 control = from.add(line.scale(0.5D)).add(side.scale(2.0D * bend));
		List<Vec3> points = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			double t = i / (double) segments, u = 1.0D - t;
			points.add(new Vec3(u * u * from.x + 2.0D * u * t * control.x + t * t * to.x, from.y,
					u * u * from.z + 2.0D * u * t * control.z + t * t * to.z));
		}
		return points;
	}

	/** The far ends of the carousel's three volley lanes, one through each horse at the carousel's {@code yaw}. */
	public static List<Vec3> horseLanes(Vec3 centre, float yaw) {
		List<Vec3> ends = new ArrayList<>(3);
		for (int horse = 0; horse < 3; horse++) ends.add(horseAt(centre, yaw, horse, HORSE_LANE));
		return ends;
	}

	/** Where horse {@code horse} stands at {@code reach} from the hub, the carousel turned to {@code yaw}. */
	public static Vec3 horseAt(Vec3 centre, float yaw, int horse, double reach) {
		CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(yaw, horse);
		double scale = reach / CircusCarouselRules.HORSE_RADIUS;
		return new Vec3(centre.x + pose.x() * scale, centre.y, centre.z + pose.z() * scale);
	}

	/** The carousel's turn that points horse 0 from {@code centre} at {@code target}. */
	public static float yawToward(Vec3 centre, Vec3 target) {
		float angle = (float) Math.toDegrees(Math.atan2(target.z - centre.z, target.x - centre.x));
		return 180.0F - angle;
	}

	/** How far out the spiralling horses are {@code ticks} into their sprint: 2.4 to 9 over 40 ticks, then held. */
	public static double spiralRadius(double ticks) {
		return HemoEasing.lerp(SPIRAL_FROM, SPIRAL_TO, HemoEasing.clamp01(ticks / SPIRAL_TICKS));
	}

	/** Where horse {@code horse} is {@code ticks} into the spiral that starts with the carousel at {@code yaw}. */
	public static Vec3 spiralPoint(Vec3 centre, float yaw, int horse, double ticks) {
		return horseAt(centre, yaw + SPIRAL_TURN * (float) ticks, horse, spiralRadius(ticks));
	}

	/** The radius of a slam ring {@code ticks} after it left the hub, or -1 before and after its run. */
	public static double slamRadius(double ticks) {
		if (ticks < 0.0D || ticks > SLAM_TRAVEL) return -1.0D;
		return HemoEasing.lerp(SLAM_FROM, SLAM_TO, ticks / SLAM_TRAVEL);
	}

	/** Whether a ring of {@code radius} and {@code band} round {@code centre} strikes feet that have not jumped it. */
	public static boolean ringHits(Vec3 centre, double radius, double band, Vec3 feet, double clearance) {
		double from = Math.hypot(feet.x - centre.x, feet.z - centre.z);
		return Math.abs(from - radius) <= band / 2.0D && feet.y - centre.y < clearance && feet.y >= centre.y - 0.5D;
	}

	/** Whether a floor disc of {@code radius} round {@code centre} strikes feet. */
	public static boolean discHits(Vec3 centre, double radius, Vec3 feet) {
		return Math.hypot(feet.x - centre.x, feet.z - centre.z) <= radius && standing(centre, feet);
	}

	/**
	 * Whether feet stand in a swing's band round {@code pivot}: within {@code band} of {@code radius} and inside the
	 * swept angle from {@code fromDeg} to {@code toDeg} (degrees from +x toward +z; {@code toDeg} may pass 360).
	 */
	public static boolean arcHits(Vec3 pivot, double radius, double band, float fromDeg, float toDeg, Vec3 feet) {
		double dx = feet.x - pivot.x, dz = feet.z - pivot.z;
		if (Math.abs(Math.hypot(dx, dz) - radius) > band / 2.0D || !standing(pivot, feet)) return false;
		double angle = Math.toDegrees(Math.atan2(dz, dx));
		double into = ((angle - fromDeg) % 360.0D + 360.0D) % 360.0D;
		return into <= toDeg - fromDeg;
	}

	/** Whether feet stand in a wedge from {@code origin} along the flat {@code facing}: {@code radius} long, {@code degrees} wide. */
	public static boolean wedgeHits(Vec3 origin, Vec3 facing, double radius, double degrees, Vec3 feet) {
		double dx = feet.x - origin.x, dz = feet.z - origin.z;
		double distance = Math.hypot(dx, dz);
		if (distance > radius || !standing(origin, feet)) return false;
		if (distance < 0.5D) return true;
		double fl = Math.hypot(facing.x, facing.z);
		if (fl < 1.0E-6D) return false;
		double cos = (dx * facing.x + dz * facing.z) / (distance * fl);
		return cos >= Math.cos(Math.toRadians(degrees / 2.0D));
	}

	/** A point on a swing's circle at {@code degrees} (from +x toward +z). */
	public static Vec3 onCircle(Vec3 pivot, double radius, double degrees) {
		double a = Math.toRadians(degrees);
		return new Vec3(pivot.x + Math.cos(a) * radius, pivot.y, pivot.z + Math.sin(a) * radius);
	}

	private static boolean standing(Vec3 floor, Vec3 feet) {
		return feet.y >= floor.y - 0.5D && feet.y - floor.y < STANDING;
	}
}
