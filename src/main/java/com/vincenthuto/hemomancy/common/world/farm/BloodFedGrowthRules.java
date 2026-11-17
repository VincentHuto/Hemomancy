package com.vincenthuto.hemomancy.common.world.farm;

/**
 * Fed ground (core-loop Track 7), the pure numbers. One question for a crop: how well is the ground under it fed, from
 * 0 to 3? Level 1 is a drip from a scab spike above (or a wet stain beside the soil), cheap and wide; level 2 is a
 * planting basin holding fresh blood (or a lit feeding gouge beside the crop), direct and small; level 3 is the
 * cloche, one plant at the highest cost. A level buys extra growth chances on the crop's own growth checks; a crop that
 * needs blood (the gourd) does not advance at all on unfed ground and only slowly at level 1.
 */
public final class BloodFedGrowthRules {
	public static final int UNFED = 0;
	public static final int DRIP = 1;
	public static final int BASIN = 2;
	public static final int CLOCHE = 3;

	/** The extra chance, by level, that a growth check a crop would have failed grows it anyway. */
	private static final float[] BONUS = {0.0F, 0.25F, 0.5F, 1.0F};
	/** At level 1 a crop that needs blood keeps this share of its own growth checks; the rest are refused. */
	public static final float NEEDS_BLOOD_SLOW_SHARE = 1.0F / 3.0F;

	// ── The drip ──
	/** Blood one drip takes from what feeds the spike. */
	public static final double DRIP_ML = 4.0D;
	/** How far below its tip a drip can fall and still land. */
	public static final int DRIP_REACH = 10;
	/** A drip feeds the soil it lands on and the ring round it. */
	public static final int DRIP_SPLASH_RADIUS = 1;
	/** How long a drip keeps soil fed: several random ticks of the spike, so a fed spike keeps its ground fed. */
	public static final int DRIP_FEED_TICKS = 6_000;
	/** The chance a fed spike grows one block longer when it drips. */
	public static final float DRIP_GROW_CHANCE = 0.1F;
	/** The longest a fed spike grows. */
	public static final int MAX_SPIKE = 7;
	/** The chance a drop on bare scab or stone raises a spike from the floor, or one on a floor spike lengthens it. */
	public static final float FLOOR_GROW_CHANCE = 0.1F;
	/** The tallest a floor spike grows. */
	public static final int MAX_FLOOR_SPIKE = 3;
	/** Empty blocks a floor spike always leaves between its top and the hanging tip. */
	public static final int FLOOR_SPIKE_GAP = 2;

	// ── Tainted harvest ──
	/** The chance a ripe crop broken on over-fed ground (level 2) adds one Tainted Harvest to its drops. */
	public static final float TAINT_CHANCE = 0.08F;

	// ── Planting basins ──
	/** A planting basin's small clotting tank. */
	public static final double PLANTING_BASIN_CAPACITY = 500.0D;
	/** Blood a planting basin gives up for each extra growth it buys a crop. */
	public static final double BASIN_GROWTH_ML = 5.0D;
	/** Farmland a worked gutter keeps wet is fed at level 1 for this long after the stream last reached it. */
	public static final int GUTTER_FEED_TICKS = 1_200;

	// ── The cloche ──
	/** One growth stage of the cloche's planted crop every this many ticks while it has blood. */
	public static final int CLOCHE_STAGE_TICKS = 600;
	/** Blood one cloche growth stage costs. */
	public static final double CLOCHE_STAGE_ML = 25.0D;

	private BloodFedGrowthRules() {
	}

	/** What a crop's growth check becomes. */
	public enum Verdict {
		/** The crop decides by its own roll. */
		DEFAULT,
		/** The crop grows this check. */
		GROW,
		/** The crop does not grow this check. */
		DO_NOT_GROW
	}

	/** The extra growth chance at {@code level}, 0 for unfed ground. */
	public static float bonusChance(int level) {
		return BONUS[clamp(level)];
	}

	/**
	 * The ground's say on one growth check. A chilled crop (a lit cold gouge beside it) never grows. A crop that needs
	 * blood refuses on unfed ground, keeps a third of its checks at level 1 and its own speed from level 2. Any other
	 * crop keeps its own roll and gains {@link #bonusChance}. {@code roll} is uniform in [0, 1).
	 */
	public static Verdict verdict(int level, boolean needsBlood, boolean chilled, float roll) {
		int fed = clamp(level);
		if (chilled) return Verdict.DO_NOT_GROW;
		if (needsBlood) {
			if (fed == UNFED) return Verdict.DO_NOT_GROW;
			if (fed == DRIP) return roll < NEEDS_BLOOD_SLOW_SHARE ? Verdict.DEFAULT : Verdict.DO_NOT_GROW;
			return Verdict.DEFAULT;
		}
		return roll < bonusChance(fed) ? Verdict.GROW : Verdict.DEFAULT;
	}

	/**
	 * The level the ground holds, from what feeds it: a cloche, a fresh planting basin or feeding gouge, a drip's stamp
	 * or a wet stain. The best source wins; sources do not add.
	 */
	public static int level(boolean cloche, boolean basinOrFeedRun, boolean dripStamp, boolean wetStain) {
		if (cloche) return CLOCHE;
		if (basinOrFeedRun) return BASIN;
		if (dripStamp || wetStain) return DRIP;
		return UNFED;
	}

	/**
	 * Whether a drop raises a floor spike of {@code length} blocks (0 for bare floor) by one, when the block it would fill
	 * leaves {@code gapBelowTip} empty blocks under the hanging tip. {@code roll} is uniform in [0, 1).
	 */
	public static boolean floorSpikeMayRise(int length, int gapBelowTip, float roll) {
		return length < MAX_FLOOR_SPIKE && gapBelowTip >= FLOOR_SPIKE_GAP && roll < FLOOR_GROW_CHANCE;
	}

	/**
	 * Whether a crop's harvest is tainted: only a ripe crop, only on over-fed ground (a fresh planting basin or a feeding
	 * run, level 2; a drip or a stain never taints, and the cloche's sealed bed is not asked), on a roll under
	 * {@code chance}. {@code roll} is uniform in [0, 1).
	 */
	public static boolean taints(int level, boolean ripe, float roll, float chance) {
		return ripe && level == BASIN && roll < chance;
	}

	public static boolean taints(int level, boolean ripe, float roll) {
		return taints(level, ripe, roll, TAINT_CHANCE);
	}

	/** Whether a stamp that lasts until {@code until} still feeds at {@code now}. */
	public static boolean stampFeeds(long until, long now) {
		return until > now;
	}

	/** The later of two stamps: a new drip never shortens what an earlier one gave. */
	public static long restamp(long current, long now, int ticks) {
		return Math.max(current, now + ticks);
	}

	/** Whether the cloche's next growth stage is due. */
	public static boolean clocheStageDue(int ticksSinceStage) {
		return ticksSinceStage >= CLOCHE_STAGE_TICKS;
	}

	/** Ticks for a cloche to bring a crop of {@code stages} stages to harvest, and the blood it costs. */
	public static int clocheTicksToHarvest(int stages) {
		return Math.max(0, stages) * CLOCHE_STAGE_TICKS;
	}

	public static double clocheBloodToHarvest(int stages) {
		return Math.max(0, stages) * CLOCHE_STAGE_ML;
	}

	/** The expected share of growth checks that grow, given the crop's own chance and the level's bonus. */
	public static double expectedGrowth(double ownChance, int level, boolean needsBlood) {
		int fed = clamp(level);
		if (needsBlood) {
			if (fed == UNFED) return 0.0D;
			return fed == DRIP ? ownChance * NEEDS_BLOOD_SLOW_SHARE : ownChance;
		}
		double bonus = bonusChance(fed);
		return ownChance + (1.0D - ownChance) * bonus;
	}

	private static int clamp(int level) {
		return Math.max(UNFED, Math.min(CLOCHE, level));
	}
}
