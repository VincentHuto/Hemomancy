package com.vincenthuto.hemomancy.client.boss;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBurrowRules;
import net.minecraft.util.Mth;

/**
 * Camera shake for the Burrow's rumble: silent while the mount hunts, building steadily once the trail goes quiet,
 * peaking as it erupts and dying away while it gathers itself. Driven purely from the synced burrow tick.
 */
public final class VesperRumbleClientState {
	public static final float MAX_AMPLITUDE = 2.6F;

	private VesperRumbleClientState() {
	}

	public static float amplitude(VesperBurrowRules.Plan plan, float burrowTick) {
		float rumble = burrowTick - VesperBurrowRules.rumbleStart(plan);
		if (rumble < 0.0F) return 0.0F;
		if (rumble < VesperBurrowRules.RUMBLE_TICKS) {
			float build = rumble / VesperBurrowRules.RUMBLE_TICKS;
			return MAX_AMPLITUDE * (0.15F + 0.85F * build * build);
		}
		float eruption = rumble - VesperBurrowRules.RUMBLE_TICKS;
		if (eruption < VesperBurrowRules.ERUPTION_TICKS) {
			return MAX_AMPLITUDE * (1.0F - 0.6F * eruption / VesperBurrowRules.ERUPTION_TICKS);
		}
		return 0.0F;
	}

	public static float pitchShake(float amplitude, float time) {
		return amplitude * (Mth.sin(time * 1.9F) * 0.6F + Mth.sin(time * 4.3F + 1.1F) * 0.4F);
	}

	public static float rollShake(float amplitude, float time) {
		return amplitude * 0.55F * Mth.sin(time * 2.7F + 0.6F);
	}
}
