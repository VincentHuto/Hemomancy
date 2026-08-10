package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.world.entity.AnimationState;

/** Timing shared by the server phase gate and the client dismount/absorption animation. */
public final class VesperPhaseTransitionRules {
	public static final int DISMOUNT_TICKS = 36;
	public static final int MOUNT_ABSORPTION_TICKS = 84;
	public static final int TOTAL_TICKS = DISMOUNT_TICKS + MOUNT_ABSORPTION_TICKS;
	public static final int AWAKENING_GLOW_TICKS = 12;
	public static final int AWAKENING_SIGIL_START_TICK = 12;
	public static final int AWAKENING_SIGIL_INTERVAL_TICKS = 6;
	public static final int AWAKENING_GROWTH_END_TICK = 60;
	public static final int AWAKENING_TOTAL_TICKS = 72;
	public static final float EVENING_STAR_SCALE = 1.25F;
	private static final int AFFINITY_SIGIL_COUNT = 8;

	private VesperPhaseTransitionRules() {
	}

	public static float dismountProgress(float transitionTick) {
		return clamp(transitionTick / DISMOUNT_TICKS);
	}

	public static float absorptionProgress(float transitionTick) {
		return clamp((transitionTick - DISMOUNT_TICKS) / MOUNT_ABSORPTION_TICKS);
	}

	public static float collapseProgress(float transitionTick) {
		return smooth(dismountProgress(transitionTick));
	}

	public static float jumpArc(float transitionTick) {
		float progress = dismountProgress(transitionTick);
		return 4.0F * progress * (1.0F - progress);
	}

	public static int awakeningSigilCount(float awakeningTick) {
		if (awakeningTick < AWAKENING_SIGIL_START_TICK) return 0;
		int revealed = 1 + (int) ((awakeningTick - AWAKENING_SIGIL_START_TICK)
				/ AWAKENING_SIGIL_INTERVAL_TICKS);
		return Math.max(0, Math.min(AFFINITY_SIGIL_COUNT, revealed));
	}

	public static float awakeningScale(float awakeningTick) {
		float progress = clamp((awakeningTick - AWAKENING_SIGIL_START_TICK)
				/ (AWAKENING_GROWTH_END_TICK - AWAKENING_SIGIL_START_TICK));
		return 1.0F + (EVENING_STAR_SCALE - 1.0F) * smooth(progress);
	}

	public static float awakeningGlow(float awakeningTick) {
		if (awakeningTick <= AWAKENING_GLOW_TICKS) return 1.0F;
		return 1.0F - clamp((awakeningTick - AWAKENING_GLOW_TICKS)
				/ (AWAKENING_GROWTH_END_TICK - AWAKENING_GLOW_TICKS));
	}

	public static boolean isAwakeningComplete(int awakeningTick) {
		return awakeningTick >= AWAKENING_TOTAL_TICKS;
	}

	public static boolean isAbsorbing(float transitionTick) {
		return transitionTick > DISMOUNT_TICKS && transitionTick < TOTAL_TICKS;
	}

	public static boolean isComplete(int transitionTick) {
		return transitionTick >= TOTAL_TICKS;
	}

	public static void syncAnimationState(AnimationState state, int entityTickCount, int transitionTick) {
		if (transitionTick <= 0) {
			state.stop();
		} else if (!state.isStarted()) {
			state.start(entityTickCount - transitionTick);
		}
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static float smooth(float value) {
		return value * value * (3.0F - 2.0F * value);
	}
}
