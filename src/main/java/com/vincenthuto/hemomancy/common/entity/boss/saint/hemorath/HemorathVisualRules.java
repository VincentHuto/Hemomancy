package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * What his body shows, as pure numbers (design section 12): the three nested vessel stages that bud and fill inside his
 * ribs as the meter rises (each authored at its true size and only scaled between 0.8 and 1.15 once shown, so texel
 * density holds), the rib spread, the patch eruption overshoot, the glow, the hitbox width, and the server's burst
 * kinds. Visual numbers never decide hits, costs or rewards. JVM-tested.
 */
public final class HemorathVisualRules {
	/** One render scale in both phases: 2 texels a model unit at 2.0 is exactly 16 px a block (resolution B-11). */
	public static final float RENDER_SCALE = 2.0F;

	/** {@code ManipulationVisuals.burst} kinds of the HEMORATH_RELIQUARY form. */
	public static final int DEBT_HIT = 1, PULSE_RELEASE = 2, PHASE_BREAK = 3, INTAKE = 4, EXSANGUINATE = 5, MELEE = 6,
			SIPHON = 7, RIB_JETS = 8, PATCH_BLOOM = 9, CROWN_SPIKES = 10, STAFF_CRACK = 11, WELL_STREAM = 12,
			ROAD_HOME = 13;
	public static final int BURST_TICKS = 24, FINALE_TICKS = 32, STREAM_TICKS = 40, HOME_TICKS = 18;

	/** Vessel stages, in the order they emerge: cords always, lobes from 25%, the heart mass from 55%. */
	public enum Stage { CORDS, ARMS, LOBES, MASS }

	public static final float CORDS_REST = 0.6F;
	public static final float SHOWN_MIN = 0.8F;
	public static final float SHOWN_MAX = 1.15F;
	/** The meter band over which a stage buds from nothing to its smallest shown size. */
	public static final float BUD = 0.05F;
	public static final float LOBES_FROM = 0.25F;
	public static final float ARMS_FROM = 0.35F;
	public static final float MASS_FROM = 0.55F;

	/** Ribs part with the meter, up to this many radians; a Rib Burst prises them to RIB_BURST_SPREAD. */
	public static final float RIB_SPREAD = 0.55F;
	public static final float RIB_BURST_SPREAD = 0.8F;
	/** A patch erupts to this, then settles at 1, over PATCH_ERUPT ticks. */
	public static final float PATCH_OVERSHOOT = 1.15F;
	public static final int PATCH_ERUPT = 8;
	/** A fresh patch's tip glows for this long. */
	public static final int PATCH_FRESH = 100;

	/** The glow mask's alpha: 0.15 hollow to 0.75 full; an opening that feeds lifts it to 0.9. */
	public static final float GLOW_BASE = 0.15F;
	public static final float GLOW_RANGE = 0.6F;
	public static final float GLOW_OPEN = 0.9F;
	/** The heartbeat's rate (radians a tick): 0.12 hollow, 0.30 full. */
	public static final float BEAT_EMPTY = 0.12F;
	public static final float BEAT_FULL = 0.30F;

	/** The hitbox: 0.9 wide hollow, 1.4 brimming, 3.0 tall; the culling box grows while the halo is away. */
	public static final float WIDTH_SUNKEN = 0.9F;
	public static final float WIDTH_BRIMMING = 1.4F;
	public static final float HEIGHT = 3.0F;
	public static final double HALO_CULL = 11.0D;
	/** The client smooths the synced meter over this many ticks before the body reads it. */
	public static final float SWELL_SMOOTHING = 10.0F;

	private HemorathVisualRules() {
	}

	/** A stage's scale at meter fraction {@code f}: 0 while hidden, then 0.8 to 1.15 once shown (cords 0.6 to 1). */
	public static float stage(Stage stage, float f) {
		float x = HemoEasing.clamp01(f);
		return switch (stage) {
			case CORDS -> HemoEasing.lerp(CORDS_REST, 1.0F, HemoEasing.ramp(x, 0.0F, LOBES_FROM));
			case ARMS -> emerge(x, ARMS_FROM);
			case LOBES -> emerge(x, LOBES_FROM);
			case MASS -> emerge(x, MASS_FROM);
		};
	}

	/** Buds from 0 to 0.8 over BUD, then fills to 1.15 by a full meter. */
	private static float emerge(float f, float from) {
		if (f <= from) return 0.0F;
		if (f < from + BUD) return SHOWN_MIN * HemoEasing.ramp(f, from, from + BUD);
		return HemoEasing.lerp(SHOWN_MIN, SHOWN_MAX, HemoEasing.ramp(f, from + BUD, 1.0F));
	}

	public static float ribSpread(float f) {
		return RIB_SPREAD * HemoEasing.ramp(HemoEasing.clamp01(f), LOBES_FROM, 0.85F);
	}

	/** A patch {@code age} ticks after it erupted: 0 before, up to 1.15, settling at 1. */
	public static float patch(float age) {
		if (age < 0.0F) return 0.0F;
		float peak = PATCH_ERUPT * 0.55F;
		if (age < peak) return PATCH_OVERSHOOT * HemoEasing.ramp(age, 0.0F, peak);
		return HemoEasing.lerp(PATCH_OVERSHOOT, 1.0F, HemoEasing.ramp(age, peak, PATCH_ERUPT));
	}

	public static float glow(float f, boolean feedingOpening) {
		float glow = GLOW_BASE + GLOW_RANGE * HemoEasing.clamp01(f);
		return feedingOpening ? Math.max(glow, GLOW_OPEN) : glow;
	}

	public static float beatRate(float f) {
		return HemoEasing.lerp(BEAT_EMPTY, BEAT_FULL, HemoEasing.clamp01(f));
	}

	/** pow(max(0, sin(t x rate)), 4): a short throb. */
	public static float heartbeat(float time, float f) {
		float s = (float) Math.sin(time * beatRate(f));
		float positive = Math.max(0.0F, s);
		return positive * positive * positive * positive;
	}

	public static float width(float f) {
		return HemoEasing.lerp(WIDTH_SUNKEN, WIDTH_BRIMMING, HemoEasing.ramp(HemoEasing.clamp01(f), 0.0F,
				HemorathMeterRules.BRIMMING_FROM));
	}

	/**
	 * A move's time on its authored track: phase 2 and the Beholding deed change a wind-up's length, so the track's
	 * wind-up keys are stretched to the wind-up that actually runs, and everything after the commit keeps its own clock.
	 */
	public static float authoredTime(float t, int actualWindup, int authoredWindup) {
		if (actualWindup <= 0 || t >= actualWindup) return t - actualWindup + authoredWindup;
		return t * authoredWindup / (float) actualWindup;
	}

	/** Raking Claw's Beholding feint: the authored swing, frozen at tick 10 for 8 ticks. */
	public static float heldTime(float t) {
		int from = HemorathMove.HELD_FREEZE_FROM, ticks = HemorathMove.HELD_FREEZE_TICKS;
		if (t < from) return t;
		if (t < from + ticks) return from;
		return t - ticks;
	}

	/** A world effect's strength at {@code age} of {@code life}: a quick ease out after two ticks. */
	public static float fade(float age, int life) {
		return age < 0 || age >= life ? 0 : 1 - HemoEasing.smoothstep((age - 2) / (life - 2));
	}
}
