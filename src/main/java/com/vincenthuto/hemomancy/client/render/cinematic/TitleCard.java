package com.vincenthuto.hemomancy.client.render.cinematic;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * A boss's name card on the lower third: the epithet above, the name below, and a rule in the boss's colour drawn out
 * from the middle. It shows from {@code start} for {@code length} ticks, easing in and out over {@link #FADE} ticks.
 * The keys are {@code boss.hemomancy.<id>.name} and {@code .epithet}.
 */
public record TitleCard(String nameKey, String epithetKey, int start, int length, int ruleColor) {
	public static final int FADE = 12;
	public static final int RULE_TICKS = 16;

	public static TitleCard of(String id, int start, int length, int ruleColor) {
		return new TitleCard("boss.hemomancy." + id + ".name", "boss.hemomancy." + id + ".epithet", start, length, ruleColor);
	}

	public float alpha(float t) {
		return BossSceneTracks.window(t, start, FADE, start + length, FADE);
	}

	public float rule(float t) {
		return HemoEasing.smoothstep(HemoEasing.clamp01((t - start) / RULE_TICKS));
	}
}
