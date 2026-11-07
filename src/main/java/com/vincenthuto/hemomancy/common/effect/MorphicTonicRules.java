package com.vincenthuto.hemomancy.common.effect;

/**
 * The Morphic Tonic (D-4, ruled 2026-11-06): an Apotheos drinks it in the Gardens, in the dimension or the Overworld
 * Gardens biome, and nobody else drinks it at all. It purges harmful effects and gives Morphic Mending, which regrows
 * health and blood together.
 */
public final class MorphicTonicRules {
	public static final int MENDING_TICKS = 200;
	public static final int HEALTH_PULSE_TICKS = 25;
	public static final float HEALTH_PER_PULSE = 1.0F;
	/** 2.5 mL a tick: 500 mL over the ten seconds. */
	public static final double BLOOD_PER_TICK = 2.5D;

	public enum Refusal { NONE, NOT_APOTHEOS, OUTSIDE_GARDENS }

	private MorphicTonicRules() {
	}

	public static Refusal refusal(boolean apotheos, boolean inGardens) {
		if (!apotheos) return Refusal.NOT_APOTHEOS;
		return inGardens ? Refusal.NONE : Refusal.OUTSIDE_GARDENS;
	}

	public static double bloodPerTick(boolean mending) {
		return mending ? BLOOD_PER_TICK : 0.0D;
	}
}
