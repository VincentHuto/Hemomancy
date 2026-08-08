package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Pure timing and style rules consumed by Vesper's server-side weapon effects. */
public final class VesperWeaponEffectRules {
	private VesperWeaponEffectRules() { }

	public static String styleName(VesperWeaponAction action) {
		return VesperVisualRules.weaponScene(action);
	}

	public static boolean shouldEmit(VesperWeaponAction action, int tick) {
		return switch (action) {
			case ICHIMONJI -> tick == 18;
			case CROSSCUT -> tick == 15 || tick == 26;
			case LEAPING_CLEAVE -> tick == 10 || tick == 22;
			case REAPER_SWEEP -> tick >= 14 && tick <= 18 && (tick & 1) == 0;
			case SKY_LANCE -> tick >= 12 && tick <= 25;
			case LANCE_FLURRY -> tick >= 12 && tick <= 28 && (tick & 1) == 0;
			case TWIN_REND -> tick == 12 || tick == 20;
			case PREDATOR_POUNCE -> tick == 20;
			case BRANDING_THRUSTS -> tick == 12 || tick == 19 || tick == 26;
			case UPDRAFT_IMPALEMENT -> tick >= 14 && tick <= 24 && (tick & 1) == 0;
			case CHAIN_SWEEP -> tick >= 12 && tick <= 18 && (tick & 1) == 0;
			case HOOK_AND_CRUSH -> tick == 14 || tick >= 24 && tick <= 28 && (tick & 1) == 0;
			default -> false;
		};
	}

	public static double arcDegrees(VesperWeaponAction action) {
		return switch (action) {
			case CHAIN_SWEEP -> 360.0D;
			case HOOK_AND_CRUSH -> 210.0D;
			default -> 0.0D;
		};
	}
}
