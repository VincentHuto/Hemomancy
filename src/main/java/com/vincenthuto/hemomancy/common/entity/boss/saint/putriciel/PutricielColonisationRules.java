package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import net.minecraft.util.Mth;

/**
 * Colonisation, Putriciel's presence on each challenger (design section 5): 0 to 20, four thresholds, its decay and
 * its cures. Pure, so it is JVM-tested. On the Saint it rides the kit meter's per-player state: the exact value is the
 * state's secondary (maximum 20); the flag word carries it in halves (bits 0 to 5) with the Fruiting (6), Overgrown (7)
 * and burning (8) bits, so {@code DATA_CHALLENGERS} shows every challenger's growth to every client.
 */
public final class PutricielColonisationRules {
	public static final float MAX = 20.0F;
	public static final float ROOTED = 5.0F;
	public static final float SHELVED = 10.0F;
	public static final float FRUITING = 15.0F;
	public static final float OVERGROWN = 20.0F;
	/** Where Overgrown leaves it. */
	public static final float OVERGROWN_FALL_TO = 12.0F;

	/** Rooted: movement speed, multiplied total. */
	public static final double ROOTED_SPEED = -0.10D;
	/** Shelved: healing received. */
	public static final float SHELVED_HEAL = 0.6F;
	/** How long a refreshed Shelved heal cut holds; it is refreshed every pulse. */
	public static final int SHELVED_HOLD = 25;
	public static final int FRUIT_EVERY = 40;
	public static final float FRUIT_SELF_DAMAGE = 1.0F;
	public static final float FRUIT_SPREAD = 1.0F;
	public static final double FRUIT_RADIUS = 3.0D;
	public static final int OVERGROWN_ROOT = 30;
	public static final float OVERGROWN_DAMAGE = 6.0F;

	/** Outside the presence in the rot: -1 a 40 ticks. */
	public static final int DECAY_EVERY = 40;
	public static final float DECAY = 1.0F;
	/** A Flammeus manipulation the player lands: self-cautery. */
	public static final float FLAMMEUS_CAUTERY = 3.0F;
	/** The purging heat in the interval: within 4, -2 a 10 ticks, 1 cleansing damage a 20. */
	public static final float HEAT_CLEANSE = 2.0F;
	public static final int HEAT_EVERY = 10;
	public static final float HEAT_DAMAGE = 1.0F;
	public static final int HEAT_DAMAGE_EVERY = 20;

	public static final int HALVES_MASK = 0x3F;
	public static final int FLAG_FRUITING = 1 << 6;
	public static final int FLAG_OVERGROWN = 1 << 7;
	public static final int FLAG_BURNING = 1 << 8;

	private PutricielColonisationRules() {
	}

	public static float bounded(float value) {
		return Mth.clamp(value, 0.0F, MAX);
	}

	/** 0 bare, 1 Rooted, 2 Shelved, 3 Fruiting, 4 Overgrown. */
	public static int tier(float value) {
		if (value >= OVERGROWN) return 4;
		if (value >= FRUITING) return 3;
		if (value >= SHELVED) return 2;
		return value >= ROOTED ? 1 : 0;
	}

	public static boolean rooted(float value) {
		return value >= ROOTED;
	}

	public static boolean shelved(float value) {
		return value >= SHELVED;
	}

	public static boolean fruiting(float value) {
		return value >= FRUITING;
	}

	public static boolean overgrows(float value) {
		return value >= OVERGROWN;
	}

	/** The rot presence's gain a pulse in {@code phase}; nothing in the interval. */
	public static float gain(PutricielPhase phase) {
		return phase.rot() ? phase.colonisationGain() : 0.0F;
	}

	public static double radius(PutricielPhase phase) {
		return phase.presenceRadius();
	}

	/** The flag word: halves in bits 0 to 5, then Fruiting, Overgrown and burning. */
	public static int flags(float value, boolean overgrown, boolean burning) {
		int halves = Mth.clamp(Math.round(bounded(value) * 2.0F), 0, HALVES_MASK);
		int flags = halves;
		if (fruiting(value)) flags |= FLAG_FRUITING;
		if (overgrown) flags |= FLAG_OVERGROWN;
		if (burning) flags |= FLAG_BURNING;
		return flags;
	}

	/** The colonisation a flag word carries. */
	public static float value(int flags) {
		return (flags & HALVES_MASK) / 2.0F;
	}

	public static boolean overgrown(int flags) {
		return (flags & FLAG_OVERGROWN) != 0;
	}

	public static boolean burning(int flags) {
		return (flags & FLAG_BURNING) != 0;
	}

	public static boolean fruitingFlag(int flags) {
		return (flags & FLAG_FRUITING) != 0;
	}
}
