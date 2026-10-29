package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

import net.minecraft.util.Mth;

/** The numbers of an Apotheos body that runs on spores instead of food. */
public final class SporeLoadRules {
	public static final float MAX = 100f;
	public static final float BURN_COST = 25f;
	public static final float BURN_HEAL = 6f;
	public static final int BURN_COOLDOWN_TICKS = 100;
	public static final int SINK_DELAY_TICKS = 40;
	public static final float SINK_HEAL = 2f;
	public static final int SINK_INTERVAL = 20;
	public static final double STILL_SPEED_SQR = 0.0004;

	public enum BurnResult { OK, TOO_FEW_SPORES, COOLING, WHOLE }

	private SporeLoadRules() {}

	/** Spores gather quickly in the dimension, slowly in the Overworld's Gardens, and nowhere else. */
	public static float gainPerSecond(boolean inDimension, boolean inOverworldBiome) {
		if (inDimension) return 2.0f;
		return inOverworldBiome ? 0.5f : 0.0f;
	}

	public static float withinCapacity(float load) {
		return Mth.clamp(load, 0f, MAX);
	}

	public static BurnResult burn(float load, float health, float maxHealth, boolean cooldownReady) {
		if (health >= maxHealth) return BurnResult.WHOLE;
		if (!cooldownReady) return BurnResult.COOLING;
		return load >= BURN_COST ? BurnResult.OK : BurnResult.TOO_FEW_SPORES;
	}

	/** Regrowth is the Gardens' network taking the body back: it needs that network, not a carried block. */
	public static boolean isSinking(boolean crouching, boolean onGround, double horizontalSpeedSqr,
			boolean fungalGroundBelow, boolean inGardens) {
		return crouching && onGround && horizontalSpeedSqr < STILL_SPEED_SQR && fungalGroundBelow && inGardens;
	}

	/** Regrowth heals once the body has been still for the delay, then once per interval. */
	public static float sinkHeal(int stillTicks) {
		return stillTicks >= SINK_DELAY_TICKS && (stillTicks - SINK_DELAY_TICKS) % SINK_INTERVAL == 0
				? SINK_HEAL : 0f;
	}
}
