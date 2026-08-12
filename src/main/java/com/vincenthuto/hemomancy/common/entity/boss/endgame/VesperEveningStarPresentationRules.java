package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Pure thresholds and interpolation for the Evening Star's presentation-only state. */
public final class VesperEveningStarPresentationRules {
	public static final int HOOD_REMOVAL_TICKS = 30;
	public static final int FINAL_COLLAPSE_TICKS = 5;
	public static final int RED_LINE_LIGHT = 0x00500050;

	private VesperEveningStarPresentationRules() { }

	public static boolean shouldBeginHoodRemoval(boolean hoodRemoved, float health, float maxHealth) {
		return !hoodRemoved && health > 0.0F && maxHealth > 0.0F && health <= maxHealth * 0.5F;
	}

	public static boolean isHoodRemovalActive(boolean hoodRemoved, int transitionTick) {
		return hoodRemoved && transitionTick >= 0 && transitionTick < HOOD_REMOVAL_TICKS;
	}

	public static boolean isHoodVisible(boolean hoodRemoved, int transitionTick) {
		return !hoodRemoved || isHoodRemovalActive(hoodRemoved, transitionTick);
	}

	public static boolean shouldRenderRedLines(float health, float maxHealth, boolean shamed) {
		return !shamed && maxHealth > 0.0F && health <= maxHealth * 0.5F;
	}

	public static float redLineAlpha(float ageInTicks) {
		return 0.30F + (float) Math.sin(ageInTicks * 0.16F) * 0.08F;
	}

	public static float absorptionProgress(float progress) {
		return smoothstep(clamp(progress / VesperCombatRules.DEFEAT_ABSORPTION_REQUIRED));
	}

	public static float absorptionScale(float progress) {
		return lerp(absorptionProgress(progress), 1.0F, 0.08F);
	}

	public static float absorptionLowering(float progress) {
		return absorptionProgress(progress) * 0.82F;
	}

	public static float absorptionDissolve(float progress) {
		return absorptionProgress(progress);
	}

	public static float finalCollapseProgress(float progress) {
		return smoothstep(clamp((progress - 95.0F) / 5.0F));
	}

	public static float finalCollapseScale(int collapseTick) {
		return lerp(smoothstep(clamp(collapseTick / (float) FINAL_COLLAPSE_TICKS)), 1.0F, 0.5F);
	}

	public static boolean isFinalCollapseComplete(int collapseTick) {
		return collapseTick >= FINAL_COLLAPSE_TICKS;
	}

	private static float clamp(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static float smoothstep(float value) {
		return value * value * (3.0F - 2.0F * value);
	}

	private static float lerp(float progress, float start, float end) {
		return start + (end - start) * progress;
	}
}
