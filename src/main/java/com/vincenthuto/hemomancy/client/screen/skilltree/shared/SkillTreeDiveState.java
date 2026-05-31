package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

public final class SkillTreeDiveState {
	public static final float DEEP_ENTER_THRESHOLD = 0.98f;
	public static final float DEEP_EXIT_ZOOM = 0.35f;
	public static final int TRANSITION_PULSE_TICKS = 16;

	private SkillTreeLayer activeLayer = SkillTreeLayer.SURFACE;
	private int transitionPulseTicks;
	private boolean transitionPulseEnteringDeep;

	public SkillTreeLayer activeLayer() {
		return activeLayer;
	}

	public boolean isDeepActive() {
		return activeLayer == SkillTreeLayer.DEEP;
	}

	public float deepFade(float surfaceDiveProgress) {
		return isDeepActive() ? 1.0f : surfaceDiveProgress;
	}

	public float transitionPulseProgress() {
		return transitionPulseTicks <= 0 ? 0.0f : (float) transitionPulseTicks / TRANSITION_PULSE_TICKS;
	}

	public boolean transitionPulseEnteringDeep() {
		return transitionPulseEnteringDeep;
	}

	public void tickTransitionPulse() {
		if (transitionPulseTicks > 0) transitionPulseTicks--;
	}

	public boolean updateSurfaceDive(int playerDegree, float surfaceDiveProgress) {
		if (isDeepActive() || !SkillTreeLayerRules.isDeepUnlocked(playerDegree)) return false;
		if (surfaceDiveProgress < DEEP_ENTER_THRESHOLD) return false;
		activeLayer = SkillTreeLayer.DEEP;
		startTransitionPulse(true);
		return true;
	}

	public boolean updateDeepZoom(float deepZoom) {
		if (!isDeepActive() || deepZoom > DEEP_EXIT_ZOOM) return false;
		activeLayer = SkillTreeLayer.SURFACE;
		startTransitionPulse(false);
		return true;
	}

	public void resetToSurface() {
		activeLayer = SkillTreeLayer.SURFACE;
	}

	private void startTransitionPulse(boolean enteringDeep) {
		transitionPulseTicks = TRANSITION_PULSE_TICKS;
		transitionPulseEnteringDeep = enteringDeep;
	}
}
