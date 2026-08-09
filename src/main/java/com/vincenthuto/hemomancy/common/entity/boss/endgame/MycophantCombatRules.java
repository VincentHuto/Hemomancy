package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.util.Mth;

public final class MycophantCombatRules {
	public enum NectarHazard {
		SHALLOW,
		SLOW,
		DEEP,
		ENGULFING
	}

	private MycophantCombatRules() {
	}

	public static int phase(float health, float maxHealth) {
		return health <= maxHealth * 0.5F ? 2 : 1;
	}

	public static int sweepCadenceTicks(int phase) {
		return phase >= 2 ? 70 : 100;
	}

	public static int cocoonCadenceTicks(int phase) {
		return phase >= 2 ? 180 : 280;
	}

	public static int surgeCadenceTicks(int phase) {
		return phase >= 2 ? 240 : 360;
	}

	public static int cocoonNodeCount(int phase) {
		return phase >= 2 ? 4 : 3;
	}

	public static float pressureAfterEscape(float pressure, boolean empowered) {
		return Mth.clamp(pressure - (empowered ? 20.0F : 10.0F), 0.0F, 100.0F);
	}

	public static float pressureAfterSecond(float pressure, int phase) {
		float base = phase >= 2 ? Math.max(50.0F, pressure) : pressure;
		return Mth.clamp(base + (phase >= 2 ? 0.5F : 0.25F), 0.0F, 100.0F);
	}

	public static float pressureAfterSurge(float pressure) {
		return Mth.clamp(pressure + 10.0F, 0.0F, 100.0F);
	}

	public static float pressureAfterFailedCocoon(float pressure) {
		return Mth.clamp(pressure + 15.0F, 0.0F, 100.0F);
	}

	public static NectarHazard nectarHazard(float pressure) {
		if (pressure >= 85.0F) {
			return NectarHazard.ENGULFING;
		}
		if (pressure >= 60.0F) {
			return NectarHazard.DEEP;
		}
		if (pressure >= 30.0F) {
			return NectarHazard.SLOW;
		}
		return NectarHazard.SHALLOW;
	}
}
