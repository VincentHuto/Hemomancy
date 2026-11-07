package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation envelopes, pure and shared by the server's bursts and the client's body (design sections 12 and 13).
 * The server owns damage, openings and victory; these say only how Putriciel looks: the char layer's alpha, the ember
 * layer's (zero outside the fire and the ending, so the rot never shows a fire texel), the growths' scale, the
 * purifier's face, the hunch, and the burst kinds the {@code PUTRICIEL_OFFERING} visual channel carries.
 */
public final class PutricielVisualRules {
	/** Render scale: the rig is generated at 0.7 so the atlas lands at 16 px per block (2 px a unit x 16 / 2.0). */
	public static final float SCALE = 2.0F;
	public static final int BURST_TICKS = 32, REST_TICKS = 40;
	/** Burst kinds, carried as the visual packet's count. */
	public static final int ROT_IMPACT = 1, FIRE_IMPACT = 2, ABSOLVE = 3, IGNITE = 4, EMBER = 5, SCORCH = 6, REST = 7,
			COMMUNION_BREAK = 8, CENSER_BURST = 9;
	/** The char layer at its height through the fire. */
	public static final float CHAR_FULL = 0.85F;
	/** The growths shrink to this through the fire and overshoot to {@link #REGROW_OVERSHOOT} as they come back. */
	public static final float GROWTH_BURNT = 0.35F;
	public static final float REGROW_OVERSHOOT = 1.1F;
	/** The chest's rest pitch, degrees: hunched in the rot, straight in the fire. */
	public static final float HUNCH_ROT = 18.0F, HUNCH_FIRE = 2.0F;
	/** Every animation track blends in from the last pose over this many ticks (resolution B-8). */
	public static final float BLEND_TICKS = 5.0F;
	/** Ignition's beats, ticks from its start: the blackening runs 10 to 30. */
	public static final float BLACKEN_START = 10.0F, BLACKEN_END = 30.0F;
	/** The Embering's regrowth runs over its first 60 ticks. */
	public static final float REGROW_TICKS = 60.0F;
	/**
	 * The ember layer's fade through the Embering: the seams go out with the body's embers (revision 2026-11-06, R1.6),
	 * which wink out by the Embering's tick 30.
	 */
	public static final float EMBER_FADE = 24.0F;
	/** The Kindling tremor shows from this fraction. */
	public static final float TREMOR_FROM = PutricielKindlingRules.TREMOR;

	private PutricielVisualRules() {
	}

	/** The look of the body at one moment. */
	public record Look(float charAlpha, float ember, float growth, float face, float hunchDegrees) {
	}

	/**
	 * The body's look in {@code stage}, {@code stageTime} ticks after it began, or by the ending's tick when
	 * {@code endingTime} is not negative.
	 */
	public static Look look(PutricielStage stage, float stageTime, float endingTime) {
		if (endingTime >= 0.0F) {
			return new Look(PutricielEndingRules.charAlpha(endingTime), PutricielEndingRules.ember(endingTime),
					PutricielEndingRules.growth(endingTime), PutricielEndingRules.face(endingTime),
					HemoEasing.lerp(HUNCH_FIRE, HUNCH_ROT + 10.0F, HemoEasing.ramp(endingTime, 0.0F, PutricielEndingRules.KNEEL_END)));
		}
		return switch (stage) {
			case ROT -> new Look(0.0F, 0.0F, 1.0F, 0.0F, HUNCH_ROT);
			case IGNITING -> {
				float b = HemoEasing.ramp(stageTime, BLACKEN_START, BLACKEN_END);
				yield new Look(CHAR_FULL * b, HemoEasing.ramp(stageTime, BLACKEN_START + 10.0F, BLACKEN_END + 6.0F),
						HemoEasing.lerp(1.0F, GROWTH_BURNT, b), b, HemoEasing.lerp(HUNCH_ROT, HUNCH_FIRE, b));
			}
			case BURNING -> new Look(CHAR_FULL, 1.0F, GROWTH_BURNT, 1.0F, HUNCH_FIRE);
			case EMBERING -> {
				float r = HemoEasing.clamp01(stageTime / REGROW_TICKS);
				yield new Look(CHAR_FULL * (1.0F - HemoEasing.smoothstep(r)), 1.0F - HemoEasing.ramp(stageTime, 0.0F, EMBER_FADE),
						regrow(r), 1.0F - HemoEasing.ramp(stageTime, 0.0F, 20.0F), HemoEasing.lerp(HUNCH_FIRE, HUNCH_ROT, HemoEasing.smoothstep(r)));
			}
		};
	}

	/** The regrowth with its overshoot: 0.35 to 1.1 to 1 over r 0..1. */
	public static float regrow(float r) {
		return HemoEasing.catmullRom(r, 0.0F, GROWTH_BURNT, 0.65F, REGROW_OVERSHOOT, 1.0F, 1.0F);
	}

	/** The tremor at a Kindling fraction: none under 75%, a fine shiver above. */
	public static float tremor(float kindlingFraction, boolean rot) {
		if (!rot) return 0.0F;
		return HemoEasing.ramp(kindlingFraction, TREMOR_FROM, 1.0F);
	}

	/** The palm's nodules lit for {@code absolutions}. */
	public static float progress(int absolutions) {
		return PutricielAbsolutionRules.progress(absolutions);
	}

	/** A burst's opacity over its life. */
	public static float fade(float age, int life) {
		return age < 0 || age >= life ? 0 : 1 - HemoEasing.smoothstep((age - 3) / (life - 3));
	}

	/** Bursts in the fire's colours: the only kinds that may carry crimson. */
	public static boolean fireBurst(int kind) {
		return kind == FIRE_IMPACT || kind == ABSOLVE || kind == IGNITE || kind == EMBER || kind == SCORCH;
	}
}
