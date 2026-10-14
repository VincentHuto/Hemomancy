package com.vincenthuto.hemomancy.client.render.world.floor;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The brazier trench's centre line: a closed curve through the sockets, in coordinates local to the Focus block. */
public final class GashLoop {
	/**
	 * Curve samples between neighbouring sockets along the gash. Its direction is blended linearly across each section,
	 * so the sections stay short enough that the trench walls do not crease at tight bends.
	 */
	public static final int GASH_STEPS = 28;
	/**
	 * Around a seated brazier the trench swings outward like a moat, so it runs on around the outside of the brazier's
	 * mound: by this much at the socket, falling away over the spread along the loop. Together they keep the trench's
	 * inner lip (about 0.37 inside its line) clear of the mound's faded rim at {@code SEAT_OUTER} all the way round.
	 */
	private static final float SEAT_DETOUR = 1.1F;
	private static final float SEAT_DETOUR_SPREAD = 1.3F;

	private final List<BlockPos> sockets;
	private final boolean[] seated;
	private final float[][] points;
	private final float[][] centre;
	private final Map<Long, Float> distances = new HashMap<>();

	private GashLoop(List<BlockPos> sockets, boolean[] seated, float[][] points, float[][] centre) {
		this.sockets = sockets;
		this.seated = seated;
		this.points = points;
		this.centre = centre;
	}

	/** The loop through these sockets, swung out around the seated ones, or null with fewer than three sockets. */
	public static GashLoop through(BlockPos origin, List<BlockPos> sockets, Set<BlockPos> seatedSockets) {
		if (sockets.size() < 3) return null;
		List<BlockPos> sorted = new ArrayList<>(sockets);
		sorted.sort(Comparator.comparingDouble(socket ->
				Math.atan2(socket.getZ() - origin.getZ(), socket.getX() - origin.getX())));
		int count = sorted.size();
		float[][] points = new float[count][];
		for (int i = 0; i < count; i++) {
			points[i] = new float[] {sorted.get(i).getX() - origin.getX() + 0.5F,
					sorted.get(i).getZ() - origin.getZ() + 0.5F};
		}
		int samples = count * GASH_STEPS;
		float[][] centre = new float[samples + 1][];
		for (int i = 0; i <= samples; i++) {
			int segment = (i / GASH_STEPS) % count;
			float t = (i % GASH_STEPS) / (float) GASH_STEPS;
			centre[i] = catmullRom(points[Math.floorMod(segment - 1, count)], points[segment],
					points[(segment + 1) % count], points[(segment + 2) % count], t);
		}
		boolean[] seated = new boolean[count];
		for (int i = 0; i < count; i++) seated[i] = seatedSockets.contains(sorted.get(i));
		// Swing the line outward around each seated brazier, so the trench becomes a moat around its mound.
		float[][] detoured = new float[samples + 1][];
		for (int i = 0; i <= samples; i++) {
			float x = centre[i][0];
			float z = centre[i][1];
			for (int seat = 0; seat < count; seat++) {
				if (!seated[seat]) continue;
				float ox = points[seat][0] - 0.5F;
				float oz = points[seat][1] - 0.5F;
				float length = (float) Math.sqrt(ox * ox + oz * oz);
				if (length < 1.0e-4F) continue;
				float dx = centre[i][0] - points[seat][0];
				float dz = centre[i][1] - points[seat][1];
				float weight = (float) Math.exp(-(dx * dx + dz * dz) / (SEAT_DETOUR_SPREAD * SEAT_DETOUR_SPREAD));
				x += ox / length * SEAT_DETOUR * weight;
				z += oz / length * SEAT_DETOUR * weight;
			}
			detoured[i] = new float[] {x, z};
		}
		detoured[samples] = detoured[0];
		return new GashLoop(List.copyOf(sorted), seated, points, detoured);
	}

	/** The sockets in loop order, sorted by angle around the Focus. */
	public List<BlockPos> sockets() {
		return sockets;
	}

	/** Whether the socket at each loop position holds a seated brazier. */
	public boolean[] seated() {
		return seated;
	}

	/** Socket centres in loop order, local to the Focus block. */
	public float[][] points() {
		return points;
	}

	/** The centre line's samples; the last repeats the first, closing the loop. */
	public float[][] centre() {
		return centre;
	}

	/** Positive inside the centre line, negative outside, in blocks. */
	public float signedDistance(float x, float z) {
		float nearest = Float.MAX_VALUE;
		boolean inside = false;
		for (int i = 0, j = centre.length - 2; i < centre.length - 1; j = i++) {
			float[] a = centre[i];
			float[] b = centre[j];
			if ((a[1] > z) != (b[1] > z) && x < (b[0] - a[0]) * (z - a[1]) / (b[1] - a[1]) + a[0]) inside = !inside;
			float dx = b[0] - a[0];
			float dz = b[1] - a[1];
			float lengthSq = dx * dx + dz * dz;
			float t = lengthSq < 1.0e-8F ? 0.0F
					: Math.max(0.0F, Math.min(1.0F, ((x - a[0]) * dx + (z - a[1]) * dz) / lengthSq));
			float ex = a[0] + dx * t - x;
			float ez = a[1] + dz * t - z;
			nearest = Math.min(nearest, ex * ex + ez * ez);
		}
		float distance = (float) Math.sqrt(nearest);
		return inside ? distance : -distance;
	}

	/** {@link #signedDistance}, remembered per point to a 1/256 block grid: the loop never moves. */
	public float distance(float x, float z) {
		long key = ((long) Math.round(x * 256.0F) << 32) ^ (Math.round(z * 256.0F) & 0xFFFFFFFFL);
		Float known = distances.get(key);
		if (known != null) return known;
		float measured = signedDistance(x, z);
		distances.put(key, measured);
		return measured;
	}

	/** Whether any corner of the block at this offset from the Focus lies inside the centre line. */
	public boolean reachesInside(int x, int z) {
		return distance(x, z) > -0.05F || distance(x + 1, z) > -0.05F
				|| distance(x, z + 1) > -0.05F || distance(x + 1, z + 1) > -0.05F;
	}

	/** Whether any corner of the block at this offset from the Focus lies outside the centre line. */
	public boolean reachesOutside(int x, int z) {
		return distance(x, z) < 0.05F || distance(x + 1, z) < 0.05F
				|| distance(x, z + 1) < 0.05F || distance(x + 1, z + 1) < 0.05F;
	}

	private static float[] catmullRom(float[] p0, float[] p1, float[] p2, float[] p3, float t) {
		float t2 = t * t;
		float t3 = t2 * t;
		float[] out = new float[2];
		for (int axis = 0; axis < 2; axis++) {
			out[axis] = 0.5F * (2.0F * p1[axis] + (-p0[axis] + p2[axis]) * t
					+ (2.0F * p0[axis] - 5.0F * p1[axis] + 4.0F * p2[axis] - p3[axis]) * t2
					+ (-p0[axis] + 3.0F * p1[axis] - 3.0F * p2[axis] + p3[axis]) * t3);
		}
		return out;
	}
}
