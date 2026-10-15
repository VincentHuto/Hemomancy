package com.vincenthuto.hemomancy.client.render.world.floor;

/**
 * Where a consecrated floor's membrane ends and how its braziers are wired in. The trench runs as a rounded frame
 * just inside the floor's own edge, so the membrane fits the blocks the player laid instead of needing level ground
 * out to the brazier ring. Each seated brazier beyond it is joined to the floor by a root that leaves the rim facing
 * it, arches across whatever gap lies between, and dives into the brazier's mound. Coordinates are local to the Focus
 * block (its centre at 0.5, 0.5), like {@link GashLoop}'s. Pure maths so it can be tested without a client.
 */
public final class FloorRimRules {
	/** How far inside the floor's outer edge the trench's centre line runs, so its strip stays on the floor. */
	public static final float EDGE_INSET = 0.85F;
	/** Squareness of the frame: higher is squarer, 2 would be a circle. */
	private static final double SQUARENESS = 6.0D;

	private FloorRimRules() {
	}

	/** The trench's centre line round a floor {@code half} blocks from Focus to edge: {@code samples + 1} points, closed. */
	public static float[][] rimLoop(int half, int samples) {
		float reach = half + 0.5F - EDGE_INSET;
		float[][] loop = new float[samples + 1][];
		for (int i = 0; i < samples; i++) {
			double angle = Math.PI * 2.0D * i / samples;
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			loop[i] = new float[] {0.5F + (float) (reach * squircle(cos)), 0.5F + (float) (reach * squircle(sin))};
		}
		loop[samples] = loop[0];
		return loop;
	}

	/** The point on the rim facing a brazier socket centred at ({@code socketX}, {@code socketZ}). */
	public static float[] rimPoint(int half, float socketX, float socketZ) {
		float reach = half + 0.5F - EDGE_INSET;
		double dx = socketX - 0.5D;
		double dz = socketZ - 0.5D;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0e-6D) return new float[] {0.5F + reach, 0.5F};
		dx /= length;
		dz /= length;
		// Along this heading the frame lies where the squircle's level set reaches 1.
		double scale = reach / Math.pow(Math.pow(Math.abs(dx), SQUARENESS) + Math.pow(Math.abs(dz), SQUARENESS),
				1.0D / SQUARENESS);
		return new float[] {0.5F + (float) (dx * scale), 0.5F + (float) (dz * scale)};
	}

	/**
	 * A root's spine from {@code start} (on the rim, at the membrane) to {@code end} (in the brazier's mound): it lifts
	 * out of the membrane, arches over the gap and dives into the mound. Points are {x, y, z}.
	 */
	public static float[][] rootPath(float[] start, float[] end, int samples) {
		float dx = end[0] - start[0];
		float dz = end[2] - start[2];
		float span = (float) Math.sqrt(dx * dx + dz * dz);
		float lift = 0.22F + 0.1F * span;
		float[] c1 = {start[0] + dx * 0.3F, start[1] + lift, start[2] + dz * 0.3F};
		float[] c2 = {start[0] + dx * 0.75F, Math.max(start[1], end[1]) + lift, start[2] + dz * 0.75F};
		float[][] path = new float[samples + 1][];
		for (int i = 0; i <= samples; i++) {
			float t = i / (float) samples;
			float u = 1.0F - t;
			float a = u * u * u;
			float b = 3.0F * u * u * t;
			float c = 3.0F * u * t * t;
			float d = t * t * t;
			path[i] = new float[] {
					a * start[0] + b * c1[0] + c * c2[0] + d * end[0],
					a * start[1] + b * c1[1] + c * c2[1] + d * end[1],
					a * start[2] + b * c1[2] + c * c2[2] + d * end[2]};
		}
		return path;
	}

	/** Maps a unit circle's coordinate onto a squircle's: flattens the sides, rounds the corners. */
	private static double squircle(double value) {
		return Math.signum(value) * Math.pow(Math.abs(value), 2.0D / SQUARENESS);
	}
}
