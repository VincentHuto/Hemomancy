package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import java.util.ArrayList;
import java.util.List;

/**
 * Where the Spiked Halo flies, in the move's own frame ({along the locked aim, to its right}, blocks from his feet):
 * straight out 10 blocks, then back along an arc bowed 3 blocks to his staff side (his left), at one speed over the
 * active span. The server's footprint (a {@code Path} through these points with one walker) and the model's halo read
 * the same numbers, so the drawn halo is where the hit is. JVM-tested.
 */
public final class HemorathHaloPath {
	public static final double OUT = 10.0D;
	/** The arc's bow, negative: toward his left hand, where the staff is. */
	public static final double BOW = -3.0D;
	/** The return ends this far in front of him, where he catches it. */
	public static final double CATCH = 1.0D;
	public static final int ARC_SAMPLES = 10;
	/** The active span the halo flies through. */
	public static final int FLIGHT_TICKS = 34;
	/** The halo's reach either side of its line, and the length of its live front. */
	public static final double HALF_WIDTH = 1.0D;
	public static final double FRONT = 1.5D;
	/** Above the floor: under it you are safe only flat on the ground, which no one is; over it, a jump clears. */
	public static final double LOW = 0.4D;
	public static final double HIGH = 2.2D;

	private static final List<double[]> POINTS = build();
	private static final double LENGTH = length(POINTS);

	private HemorathHaloPath() {
	}

	/** The polyline, {along, right} pairs, out then back. */
	public static List<double[]> points() {
		return POINTS;
	}

	public static double length() {
		return LENGTH;
	}

	/** Blocks a tick the halo flies. */
	public static double speed() {
		return LENGTH / FLIGHT_TICKS;
	}

	/** The halo's {along, right} at {@code age} ticks after the throw, clamped to the path's ends. */
	public static double[] at(double age) {
		double distance = Math.max(0.0D, Math.min(LENGTH, age * speed()));
		for (int i = 0; i + 1 < POINTS.size(); i++) {
			double[] a = POINTS.get(i), b = POINTS.get(i + 1);
			double segment = Math.hypot(b[0] - a[0], b[1] - a[1]);
			if (distance <= segment || i + 2 == POINTS.size()) {
				double t = segment < 1.0E-9D ? 0.0D : Math.min(1.0D, distance / segment);
				return new double[]{a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t};
			}
			distance -= segment;
		}
		return POINTS.get(POINTS.size() - 1).clone();
	}

	/** True once the halo has turned for home. */
	public static boolean returning(double age) {
		return age * speed() > OUT;
	}

	private static List<double[]> build() {
		List<double[]> points = new ArrayList<>();
		points.add(new double[]{0.0D, 0.0D});
		points.add(new double[]{OUT, 0.0D});
		for (int i = 1; i <= ARC_SAMPLES; i++) {
			double u = (double) i / ARC_SAMPLES;
			double along = OUT + (CATCH - OUT) * u;
			points.add(new double[]{along, BOW * Math.sin(Math.PI * u)});
		}
		return List.copyOf(points);
	}

	private static double length(List<double[]> points) {
		double total = 0.0D;
		for (int i = 0; i + 1 < points.size(); i++) {
			total += Math.hypot(points.get(i + 1)[0] - points.get(i)[0], points.get(i + 1)[1] - points.get(i)[1]);
		}
		return total;
	}
}
