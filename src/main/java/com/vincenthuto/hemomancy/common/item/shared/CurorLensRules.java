package com.vincenthuto.hemomancy.common.item.shared;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

public final class CurorLensRules {
	public static final double SCAN_RANGE = 16.0D;
	public static final int USE_DURATION_TICKS = 72000;
	public static final int MAX_SCAR_NAMES = 2;
	public static final int SCREEN_TINT_ALPHA = 18;

	private CurorLensRules() {
	}

	public static String dominantTendencyName(float[] values) {
		EnumBloodTendency[] tendencies = EnumBloodTendency.values();
		float best = 0.0F;
		EnumBloodTendency bestTendency = null;
		for (int i = 0; i < values.length && i < tendencies.length; i++) {
			if (values[i] > best) {
				best = values[i];
				bestTendency = tendencies[i];
			}
		}
		return bestTendency == null ? "unresolved" : bestTendency.name();
	}

	public static String healthLine(float health, float maxHealth) {
		return Math.round(health) + "/" + Math.round(maxHealth);
	}

	public static String bloodLine(double blood, double maxBlood) {
		return Math.round(blood) + "/" + Math.round(maxBlood) + " mL";
	}

	public static int screenTintColor() {
		return (SCREEN_TINT_ALPHA << 24) | 0x050001;
	}
}
