package com.vincenthuto.hemomancy.common.entity.mob.animal;

public final class ScarletSerpentSlitherRules {
	public static final int BODY_SEGMENT_COUNT = 9;
	public static final float BODY_SEGMENT_SPACING = 3.25F;

	private static final float PHASE_STEP = 0.45F;
	private static final float BASE_YAW_AMPLITUDE = 0.12F;
	private static final float TAIL_YAW_AMPLITUDE = 0.2F;

	private ScarletSerpentSlitherRules() {
	}

	public static float segmentLocalYaw(int segmentIndex, float waveTime, float movement) {
		float taper = 0.85F + segmentIndex * 0.025F;
		return (float) Math.sin(waveTime - segmentIndex * PHASE_STEP) * BASE_YAW_AMPLITUDE * taper * movement;
	}

	public static float segmentLocalPitch(int segmentIndex, float waveTime, float movement) {
		return (float) Math.cos((waveTime - segmentIndex * PHASE_STEP) * 0.8F) * 0.025F * movement;
	}

	public static float tailLocalYaw(float waveTime, float movement) {
		return (float) Math.sin(waveTime - BODY_SEGMENT_COUNT * PHASE_STEP) * TAIL_YAW_AMPLITUDE * movement;
	}

	public static float segmentPropagatedYaw(int segmentIndex, float waveTime, float movement) {
		float yaw = 0.0F;
		for (int i = 0; i <= segmentIndex; i++) {
			yaw += segmentLocalYaw(i, waveTime, movement);
		}
		return yaw;
	}
}
