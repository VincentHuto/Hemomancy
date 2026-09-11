package com.vincenthuto.hemomancy.common.vein;

public record EarthenVeinFeedingVortexProfile(Effect effect, double angularSpeed,
		double inwardSpeed, boolean graspingTendrils) {
	public enum Effect {
		DARK_GLOW,
		BLOOD_GLOW
	}

	public static EarthenVeinFeedingVortexProfile forPhase(EarthenVeinTravelPhase phase, float intensity) {
		float strength = Math.max(0.0F, Math.min(1.0F, intensity));
		if (phase == EarthenVeinTravelPhase.READY) {
			return new EarthenVeinFeedingVortexProfile(Effect.BLOOD_GLOW, 0.16D,
					0.055D + strength * 0.045D, true);
		}
		return new EarthenVeinFeedingVortexProfile(Effect.DARK_GLOW, 0.42D,
				0.12D + strength * 0.08D, true);
	}
}
