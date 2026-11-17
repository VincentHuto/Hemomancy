package com.vincenthuto.hemomancy.common.manipulation.lux;

/**
 * Hematic Flare under held Projection, as plain numbers: a beam of light swept like a searchlight. What it passes over
 * is Illuminated and outlined; held steady on one body it builds heat, and the attack button spends the heat as a
 * blinding flash.
 */
public final class FlareBeamRules {
	/** How far the beam reaches, as a multiple of the verb's reach, and how wide it is. */
	public static final double REACH_SCALE = 2.5D, RADIUS = 1.0D;
	/** Blood a pulse costs before the cost ledger: 40 mL a second. */
	public static final double BLOOD_PER_PULSE = 20.0D;
	/** How long a body the beam crossed stays Illuminated and outlined, and ticks before the beam marks it again. */
	public static final int ILLUMINATED_TICKS = 100, OUTLINE_TICKS = 80, MARK_LOCKOUT = 10;
	/** Ticks held on one body for full heat; heat is lost twice as fast as it builds once the beam leaves it. */
	public static final int FULL_HEAT_TICKS = 50;
	/** The least heat a flash needs. */
	public static final double MIN_FLASH_HEAT = 0.3D;
	/** The flash's damage at the least heat and at full, before Crimson Mastery, and how long it blinds. */
	public static final float MIN_FLASH = 2.0F, MAX_FLASH = 8.0F;
	public static final int BLIND_TICKS = 60, FLASH_ILLUMINATED_TICKS = 240;
	/** Ticks between flashes. */
	public static final int FLASH_COOLDOWN = 20;
	/** How fast the caster walks while sweeping, as a share of their pace. */
	public static final double PACE = 0.85D;

	private FlareBeamRules() {
	}

	/** The heat after one more tick: it climbs on the body it was building on, and falls away twice as fast off it. */
	public static double heat(double heat, boolean onSameBody) {
		double step = 1.0D / FULL_HEAT_TICKS;
		return Math.max(0.0D, Math.min(1.0D, heat + (onSameBody ? step : -2.0D * step)));
	}

	public static boolean mayFlash(double heat) {
		return heat >= MIN_FLASH_HEAT;
	}

	/** The flash's damage for {@code heat}; nothing below the least heat. */
	public static float flash(double heat) {
		if (!mayFlash(heat)) return 0.0F;
		double t = (Math.min(1.0D, heat) - MIN_FLASH_HEAT) / (1.0D - MIN_FLASH_HEAT);
		return (float) (MIN_FLASH + (MAX_FLASH - MIN_FLASH) * t);
	}
}
