package com.vincenthuto.hemomancy.client.model.entity.boss.hemorath;

import java.util.Arrays;

/**
 * Blends Hemorath out of whatever pose he last showed into a new track (resolution B-8), by inertialization rather
 * than a crossfade: when the track key changes, the gap between the last shown pose and the new track's first pose is
 * kept as an offset, with the velocity the body was moving at, and that offset dies away along a cubic Hermite curve
 * over the blend. The new track plays at full speed underneath from its first frame, so a strike that follows a
 * wind-up keeps its snap, and a body moving fast when the move changed carries on a little before it turns. Pure
 * maths, JVM-tested.
 */
public final class HemorathPoseBlend {
	/** How quickly the measured pose velocity follows the frames (share per tick). */
	private static final float VELOCITY_FOLLOW = 0.6F;
	/** The carried velocity never moves a channel more than this many times its offset over the blend. */
	private static final float VELOCITY_LIMIT = 1.5F;

	private long key = Long.MIN_VALUE;
	private float[] shown;
	private float[] velocity;
	private float[] offset;
	private float[] slope;
	private float lastTime = Float.NaN;
	private float changedAt;
	private float duration = 1.0F;

	/**
	 * Lays the blend onto {@code pose} in place and remembers the result. {@code key} names the track (a change starts
	 * a blend), {@code time} is the frame time in ticks and {@code duration} the blend's length in ticks.
	 */
	public void apply(float[] pose, long key, float time, float duration) {
		if (shown == null || shown.length != pose.length) {
			shown = pose.clone();
			velocity = new float[pose.length];
			offset = new float[pose.length];
			slope = new float[pose.length];
			this.key = key;
			lastTime = time;
			changedAt = time - duration;
			this.duration = Math.max(0.5F, duration);
			return;
		}
		if (key != this.key) {
			this.key = key;
			changedAt = time;
			this.duration = Math.max(0.5F, duration);
			for (int i = 0; i < pose.length; i++) {
				offset[i] = shown[i] - pose[i];
				float limit = Math.abs(offset[i]) * VELOCITY_LIMIT / this.duration + 0.02F;
				slope[i] = Math.max(-limit, Math.min(limit, velocity[i]));
			}
		}
		float age = time - changedAt;
		if (age < this.duration) {
			for (int i = 0; i < pose.length; i++) pose[i] += decay(offset[i], slope[i], age, this.duration);
		}
		float dt = Float.isNaN(lastTime) ? 0.0F : time - lastTime;
		if (dt > 1.0E-4F && dt < 10.0F) {
			float follow = Math.min(1.0F, VELOCITY_FOLLOW * dt);
			for (int i = 0; i < pose.length; i++) {
				float measured = (pose[i] - shown[i]) / dt;
				velocity[i] += (measured - velocity[i]) * follow;
			}
		} else if (dt >= 10.0F || dt < 0.0F) {
			Arrays.fill(velocity, 0.0F);
		}
		lastTime = time;
		System.arraycopy(pose, 0, shown, 0, pose.length);
	}

	/**
	 * The offset left at {@code age} of a blend of {@code duration}: starts at {@code offset} moving at {@code slope}
	 * (units a tick) and comes to rest at zero, flat, at the end.
	 */
	public static float decay(float offset, float slope, float age, float duration) {
		if (age <= 0.0F) return offset;
		if (age >= duration) return 0.0F;
		float s = age / duration;
		float s2 = s * s, s3 = s2 * s;
		return offset * (2 * s3 - 3 * s2 + 1) + slope * duration * (s3 - 2 * s2 + s);
	}

	/** Whether a blend is still running at {@code time}. */
	public boolean blending(float time) {
		return shown != null && time - changedAt < duration;
	}

	/** For tests and the review: the pose last shown. */
	public float[] shown() {
		return shown == null ? new float[0] : shown.clone();
	}
}
