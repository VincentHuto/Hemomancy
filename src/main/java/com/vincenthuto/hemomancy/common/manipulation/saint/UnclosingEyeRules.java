package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * The Unclosing Eye's numbers (D-1, canon memory rework spec 3.2): Seraphae's own eye opened in the caster's chest. A
 * charged beam swept with the head at her capped rate; it heals the hurt and grows the whole, friend or not
 * (NQ-08-7), then the eye lapses and stays open as a slit. No Minecraft types, so the JVM tests read it whole.
 */
public final class UnclosingEyeRules {
	/** The full charge; the shared floor (half of it) is the shortest charge that opens the eye. */
	public static final int FULL_CHARGE = 50;
	public static final int MIN_CHARGE = FULL_CHARGE / 2;
	public static final int BEAM_TICKS = 40;
	public static final int PULSE_TICKS = 5;
	public static final double BASE_RANGE = 12.0D, CHARGE_RANGE = 12.0D;
	public static final double HALF_ANGLE_DEGREES = 4.0D;
	/** The beam follows the caster's look at most this far a tick: her 110 degrees over 40 ticks. */
	public static final double SWEEP_DEGREES_PER_TICK = 5.0D;
	/** The caster moves at sneak speed while the eye charges and while the beam is out. */
	public static final double SNEAK_SLOW = 0.70D;
	public static final int COOLDOWN_TICKS = 500;
	/** On Seraphae herself, in her escaping light, each pulse gives her this much integrity, at most six a cast. */
	public static final float SERAPHAE_INTEGRITY = 1.0F;
	public static final int SERAPHAE_INTEGRITY_CAP = 6;

	public static final int SWELLING = 3, KNOTTING = 6, BURST = 8, AFTER_BURST = 2;
	public static final int KNOT_ROOT = 20, RIME_KNOT_ROOT = 30;
	public static final float BURST_DAMAGE = 8.0F, BOSS_BURST_DAMAGE = 5.0F, ALLY_BURST_DAMAGE = 4.0F;
	public static final double ILLUMINATED_BURST = 1.5D;
	public static final int DECAY_TICKS = 40;
	public static final float ALLY_HEAL = 1.0F;

	public static final int LAPSE_TICKS = 50, SLIT_TICKS = 200;
	public static final double LAPSE_HURT = 0.20D;
	public static final double SLIT_RANGE = 32.0D;
	public static final int SLIT_CHECK_TICKS = 10, SLIT_ILLUMINATED = 60;

	/** What one pulse does to a body's growth. */
	public enum Step { GROW, HOLD, SHED }

	private UnclosingEyeRules() {
	}

	/** The beam's charge, 0 at the floor and 1 at full. */
	public static float charge(float chargeTicks) {
		return (float) HemoEasing.clamp01((chargeTicks - MIN_CHARGE) / (double) (FULL_CHARGE - MIN_CHARGE));
	}

	public static double range(float charge) {
		return BASE_RANGE + CHARGE_RANGE * HemoEasing.clamp01(charge);
	}

	public static float pulseDamage(float charge) {
		return (float) (2.0D + 1.5D * HemoEasing.clamp01(charge));
	}

	/**
	 * How a pulse moves a body's growth: a Searing body sheds one (heat closes the light out); a whole ally grows on
	 * every other pulse; anyone else grows on every pulse.
	 */
	public static Step step(boolean searing, boolean ally, int allyPulse) {
		if (searing) return Step.SHED;
		if (ally && allyPulse % 2 == 1) return Step.HOLD;
		return Step.GROW;
	}

	public static int grow(int growth, Step step) {
		return switch (step) {
			case GROW -> growth + 1;
			case HOLD -> growth;
			case SHED -> Math.max(0, growth - 1);
		};
	}

	/** The stage a growth stands at: 0 none, 1 Swelling, 2 Knotting, 3 Burst. */
	public static int stage(int growth) {
		return growth >= BURST ? 3 : growth >= KNOTTING ? 2 : growth >= SWELLING ? 1 : 0;
	}

	/** True when a pulse that took growth from {@code before} to {@code after} reached Knotting. */
	public static boolean knots(int before, int after) {
		return before < KNOTTING && after >= KNOTTING;
	}

	public static float burstDamage(boolean boss, boolean ally) {
		return ally ? ALLY_BURST_DAMAGE : boss ? BOSS_BURST_DAMAGE : BURST_DAMAGE;
	}

	/** An ally's Burst never takes them below 1 health. */
	public static float allyBurst(float health) {
		return Math.max(0.0F, Math.min(ALLY_BURST_DAMAGE, health - 1.0F));
	}

	/**
	 * The beam's yaw or pitch one tick on, turned toward {@code look} by at most
	 * {@link #SWEEP_DEGREES_PER_TICK}, the short way round.
	 */
	public static double sweep(double current, double look) {
		double delta = wrapDegrees(look - current);
		double step = Math.max(-SWEEP_DEGREES_PER_TICK, Math.min(SWEEP_DEGREES_PER_TICK, delta));
		return current + step;
	}

	/**
	 * Whether a body is in the beam: ahead of the chest within {@code range}, and within the half-angle of the axis or
	 * close enough to it that its own breadth touches the beam.
	 */
	public static boolean inBeam(double along, double across, double range, double bodyRadius) {
		if (along <= 0.0D || along > range + bodyRadius) return false;
		double halfWidth = along * Math.tan(Math.toRadians(HALF_ANGLE_DEGREES));
		return across <= halfWidth + bodyRadius;
	}

	public static double wrapDegrees(double degrees) {
		double wrapped = degrees % 360.0D;
		if (wrapped >= 180.0D) wrapped -= 360.0D;
		if (wrapped < -180.0D) wrapped += 360.0D;
		return wrapped;
	}
}
