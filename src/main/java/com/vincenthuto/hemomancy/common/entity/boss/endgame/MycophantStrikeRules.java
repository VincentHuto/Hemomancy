package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import java.util.Random;

/**
 * Where each of her blows lands and when: hit shapes (cones, the rear arc, circles, the hand sweep), string, tail and
 * glob timelines, the mortar's spread and lob, the lance line and the Gaze's sweep. Offsets are horizontal (dx, dz)
 * from her centre, and yaws follow Minecraft's convention: yaw 0 faces +z and a facing is (-sin yaw, cos yaw).
 */
public final class MycophantStrikeRules {
	/** Ticks a lance spot cracks amber before it erupts. */
	public static final int LANCE_WARN_TICKS = 10;
	/** Ticks the Gaze takes to cross its whole sweep. */
	public static final int GAZE_SWEEP_TICKS = 40;
	/** The Gaze's sweep, end to end, in degrees. */
	public static final float GAZE_SWEEP_DEG = 100.0F;
	/** How far either side of the beam's line the Gaze still catches the player. */
	public static final double GAZE_HALF_WIDTH_DEG = 6.0;
	/** How far the Gaze reaches from the pulp; the client draws the beam out to the same reach. */
	public static final double GAZE_RANGE = 32.0;

	private static final double SWEEP_INNER = 4.0;
	private static final double SWEEP_OUTER = 9.0;
	private static final double SWEEP_HALF_ANGLE = 60.0;
	private static final double SWEEP_CLEARANCE = 0.8;
	/** Boundary slack so a point exactly on an edge counts as inside despite trigonometric rounding. */
	private static final double EDGE = 1.0E-6;

	private MycophantStrikeRules() {
	}

	/** The yaw that faces along the offset (dx, dz), in [-180, 180). */
	public static float bearing(double dx, double dz) {
		return (float) wrap(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
	}

	/** Degrees, 0..180, between facing {@code yawDeg} and the offset (dx, dz). Zero for a zero offset. */
	public static double angleFromFacing(double dx, double dz, float yawDeg) {
		if (dx * dx + dz * dz < 1.0E-12) return 0.0;
		return Math.abs(wrap(Math.toDegrees(Math.atan2(dz, dx)) - 90.0 - yawDeg));
	}

	/** Within {@code radius} and within {@code halfAngleDeg} of where she faces. */
	public static boolean inCone(double dx, double dz, float yawDeg, double radius, double halfAngleDeg) {
		return inCircle(dx, dz, radius) && angleFromFacing(dx, dz, yawDeg) <= halfAngleDeg + EDGE;
	}

	/** Within {@code radius} and inside an arc {@code arcDeg} wide centred on her rear. */
	public static boolean inRearArc(double dx, double dz, float yawDeg, double radius, double arcDeg) {
		return inCircle(dx, dz, radius) && angleFromFacing(dx, dz, yawDeg) >= 180.0 - arcDeg * 0.5 - EDGE;
	}

	public static boolean inCircle(double dx, double dz, double radius) {
		return Math.sqrt(dx * dx + dz * dz) <= radius + EDGE;
	}

	/** The Hand Sweep: 4 to 9 blocks out across the front 120 degrees. Closing in under it is safe. */
	public static boolean inSweep(double dx, double dz, float yawDeg) {
		double distance = Math.sqrt(dx * dx + dz * dz);
		return distance >= SWEEP_INNER - EDGE && distance <= SWEEP_OUTER + EDGE
				&& angleFromFacing(dx, dz, yawDeg) <= SWEEP_HALF_ANGLE + EDGE;
	}

	/** The sweep runs along the floor: feet at least 0.8 blocks above the ground when it passes are missed. */
	public static boolean clearsSweep(double feetAboveGround) {
		return feetAboveGround >= SWEEP_CLEARANCE;
	}

	/**
	 * The Rake String's strike ticks, relative to the start of its active window. Variant 0 is two rakes; variant 1
	 * adds the delayed overhead slam (a little quicker in phase 2); variant 2 (phase 2) rakes three times before it.
	 */
	public static int[] stringHits(int variant, int phase) {
		if (variant >= 2) return new int[] { 0, 7, 14, 26 };
		if (variant == 1) return phase >= 2 ? new int[] { 0, 7, 18 } : new int[] { 0, 8, 20 };
		return new int[] { 0, 8 };
	}

	/** The last blow of a variant 1 or 2 string is the slam (a circle at the claw tip, not a cone). */
	public static boolean isSlam(int variant, int index, int hits) {
		return variant >= 1 && index == hits - 1;
	}

	/** The Tail Lash's strike ticks: one swing, or back and forth (variant 1, phase 2). */
	public static int[] tailHits(int variant) {
		return variant >= 1 ? new int[] { 0, 10 } : new int[] { 0 };
	}

	/** The Siphon Spit's release ticks: three globs, five in phase 2. */
	public static int[] globTicks(int phase) {
		return phase >= 2 ? new int[] { 0, 3, 6, 9, 12 } : new int[] { 0, 4, 8 };
	}

	/**
	 * Landing points for a volley of {@code count}. Entry 0 is the target itself; the rest sit on a ring around it
	 * between {@code minRadius} and {@code maxRadius}, evenly spaced in angle from a seeded start, each nudged by up to
	 * a quarter of the spacing. The same seed gives the same points.
	 */
	public static double[][] spread(double tx, double tz, int count, long seed, double minRadius, double maxRadius) {
		if (count <= 0) return new double[0][];
		double[][] points = new double[count][];
		points[0] = new double[] { tx, tz };
		int ring = count - 1;
		if (ring == 0) return points;
		Random random = new Random(seed);
		double step = Math.PI * 2.0 / ring;
		double start = random.nextDouble() * Math.PI * 2.0;
		for (int i = 0; i < ring; i++) {
			double angle = start + step * i + (random.nextDouble() - 0.5) * step * 0.5;
			double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
			points[i + 1] = new double[] { tx + Math.cos(angle) * radius, tz + Math.sin(angle) * radius };
		}
		return points;
	}

	/** Air time for a mortar egg, so far shots hang longer and the landing rings stay up for at least 14 ticks. */
	public static int mortarFlightTicks(double horizontalDistance) {
		return Math.max(14, (int) Math.round(8.0 + horizontalDistance * 1.1));
	}

	/**
	 * The launch velocity (x, y, z per tick) that lands on the offset after {@code flightTicks}, for a projectile with
	 * no drag that moves by its velocity and then loses {@code gravity} from its vertical speed each tick.
	 */
	public static double[] lobVelocity(double dx, double dy, double dz, int flightTicks, double gravity) {
		int n = Math.max(1, flightTicks);
		return new double[] { dx / n, (dy + gravity * n * (n - 1) * 0.5) / n, dz / n };
	}

	/**
	 * The Hyphae Lances' eruption points, {@code spacing} apart from the origin toward the target out to
	 * {@code length}; the first sits one spacing out. Empty when the target is on the origin (no direction).
	 */
	public static double[][] lanceLine(double ox, double oz, double tx, double tz, double length, double spacing) {
		double dx = tx - ox;
		double dz = tz - oz;
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance < 1.0E-6 || spacing <= 0.0) return new double[0][];
		int count = (int) Math.floor(length / spacing + 1.0E-9);
		double[][] points = new double[count][];
		for (int i = 0; i < count; i++) {
			double along = spacing * (i + 1);
			points[i] = new double[] { ox + dx / distance * along, oz + dz / distance * along };
		}
		return points;
	}

	/** Ticks after the first lance before lance {@code index} starts cracking. */
	public static int lanceDelay(int index) {
		return 3 * index;
	}

	/**
	 * The beam's yaw {@code sweepTick} ticks into the Gaze: it starts 50 degrees off the quarry's bearing at commit
	 * ({@code startYawDeg}) and crosses to 50 degrees past it over {@link #GAZE_SWEEP_TICKS}. {@code direction} (+1 or
	 * -1, by variant) picks the side it starts from. Takes fractional ticks for the client.
	 */
	public static float gazeYaw(float startYawDeg, int direction, float sweepTick) {
		float t = Math.max(0.0F, Math.min(1.0F, sweepTick / GAZE_SWEEP_TICKS));
		float half = GAZE_SWEEP_DEG * 0.5F;
		return startYawDeg - half * direction + GAZE_SWEEP_DEG * direction * t;
	}

	/** The beam catches an offset within 6 degrees of its line and inside {@code maxRange}. Cover is tested apart. */
	public static boolean gazeHits(float beamYawDeg, double dx, double dz, double maxRange) {
		return inCone(dx, dz, beamYawDeg, maxRange, GAZE_HALF_WIDTH_DEG);
	}

	/** The beam's raycast aims at the body centre, not the eyes, so a two-block tumor hides a standing player. */
	public static double gazeTargetHeight(double bbHeight) {
		return bbHeight * 0.5;
	}

	/** Creative and spectator players are never pinned, netted, cocooned, pulled or hit by the beam. */
	public static boolean onlySurvival(boolean spectator, boolean creative) {
		return !spectator && !creative;
	}

	private static double wrap(double degrees) {
		double d = (degrees + 180.0) % 360.0;
		if (d < 0.0) d += 360.0;
		return d - 180.0;
	}
}
