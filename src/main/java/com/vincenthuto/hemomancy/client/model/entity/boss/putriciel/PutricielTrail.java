package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import net.minecraft.world.phys.Vec3;

/**
 * The world-space path of one posed limb (the palm, a censer, the staff head), kept a frame at a time as the renderer
 * draws it, so a sweep, a reach or a thrown censer's trail in the world geometry follows the animation's own path and
 * can never disagree with it (Hemorath's claw trail, made general). About a second of frames at 60 a second. Pure and
 * JVM-tested.
 */
public final class PutricielTrail {
	public static final int FRAMES = 64;

	private final float[] ages = new float[FRAMES];
	private final Vec3[] points = new Vec3[FRAMES];
	private int head;
	private int count;

	/** Keeps {@code point} at {@code age} (the entity's render age); a second record in one frame is ignored. */
	public void record(Vec3 point, float age) {
		if (count > 0) {
			float last = ages[Math.floorMod(head - 1, FRAMES)];
			if (age < last - 1.0F) count = 0;
			else if (age <= last) return;
		}
		ages[head] = age;
		points[head] = point;
		head = (head + 1) % FRAMES;
		count = Math.min(FRAMES, count + 1);
	}

	/** Where the limb was at {@code age}, interpolated between frames, or null outside the kept frames. */
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

	/** The newest point kept, or null. */
	public Vec3 latest() {
		return count == 0 ? null : points[Math.floorMod(head - 1, FRAMES)];
	}

	/** How fast the limb moved over the last {@code span} ticks, blocks a tick; 0 without enough frames. */
	public double speed(float span) {
		if (count < 2) return 0.0D;
		float now = ages[Math.floorMod(head - 1, FRAMES)];
		Vec3 then = at(now - span), last = latest();
		return then == null || span <= 0.0F ? 0.0D : last.distanceTo(then) / span;
	}

	public void clear() {
		count = 0;
	}
}
