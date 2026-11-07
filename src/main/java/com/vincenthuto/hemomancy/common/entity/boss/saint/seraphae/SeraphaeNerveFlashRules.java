package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/**
 * When her nerves fire gold (design section 12, "Layers"): a flash front runs a trunk from root to tip over 6 ticks
 * and leaves a 4-tick afterglow; idle, one seeded trunk fires every 70 to 110 ticks; a striking trunk fires at its
 * move's commit; the Convulsion fires every nerve at once in three stutters. Gold is only ever this nerve light, and
 * never the white vessel light. Pure, so it is JVM-tested.
 */
public final class SeraphaeNerveFlashRules {
	/** Ticks a front takes from a trunk's root to its tip fan. */
	public static final float TRAVEL = 6.0F;
	public static final float AFTERGLOW = 4.0F;
	/** Positions a front passes: three segments and the tip fan. */
	public static final int POSITIONS = 4;
	public static final int IDLE_PERIOD = 90;
	public static final int IDLE_JITTER = 20;
	public static final int TRUNKS_PER_SIDE = 3;
	public static final int TRUNKS = TRUNKS_PER_SIDE * 2;
	/** Move ticks of the Convulsion's three stutters, and how fast each fades. */
	public static final int[] STUTTERS = {6, 13, 20};
	public static final float STUTTER_FADE = 3.0F;

	private SeraphaeNerveFlashRules() {
	}

	/** One position's light {@code since} ticks after its trunk fired: dark until the front arrives, then fading. */
	public static float front(float since, int position) {
		if (since < 0.0F) return 0.0F;
		float arrival = TRAVEL * position / (POSITIONS - 1);
		float after = since - arrival;
		if (after < 0.0F) return 0.0F;
		return Math.max(0.0F, 1.0F - after / AFTERGLOW);
	}

	/** The tick idle cycle {@code n} fires, seeded: 70 to 110 ticks after the one before on average. */
	public static int idleFireTick(int seed, int n) {
		return n * IDLE_PERIOD + Math.round((hash(seed, n) - 0.5F) * IDLE_JITTER);
	}

	/** Which trunk (0..5: left upper, middle, lower, then right) idle cycle {@code n} fires. */
	public static int idleTrunk(int seed, int n) {
		return Math.min(TRUNKS - 1, (int) (hash(seed + 17, n) * TRUNKS));
	}

	/** Ticks since {@code trunk} last fired idly at {@code age}; negative when it has not fired within two cycles. */
	public static float sinceIdle(int seed, int trunk, float age) {
		int n = (int) Math.floor(age / IDLE_PERIOD) + 1;
		for (int cycle = n; cycle >= n - 2; cycle--) {
			if (cycle < 0) break;
			int at = idleFireTick(seed, cycle);
			if (at <= age && idleTrunk(seed, cycle) == trunk) return age - at;
		}
		return -1.0F;
	}

	/** The idle light on one position of one trunk. */
	public static float idle(int seed, int trunk, int position, float age) {
		return front(sinceIdle(seed, trunk, age), position);
	}

	/** Every nerve's light in the Convulsion's wind-up: 1 at each stutter, gone 3 ticks later. */
	public static float stutter(float moveTime) {
		float best = 0.0F;
		for (int at : STUTTERS) {
			float after = moveTime - at;
			if (after >= 0.0F && after < STUTTER_FADE) best = Math.max(best, 1.0F - after / STUTTER_FADE);
		}
		return best;
	}

	/** A trunk index from its side and its place (0 upper, 1 middle, 2 lower). */
	public static int trunk(boolean left, int place) {
		return (left ? 0 : TRUNKS_PER_SIDE) + Math.max(0, Math.min(TRUNKS_PER_SIDE - 1, place));
	}

	private static float hash(int seed, int n) {
		int h = seed * 0x9E3779B1 + n * 0x85EBCA77;
		h ^= h >>> 15;
		h *= 0x2C1B3C6D;
		h ^= h >>> 12;
		return (h & 0xFFFFFF) / (float) 0x1000000;
	}
}
