package com.vincenthuto.hemomancy.client.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Client memory of the burrow fissure: recent trail points, each fading out over a few seconds. */
public final class VesperBurrowClientData {
	public static final int FADE_TICKS = 100;
	private static final int MAX_POINTS = 160;
	private static final double MIN_SPACING_SQR = 0.09D;
	private static final Deque<Point> POINTS = new ArrayDeque<>();
	private static float shakeAmplitude;

	public record Point(double x, double y, double z, long gameTime, boolean breakSegment) {
	}

	private VesperBurrowClientData() {
	}

	/** Records the trail; a jump of more than two blocks starts a new crack instead of slicing across the floor. */
	public static void record(double x, double y, double z, long gameTime) {
		Point last = POINTS.peekLast();
		if (last != null) {
			double dx = x - last.x();
			double dz = z - last.z();
			double distance = dx * dx + dz * dz;
			if (distance < MIN_SPACING_SQR) return;
			POINTS.addLast(new Point(x, y, z, gameTime, distance > 4.0D || gameTime - last.gameTime() > 4));
		} else {
			POINTS.addLast(new Point(x, y, z, gameTime, true));
		}
		while (POINTS.size() > MAX_POINTS) POINTS.removeFirst();
	}

	public static void expire(long gameTime) {
		while (!POINTS.isEmpty() && gameTime - POINTS.peekFirst().gameTime() > FADE_TICKS) POINTS.removeFirst();
	}

	public static List<Point> points() {
		return new ArrayList<>(POINTS);
	}

	public static float shakeAmplitude() {
		return shakeAmplitude;
	}

	public static void setShakeAmplitude(float amplitude) {
		shakeAmplitude = amplitude;
	}

	public static void clear() {
		POINTS.clear();
		shakeAmplitude = 0.0F;
	}
}
