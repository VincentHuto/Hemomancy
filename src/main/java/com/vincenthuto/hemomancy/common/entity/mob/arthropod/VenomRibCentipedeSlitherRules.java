package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

public final class VenomRibCentipedeSlitherRules {
	public static final int BODY_SEGMENT_COUNT = 15;
	public static final int LEG_PAIRS_PER_SEGMENT = 1;
	public static final float BODY_SEGMENT_SPACING = 3.0F;
	public static final int ANTENNA_SEGMENT_COUNT = 6;
	public static final float ANTENNA_SEGMENT_LENGTH = 1.55F;
	public static final float ANTENNA_BACK_SWEEP = 0.42F;
	public static final int TAIL_FEELER_SEGMENT_COUNT = 6;
	public static final float TAIL_FEELER_SEGMENT_LENGTH = 1.05F;

	private static final float PHASE_STEP = 0.58F;
	private static final float BASE_YAW_AMPLITUDE = 0.48F;
	private static final float LEG_SWING_FREQUENCY = 3.35F;
	private static final float LEG_SWING_AMPLITUDE = 0.78F;
	private static final float LEG_LIFT_AMPLITUDE = 0.22F;
	private static final float ANTENNA_TWITCH_FREQUENCY = 0.34F;
	private static final float ANTENNA_TWITCH_AMPLITUDE = 0.075F;
	private static final float TAIL_FEELER_TWITCH_AMPLITUDE = 0.045F;

	private VenomRibCentipedeSlitherRules() {
	}

	public static float segmentLocalYaw(int segmentIndex, float waveTime, float movement) {
		float taper = 1.0F - segmentIndex * 0.035F;
		return (float) Math.sin(waveTime - segmentIndex * PHASE_STEP) * BASE_YAW_AMPLITUDE * taper * movement;
	}

	public static float segmentLocalPitch(int segmentIndex, float waveTime, float movement) {
		return (float) Math.cos((waveTime - segmentIndex * PHASE_STEP) * 0.75F) * 0.035F * movement;
	}

	public static float legPitch(int segmentIndex, boolean left, float waveTime, float movement) {
		float sideOffset = left ? 0.0F : (float) Math.PI;
		return (float) Math.sin(waveTime * LEG_SWING_FREQUENCY + sideOffset + segmentIndex * 0.72F)
				* LEG_SWING_AMPLITUDE * movement;
	}

	public static float legLift(int segmentIndex, boolean left, float waveTime, float movement) {
		float sideOffset = left ? 0.0F : (float) Math.PI;
		return (float) Math.cos(waveTime * LEG_SWING_FREQUENCY + sideOffset + segmentIndex * 0.72F)
				* LEG_LIFT_AMPLITUDE * movement;
	}

	public static float antennaTwitch(float ageInTicks, boolean left) {
		float phase = left ? 0.0F : 1.7F;
		float quickTwitch = (float) Math.sin(ageInTicks * ANTENNA_TWITCH_FREQUENCY + phase)
				* ANTENNA_TWITCH_AMPLITUDE;
		float smallDrift = (float) Math.sin(ageInTicks * 0.11F + phase * 0.6F) * 0.025F;
		return quickTwitch + smallDrift;
	}

	public static float antennaCurlYaw(int segmentIndex, boolean left) {
		float direction = left ? -1.0F : 1.0F;
		return direction * (0.18F + segmentIndex * 0.055F);
	}

	public static float antennaCurlPitch(int segmentIndex) {
		return -0.1F + segmentIndex * 0.015F;
	}

	public static float tailFeelerTwitch(int segmentIndex, float ageInTicks, boolean left) {
		float phase = (left ? 0.6F : 2.1F) + segmentIndex * 0.55F;
		return (float) Math.sin(ageInTicks * 0.22F + phase) * TAIL_FEELER_TWITCH_AMPLITUDE;
	}
}
