package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

public final class VenomRibCentipedeSlitherRules {
	public static final int BODY_SEGMENT_COUNT = 15;
	public static final int LEG_PAIRS_PER_SEGMENT = 1;
	public static final float BODY_SEGMENT_SPACING = 3.0F;

	private static final float PHASE_STEP = 0.58F;
	private static final float BASE_YAW_AMPLITUDE = 0.48F;
	private static final float LEG_SWING_FREQUENCY = 3.35F;
	private static final float LEG_SWING_AMPLITUDE = 0.78F;
	private static final float LEG_LIFT_AMPLITUDE = 0.22F;

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
}
