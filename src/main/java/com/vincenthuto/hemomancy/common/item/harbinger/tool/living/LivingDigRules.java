package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

/**
 * What a digging form of the Living Staff (the axe and the pickaxe) costs to use (core-loop Track 8). Each block it
 * breaks costs blood by the block's hardness; a player who cannot pay in full digs at a bare hand's speed and gets a
 * bare hand's drops. The forms have no durability, so blood is the only thing they wear.
 */
public final class LivingDigRules {
	/** mL per point of hardness: stone (1.5) costs 3 mL, deepslate and ores (3) cost 6. */
	public static final double ML_PER_HARDNESS = 2.0D;
	/** The most one block can cost. */
	public static final double MAX_ML_PER_BLOCK = 40.0D;
	/** A bare hand's dig speed, what an unpaid form falls back to. */
	public static final float HAND_SPEED = 1.0F;

	private LivingDigRules() {
	}

	/** The blood one block of {@code hardness} costs; nothing for blocks that break at once or never. */
	public static double bloodPerBlock(float hardness) {
		if (!(hardness > 0.0F)) return 0.0D;
		return Math.min(MAX_ML_PER_BLOCK, Math.max(1.0D, Math.ceil(ML_PER_HARDNESS * hardness)));
	}
}
