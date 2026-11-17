package com.vincenthuto.hemomancy.client.render.world.circus;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * A phantom's pose, in model radians for the humanoid parts every troupe rig shares (head, body, arms, legs). Pure:
 * the same seed and time give the same pose, so a frozen tick holds every phantom still.
 */
public record PhantomPose(float headYaw, float headPitch, float bodyLean, float rightArmX, float leftArmX,
		float rightArmZ, float leftArmZ, float legX) {
	private static final float MAX_HEAD_TURN = (float) Math.toRadians(60.0D);
	private static final double SWING = Math.toRadians(40.0D);
	private static final float SWING_PERIOD = 90.0F;
	public static final PhantomPose STANDING = new PhantomPose(0, 0, 0, 0, 0, 0.08F, -0.08F, 0);

	/**
	 * A seated audience member: legs forward, hands on the knees, a slow breath of a sway, its head turned toward
	 * {@code lookYaw} (radians off its own facing, clamped). Applause brings both hands up and together.
	 */
	public static PhantomPose seated(long seed, float time, float lookYaw, float applause) {
		float own = PhantomShowRules.unit(seed);
		float sway = (float) Math.sin(time * 0.031F + own * 6.28F) * 0.05F;
		float head = Math.clamp(lookYaw + (own - 0.5F) * 0.5F, -MAX_HEAD_TURN, MAX_HEAD_TURN);
		float pitch = (PhantomShowRules.unit(seed ^ 0x5DEECE66DL) - 0.5F) * 0.3F;
		float lean = 0.12F + own * 0.12F + sway;
		float clap = (float) Math.sin(time * 1.6F + own * 6.28F) * 0.12F;
		float k = Math.clamp(applause, 0.0F, 1.0F);
		return new PhantomPose(head, pitch - 0.1F * k, lean - 0.08F * k,
				HemoEasing.lerp(-0.62F - own * 0.15F, -1.75F + clap, k), HemoEasing.lerp(-0.6F - own * 0.12F, -1.75F - clap, k),
				HemoEasing.lerp(-0.06F, 0.35F, k), HemoEasing.lerp(0.06F, -0.35F, k), -1.41F);
	}

	/** Hanging from the bar by both hands; the legs trail the swing at {@code phase} radians. */
	public static PhantomPose swinging(float phase) {
		float trail = (float) Math.sin(phase - 0.6F) * 0.5F;
		return new PhantomPose(0.0F, -0.2F, 0.0F, -3.0F, -3.0F, 0.12F, -0.12F, trail);
	}

	/** The pendulum angle of a swinging trapeze artist, radians, within plus or minus 40 degrees. */
	public static double trapezeSwing(float time, long seed) {
		double phase = time / SWING_PERIOD * Math.PI * 2.0D + PhantomShowRules.unit(seed) * Math.PI * 2.0D;
		return Math.sin(phase) * SWING;
	}

	/** The phase {@link #trapezeSwing} is at, for the trailing legs. */
	public static float swingPhase(float time, long seed) {
		return (float) (time / SWING_PERIOD * Math.PI * 2.0D + PhantomShowRules.unit(seed) * Math.PI * 2.0D);
	}

	/**
	 * A ring act: 0 the tumbler's ready (arms high, a bounce), 1 the knife throw (wind up, release), 2 the flourish
	 * (one arm swept out and up, a bow).
	 */
	public static PhantomPose ringAct(int act, float time) {
		float beat = (time % StringLights.BEAT) / StringLights.BEAT;
		return switch (Math.floorMod(act, 3)) {
			case 0 -> {
				float bounce = (float) Math.sin(beat * Math.PI * 4.0D);
				yield new PhantomPose(0.0F, -0.15F, -0.05F, -2.9F + bounce * 0.15F, -2.9F - bounce * 0.15F, 0.25F,
						-0.25F, bounce * 0.12F);
			}
			case 1 -> {
				float arm = beat < 0.6F ? -2.6F * (beat / 0.6F) : -2.6F + (beat - 0.6F) / 0.4F * 2.2F;
				yield new PhantomPose(0.0F, 0.0F, beat < 0.6F ? -0.1F : 0.15F, arm, -0.4F, 0.1F, -0.3F, 0.0F);
			}
			default -> {
				float bow = beat > 0.7F ? (beat - 0.7F) / 0.3F : 0.0F;
				yield new PhantomPose(0.0F, bow * 0.4F, bow * 0.55F, -1.2F - (1.0F - bow) * 1.0F, -0.2F, 0.0F,
						-1.1F + bow * 0.6F, 0.0F);
			}
		};
	}
}
