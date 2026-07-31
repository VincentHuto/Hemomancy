package com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular;

/** Permanent vascular benefit granted by the Rite of Hematic Fortification. */
public final class HematicFortificationRules {
	public static final float STRAIN_MULTIPLIER = 0.85F;

	private HematicFortificationRules() {
	}

	public static float adjustedStrain(float strain, boolean fortified) {
		float safe = Math.max(0.0F, strain);
		return fortified ? safe * STRAIN_MULTIPLIER : safe;
	}
}
