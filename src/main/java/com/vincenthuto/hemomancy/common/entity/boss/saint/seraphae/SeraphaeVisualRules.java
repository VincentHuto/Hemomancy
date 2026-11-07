package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Seraphae's shared visual numbers: the render scale (16 px per block at {@code kit.SCALE = 2}), the server cue kinds
 * her geometry answers, their lives, the nerve wave the trunks sway on and the cues' fade. Decorative only: nothing here
 * gates damage or containment.
 */
public final class SeraphaeVisualRules {
	/** Render scale: 2 texels per model unit at this scale is 16 per block. */
	public static final float MODEL_SCALE = 2.0F;
	/** The generator's texels per model unit; {@code TEXELS * 16 / MODEL_SCALE} is the density per block. */
	public static final int TEXELS = 2;

	// cue kinds, carried as the cue's count
	public static final int HIT = 1, FRACTURE_BURST = 2, CLOSE = 3, CAPTURE = 4, SEALED = 5, PAD = 6,
			TRANSFUSION = 7, GROWTH_BURST = 8, KNOT = 9, SLIP = 10, FLARE = 11, WAKE = 12, RECOIL = 13;
	public static final int BURST_TICKS = 24, SEAL_TICKS = 44, CLOSE_TICKS = 8, KNOT_TICKS = 18;

	private SeraphaeVisualRules() {
	}

	/** A cue's life by kind. */
	public static int life(int kind) {
		return switch (kind) {
			case SEALED -> SEAL_TICKS;
			case CLOSE -> CLOSE_TICKS;
			case KNOT, CAPTURE -> KNOT_TICKS;
			default -> BURST_TICKS;
		};
	}

	/** A seeded, delayed wave down a nerve trunk: the tips lag the root instead of moving as one rigid sheet. */
	public static float nerveWave(float age, int segment, int side, float strain) {
		return (float) Math.sin(age * (.09F + strain * .2F) - segment * .7F + side * .7F)
				* (.04F + strain * .12F) * (1 + segment * .15F);
	}

	public static float burstOpacity(float age, float life) {
		if (age < 0 || age >= life) return 0;
		return 1 - HemoEasing.smoothstep((age - 3) / (life - 3));
	}
}
