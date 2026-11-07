package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

/**
 * Where one of Seraphae's limbs (a hand, a nerve trunk's tip fan) was in the world, frame by frame, so a slash, a reach
 * trail or a lash ribbon follows the very path the animation drew instead of a guess at it. The renderer records a
 * point each frame with the body's render age; a reader asks for any age inside the kept frames and gets the point
 * interpolated between the two frames round it. About a second of frames at 60 a second. Pure, JVM-tested.
 */
public final class SeraphaeLimbTrail {
	public static final int FRAMES = 64;
	/** A read this far past the newest frame still returns it (a frame not yet drawn this tick). */
	public static final float LEAD = 0.5F;

	private final float[] ages = new float[FRAMES];
	private final double[] xs = new double[FRAMES], ys = new double[FRAMES], zs = new double[FRAMES];
	private int head, count;

	/**
	 * Keeps the limb at {@code (x, y, z)} at render age {@code age}. A frame at or before the newest kept one is the same
	 * frame drawn again and is skipped; an age far behind it (the body was reloaded) starts the trail over.
	 */
	public void record(float age, double x, double y, double z) {
		if (count > 0) {
			float last = ages[Math.floorMod(head - 1, FRAMES)];
			if (age < last - 1.0F) count = 0;
			else if (age <= last) return;
		}
		ages[head] = age;
		xs[head] = x;
		ys[head] = y;
		zs[head] = z;
		head = (head + 1) % FRAMES;
		count = Math.min(FRAMES, count + 1);
	}

	/**
	 * Writes where the limb was at {@code age} into {@code out} (x, y, z) and returns true; false outside the kept frames
	 * or with fewer than two of them.
	 */
	public boolean at(float age, double[] out) {
		if (count < 2) return false;
		int newest = Math.floorMod(head - 1, FRAMES);
		if (age > ages[newest] + LEAD) return false;
		if (age >= ages[newest]) {
			copy(newest, out);
			return true;
		}
		for (int i = 1; i < count; i++) {
			int older = Math.floorMod(newest - i, FRAMES), newer = Math.floorMod(older + 1, FRAMES);
			if (ages[older] <= age) {
				float span = ages[newer] - ages[older];
				double t = span <= 1.0E-4F ? 1.0D : (age - ages[older]) / span;
				out[0] = xs[older] + (xs[newer] - xs[older]) * t;
				out[1] = ys[older] + (ys[newer] - ys[older]) * t;
				out[2] = zs[older] + (zs[newer] - zs[older]) * t;
				return true;
			}
		}
		return false;
	}

	/** The newest kept age, or NaN when empty. */
	public float newest() {
		return count == 0 ? Float.NaN : ages[Math.floorMod(head - 1, FRAMES)];
	}

	/** The oldest kept age, or NaN when empty. */
	public float oldest() {
		return count == 0 ? Float.NaN : ages[Math.floorMod(head - count, FRAMES)];
	}

	public int size() {
		return count;
	}

	private void copy(int index, double[] out) {
		out[0] = xs[index];
		out[1] = ys[index];
		out[2] = zs[index];
	}
}
