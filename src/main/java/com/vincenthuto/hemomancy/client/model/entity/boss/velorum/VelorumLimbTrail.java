package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import net.minecraft.world.phys.Vec3;

/**
 * A limb's world positions over the last second or so of frames, keyed by the body's render age: the renderer records
 * one each frame and the world geometry reads back where the limb was at any age in that span, interpolated between
 * frames. A slash or a reach drawn from it follows the animated limb exactly. A frame recorded at an age no later than
 * the newest is skipped (a second render in the same frame); a big step back (a reloaded body) starts the trail again.
 */
public final class VelorumLimbTrail {
	/** Frames kept: about a second at 60 frames a second. */
	public static final int FRAMES = 64;

	private final float[] ages = new float[FRAMES];
	private final Vec3[] points = new Vec3[FRAMES];
	private int head;
	private int count;

	/** Keeps {@code point} as where the limb was at {@code age}. */
	public void record(float age, Vec3 point) {
		if (count > 0) {
			float newest = ages[Math.floorMod(head - 1, FRAMES)];
			if (age < newest - 1.0F) count = 0;
			else if (age <= newest) return;
		}
		ages[head] = age;
		points[head] = point;
		head = (head + 1) % FRAMES;
		count = Math.min(FRAMES, count + 1);
	}

	/** Where the limb was at {@code age}, or null outside the kept frames (half a tick past the newest is the newest). */
	public Vec3 at(float age) {
		if (count < 2) return null;
		int newest = Math.floorMod(head - 1, FRAMES);
		if (age > ages[newest] + 0.5F) return null;
		if (age >= ages[newest]) return points[newest];
		for (int i = 1; i < count; i++) {
			int older = Math.floorMod(newest - i, FRAMES), newer = Math.floorMod(older + 1, FRAMES);
			if (ages[older] <= age) {
				float span = ages[newer] - ages[older];
				double t = span <= 1.0E-4F ? 1.0D : (age - ages[older]) / span;
				return points[older].lerp(points[newer], t);
			}
		}
		return null;
	}

	/** The age of the newest frame kept, or NaN with none. */
	public float newest() {
		return count == 0 ? Float.NaN : ages[Math.floorMod(head - 1, FRAMES)];
	}

	/** Frames kept. */
	public int size() {
		return count;
	}

	public void clear() {
		count = 0;
	}
}
