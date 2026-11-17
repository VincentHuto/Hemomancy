package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

/**
 * How the Living Staff ages (core-loop Track 8). The blood a player has moved through the Staff
 * ({@code LivingStaffProgress.bloodHandled}) lays down growth rings; each ring is a small permanent gain. The skill
 * branch and grafts stay the chosen half of the Staff's growth; rings are the earned half. Free of Minecraft types so
 * the numbers are tested on the JVM.
 */
public final class LivingStaffGrowthRules {
	/** mL handled at which each ring forms, first ring first. */
	private static final double[] THRESHOLDS = { 5_000.0D, 25_000.0D, 100_000.0D, 300_000.0D, 750_000.0D };
	public static final int MAX_RING = THRESHOLDS.length;

	/** Ring 3 moves blood a tenth faster through the Staff. */
	public static final double RING_THREE_CHANNEL_RATE = 1.10D;

	/** Which vanilla tool tier's blocks a digging form harvests. */
	public enum Harvest { STONE, IRON, DIAMOND }

	/**
	 * What a digging form (axe, pickaxe) mines like: its harvest tier and its speed on what it is made for. At the
	 * bond it already takes iron ore; diamonds wait for the second ring and obsidian for the fourth, and it never
	 * reaches a netherite tool's tier or speed.
	 */
	public enum DigTier {
		/** At the bond: stone harvest (iron, copper and lapis ore, not diamond), at a stone tool's speed. */
		BONDED(Harvest.STONE, 4.0F),
		/** Ring 2: iron harvest (diamond, gold, redstone and emerald ore) at iron speed. */
		IRON(Harvest.IRON, 6.0F),
		/** Ring 4: diamond harvest (obsidian, ancient debris) and speed. Never netherite. */
		DIAMOND(Harvest.DIAMOND, 8.0F);

		private final Harvest harvest;
		private final float speed;

		DigTier(Harvest harvest, float speed) {
			this.harvest = harvest;
			this.speed = speed;
		}

		/** The vanilla tier whose blocks the form harvests. */
		public Harvest harvest() {
			return harvest;
		}

		public float speed() {
			return speed;
		}
	}

	private LivingStaffGrowthRules() {
	}

	/** How many rings {@code bloodHandled} mL have laid down, 0 to {@link #MAX_RING}. */
	public static int ring(double bloodHandled) {
		if (!Double.isFinite(bloodHandled) || bloodHandled <= 0.0D) return 0;
		int ring = 0;
		while (ring < THRESHOLDS.length && bloodHandled >= THRESHOLDS[ring]) ring++;
		return ring;
	}

	/** The mL at which ring {@code ring} forms (1-based); 0 for ring 0 or less, the last threshold past the end. */
	public static double threshold(int ring) {
		if (ring <= 0) return 0.0D;
		return THRESHOLDS[Math.min(ring, THRESHOLDS.length) - 1];
	}

	/** The mL still to move before the next ring, or 0 once every ring is laid. */
	public static double untilNextRing(double bloodHandled) {
		int ring = ring(bloodHandled);
		if (ring >= MAX_RING) return 0.0D;
		return Math.max(0.0D, THRESHOLDS[ring] - Math.max(0.0D, bloodHandled));
	}

	/** Blocks added to the verbs' reach through the Staff: +0.5 at ring 1, +1 more at ring 3, +1 more at ring 5. */
	public static double reachBonus(int ring) {
		double bonus = 0.0D;
		if (ring >= 1) bonus += 0.5D;
		if (ring >= 3) bonus += 1.0D;
		if (ring >= 5) bonus += 1.0D;
		return bonus;
	}

	/** The Staff's transfer rates (blood moved per tick, draw per pulse) times this: +10% from ring 3. */
	public static double channelRateMultiplier(int ring) {
		return ring >= 3 ? RING_THREE_CHANNEL_RATE : 1.0D;
	}

	/** The digging forms' tier: iron ore and below at the bond, iron tier at ring 2, diamond tier at ring 4. */
	public static DigTier digTier(int ring) {
		if (ring >= 4) return DigTier.DIAMOND;
		if (ring >= 2) return DigTier.IRON;
		return DigTier.BONDED;
	}

	/** The least a hot swap between forms can cost: 50 mL, 25 mL once ring 5 is laid. */
	public static double hotSwapFloor(int ring) {
		return ring >= 5 ? 25.0D : LivingStaffWeaponFormRules.MIN_HOT_SWAP_COST_ML;
	}

	/** Ring 5 takes 25 mL off every hot swap, down to its lower floor. */
	public static double hotSwapRingDiscount(int ring) {
		return ring >= 5 ? 25.0D : 0.0D;
	}

	/** Clamps a stamped or synced ring count into range. */
	public static int clampRing(int ring) {
		return Math.max(0, Math.min(MAX_RING, ring));
	}
}
