package com.vincenthuto.hemomancy.client.model.entity.morphling;

/**
 * The strain models' motion and glow curves, kept free of Minecraft types so JVM tests can pin them. Time is in
 * ticks (game time plus the frame's partial tick, from {@code AnimationTime}).
 */
public final class MorphlingStrainMotion {
	/** A Hushbell breath: a swell over BREATH_RISE ticks, then stillness for the rest of the cycle. */
	public static final float BREATH_CYCLE = 120.0F;
	public static final float BREATH_RISE = 50.0F;
	/** How far a state motion moves toward its target per tick (about a third of a second to settle). */
	public static final float SETTLE_PER_TICK = 0.14F;

	private MorphlingStrainMotion() {
	}

	/** The size a part is drawn at for a maturity: it grows in over its first stages, then holds its full size. */
	public static float growth(int partStage, int maturity) {
		int k = maturity - partStage;
		if (k < 0) return 0.0F;
		return k == 0 ? 0.7F : k == 1 ? 0.88F : 1.0F;
	}

	/** 0..1 swell of a held breath: a sine rise and fall, then stillness. */
	public static float breath(float time, float phase) {
		float t = (float) (((time + phase * 20.0F) % BREATH_CYCLE + BREATH_CYCLE) % BREATH_CYCLE);
		return t < BREATH_RISE ? (float) Math.sin(Math.PI * t / BREATH_RISE) : 0.0F;
	}

	/** 0..1 sawtooth of a droplet lengthening, then letting go. */
	public static float drip(float time, float speed, float phase) {
		float cycle = time * speed + phase;
		return cycle - (float) Math.floor(cycle);
	}

	/** One settling step of a state motion toward its target over {@code dt} ticks. */
	public static float settle(float value, boolean on, float dt) {
		float target = on ? 1.0F : 0.0F;
		float step = Math.min(1.0F, Math.max(0.0F, dt) * SETTLE_PER_TICK);
		return value + (target - value) * step;
	}

	/** Fronds open with the light (up to 70%) and fully while Lantern Fronds, Bloom or Arbor is on. */
	public static float frondOpen(float daylight, float active) {
		return Math.max(Math.max(0.0F, Math.min(1.0F, daylight)) * 0.7F, active);
	}

	/** A pulse running along a vein: group 0 lights first, then the next, and so on. */
	public static float veinPulse(float time, int group) {
		float wave = (float) Math.max(0.0D, Math.sin(time * 0.12D - group * 0.75D));
		return 0.22F + 0.78F * wave * wave * wave;
	}

	/** Kinbud's mane glow, flickering per segment and full while Kindled. */
	public static float flame(float time, int group, boolean kindled) {
		float flicker = 0.55F + 0.25F * (float) Math.sin(time * 0.31D - group * 0.9D)
				+ 0.1F * (float) Math.sin(time * 0.83D + group * 1.7D);
		return Math.min(1.0F, flicker + (kindled ? 0.35F : 0.0F));
	}
}
