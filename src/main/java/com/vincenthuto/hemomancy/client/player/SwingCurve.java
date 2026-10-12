package com.vincenthuto.hemomancy.client.player;

import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/**
 * A keyframed value over a normalised swing (0..1). Each segment chooses its own easing, so a strike can
 * accelerate into contact while wind-ups and recoveries start and stop at rest.
 */
public final class SwingCurve {
	public enum Ease {
		/** Starts and stops at rest. */
		SMOOTH,
		/** Starts and stops at rest with a faster middle; for cuts that should pass the target at full speed. */
		SHARP,
		/** Accelerates into the next key; for blows that land at speed. */
		IN,
		/** Decelerates into the next key; for follow-through and settling. */
		OUT,
		LINEAR
	}

	private final float[] times;
	private final float[] values;
	private final Ease[] eases;

	private SwingCurve(List<Float> times, List<Float> values, List<Ease> eases) {
		this.times = new float[times.size()];
		this.values = new float[values.size()];
		for (int i = 0; i < times.size(); i++) {
			this.times[i] = times.get(i);
			this.values[i] = values.get(i);
		}
		this.eases = eases.toArray(new Ease[0]);
	}

	public static Builder from(float value) {
		return new Builder(value);
	}

	public float at(float progress) {
		if (progress <= times[0]) return values[0];
		for (int i = 1; i < times.length; i++) {
			if (progress <= times[i]) {
				float t = (progress - times[i - 1]) / (times[i] - times[i - 1]);
				return Mth.lerp(ease(eases[i - 1], t), values[i - 1], values[i]);
			}
		}
		return values[values.length - 1];
	}

	static float ease(Ease ease, float value) {
		float t = Mth.clamp(value, 0.0F, 1.0F);
		return switch (ease) {
			case SMOOTH -> t * t * (3.0F - 2.0F * t);
			case SHARP -> t * t * t * (t * (6.0F * t - 15.0F) + 10.0F);
			case IN -> t * t * t;
			case OUT -> 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
			case LINEAR -> t;
		};
	}

	public static final class Builder {
		private final List<Float> times = new ArrayList<>();
		private final List<Float> values = new ArrayList<>();
		private final List<Ease> eases = new ArrayList<>();

		private Builder(float value) {
			times.add(0.0F);
			values.add(value);
		}

		public Builder to(float time, float value, Ease ease) {
			if (time <= times.get(times.size() - 1)) throw new IllegalArgumentException("Keys must advance in time");
			times.add(time);
			values.add(value);
			eases.add(ease);
			return this;
		}

		public SwingCurve build() {
			return new SwingCurve(times, values, eases);
		}
	}
}
