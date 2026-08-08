package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Timing shared by the server phase gate and the client dismount/absorption animation. */
public final class VesperPhaseTransitionRules {
	public static final int DISMOUNT_TICKS = 36;
	public static final int MOUNT_ABSORPTION_TICKS = 84;
	public static final int TOTAL_TICKS = DISMOUNT_TICKS + MOUNT_ABSORPTION_TICKS;

	private VesperPhaseTransitionRules() {
	}

	public static float dismountProgress(float transitionTick) {
		return clamp(transitionTick / DISMOUNT_TICKS);
	}

	public static float absorptionProgress(float transitionTick) {
		return clamp((transitionTick - DISMOUNT_TICKS) / MOUNT_ABSORPTION_TICKS);
	}

	public static boolean isAbsorbing(float transitionTick) {
		return transitionTick > DISMOUNT_TICKS && transitionTick < TOTAL_TICKS;
	}

	public static boolean isComplete(int transitionTick) {
		return transitionTick >= TOTAL_TICKS;
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}
}
