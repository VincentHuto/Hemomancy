package com.vincenthuto.hemomancy.common.world.answer;

/**
 * The world's answer (core-loop Track 10), the pure numbers. Blood left in the world (a stain, chum, the blood of a
 * creature a Harbinger killed) is counted per region of {@link #REGION_BLOCKS} blocks square and fades by half every
 * {@link #HALF_LIFE_TICKS}. The count rises from waste, never from blood put to work: a stain or a pool counts, a
 * machine fed, a rite paid or a Condenser filled does not. Absorbing a stain takes its count back. A Suspended
 * Vivianite ward in the region halves what is counted there and halves the half-life, up to {@link #MAX_WARDS}.
 *
 * <p>What reads the count: the Blood Moon's nightly chance (by moon phase, raised by the count where players stand),
 * the Wills' ambush chance, the scavengers wet stains and strong chum draw, and the ground under old stains in a heavy
 * region. The Humoral Barometer shows a region's band; the Ossuary Clock the phase and tonight's chance.
 */
public final class WorldAnswerRules {
	/** A region is 4 x 4 chunks. */
	public static final int REGION_SHIFT = 6;
	public static final int REGION_BLOCKS = 1 << REGION_SHIFT;
	/** The count halves every two in-game days. */
	public static final long HALF_LIFE_TICKS = 48_000L;
	/** A region holding less than this is forgotten. */
	public static final double GONE_ML = 1.0D;
	/** The count never climbs past this; a region is as heavy as it gets. */
	public static final double MAX_ML = 12_000.0D;
	public static final int MAX_WARDS = 3;
	/** Where each band starts: stirring, uneasy, heavy, seething. Below the first the region is still. */
	private static final double[] BAND_FLOORS = {200.0D, 600.0D, 1_500.0D, 3_000.0D};
	public static final int STILL = 0;
	public static final int STIRRING = 1;
	public static final int UNEASY = 2;
	public static final int HEAVY = 3;
	public static final int SEETHING = 4;
	/** The count at which pressure is full. */
	public static final double FULL_PRESSURE_ML = 3_000.0D;

	// ── What counts ──
	/** A stain counts its blood; a body's stain the same as a paid one. */
	public static final double STAIN_WEIGHT = 1.0D;
	/** Chum thins in the water: half its blood counts. */
	public static final double CHUM_WEIGHT = 0.5D;
	/** The blood a creature a Harbinger kills leaves, per 20 points of its maximum health (at least one share). */
	public static final double KILL_SPILL = 6.0D;
	/** Kills stop adding to a region's count once it holds this much, the uneasy floor (CB-26, NQ-02-7). */
	public static final double KILL_SHARE_CAP_ML = 600.0D;

	// ── The Blood Moon ──
	/** The nightly chance by moon phase (0 full, 4 new): highest at the full moon, none at the new. */
	private static final double[] MOON_BY_PHASE = {0.25D, 0.10D, 0.04D, 0.01D, 0.0D, 0.01D, 0.04D, 0.10D};
	/** Full pressure multiplies the phase's chance by 1 + this. */
	public static final double MOON_PRESSURE_GAIN = 3.0D;
	public static final double MOON_CAP = 0.9D;
	/** Tonight's chance at or above which the Ossuary Clock shows the omen. */
	public static final double OMEN_CHANCE = 0.10D;

	// ── Wills ──
	/** Full pressure multiplies the ambush chance by 1 + this. */
	public static final double WILL_PRESSURE_GAIN = 2.0D;

	// ── Scavengers ──
	/** Wet stains near a player that draw a scavenger. */
	public static final int SCAVENGER_STAINS = 3;
	public static final int SCAVENGER_RADIUS = 12;
	public static final int SCAVENGER_CHECK_TICKS = 200;
	public static final double SCAVENGER_BASE_CHANCE = 0.2D;
	public static final double SCAVENGER_CHANCE_PER_BAND = 0.15D;
	/** Scavenging bats near one player, at most. */
	public static final int SCAVENGER_CAP = 3;
	/** A chum pool at least this strong draws one of the sea's hunters, at night or in an uneasy region. */
	public static final double HUNTER_POOL_STRENGTH = 0.75D;
	public static final int HUNTER_CAP = 1;
	/** The water a hunter rises from is this deep at least: the open sea and home waters need two, a swamp's pond one. */
	public static final int HUNTER_DEPTH = 2;
	public static final int SWAMP_HUNTER_DEPTH = 1;

	/**
	 * Which hunter a strong pool draws, by the water it lies in (core-loop round two, P7): the Brined Votary out of the
	 * Court's home waters, a Bog Revenant out of a swamp, a Drowned anywhere else.
	 */
	public enum HunterWaters {
		OPEN, HOME, SWAMP
	}

	// ── The ground remembers ──
	/** A dry stain wearing away over grass in a region this heavy may leave stained sod. */
	public static final int SOD_BAND = HEAVY;
	public static final double SOD_CHANCE = 0.5D;
	/** Stained sod in a region at or below this band slowly turns back to grass. */
	public static final int SOD_RECOVERY_BAND = STIRRING;
	public static final double SOD_RECOVERY_CHANCE = 0.25D;

	private WorldAnswerRules() {
	}

	/** The region a block column belongs to, packed as one key. */
	public static long regionKey(int blockX, int blockZ) {
		long rx = blockX >> REGION_SHIFT;
		long rz = blockZ >> REGION_SHIFT;
		return (rx << 32) ^ (rz & 0xFFFFFFFFL);
	}

	public static int clampWards(int wards) {
		return Math.max(0, Math.min(MAX_WARDS, wards));
	}

	/** What one spill of {@code ml} adds to a region with {@code wards} wards: halved per ward. */
	public static double counted(double ml, int wards) {
		if (!(ml > 0.0D)) return 0.0D;
		return ml * Math.pow(0.5D, clampWards(wards));
	}

	/** What a region holding {@code ml} holds {@code ticks} later; a ward halves the half-life. */
	public static double decay(double ml, long ticks, int wards) {
		if (!(ml > 0.0D)) return 0.0D;
		if (ticks <= 0L) return ml;
		double halfLife = HALF_LIFE_TICKS * Math.pow(0.5D, clampWards(wards));
		double left = ml * Math.pow(0.5D, ticks / halfLife);
		return left < GONE_ML ? 0.0D : left;
	}

	/** A region holding {@code ml} after {@code added} (negative to clean), never below zero nor past the cap. */
	public static double add(double ml, double added) {
		return Math.max(0.0D, Math.min(MAX_ML, Math.max(0.0D, ml) + added));
	}

	/** The band of a region holding {@code ml}, {@link #STILL} to {@link #SEETHING}. */
	public static int band(double ml) {
		int band = STILL;
		for (double floor : BAND_FLOORS) {
			if (ml >= floor) band++;
		}
		return band;
	}

	/** Where a band starts, for teaching surfaces and tests. */
	public static double bandFloor(int band) {
		if (band <= STILL) return 0.0D;
		return BAND_FLOORS[Math.min(band, SEETHING) - 1];
	}

	/** Pressure, 0 to 1. */
	public static double pressure(double ml) {
		return Math.max(0.0D, Math.min(1.0D, ml / FULL_PRESSURE_ML));
	}

	/** The phase's chance of a Blood Moon, before any pressure. */
	public static double moonBase(int phase) {
		return MOON_BY_PHASE[Math.floorMod(phase, MOON_BY_PHASE.length)];
	}

	/** Tonight's chance of a Blood Moon at {@code phase} (0 full, 4 new) under {@code pressure}. */
	public static double moonChance(int phase, double pressure) {
		double base = moonBase(phase);
		if (base <= 0.0D) return 0.0D;
		return Math.min(MOON_CAP, base * (1.0D + MOON_PRESSURE_GAIN * Math.max(0.0D, Math.min(1.0D, pressure))));
	}

	/**
	 * Whether tonight's {@code roll} raised a Blood Moon only because of {@code pressure}: it fell under the raised chance
	 * and not under the phase's own. The same one roll decides the moon, so asking this never changes its odds.
	 */
	public static boolean broughtOn(int phase, double pressure, double roll) {
		return roll < moonChance(phase, pressure) && roll >= moonChance(phase, 0.0D);
	}

	/** Nights from a night of {@code phase} to the next full moon: 0 when it is full tonight. */
	public static int nightsToFullMoon(int phase) {
		return Math.floorMod(-phase, MOON_BY_PHASE.length);
	}

	/** The Wills' ambush chance multiplier under {@code pressure}. */
	public static double willMultiplier(double pressure) {
		return 1.0D + WILL_PRESSURE_GAIN * Math.max(0.0D, Math.min(1.0D, pressure));
	}

	/**
	 * What a kill adds to a region holding {@code regionMl}, given the {@code counted} its spill would add: kills lift a
	 * region to {@link #KILL_SHARE_CAP_ML} and no further, so heavy and seething need blood spilled some other way.
	 */
	public static double killCounted(double regionMl, double counted) {
		if (!(counted > 0.0D)) return 0.0D;
		return Math.max(0.0D, Math.min(counted, KILL_SHARE_CAP_ML - Math.max(0.0D, regionMl)));
	}

	/** The blood a killed creature of {@code maxHealth} leaves where it fell. */
	public static double killSpill(float maxHealth) {
		return KILL_SPILL * Math.max(1.0D, maxHealth / 20.0D);
	}

	/** The chance a check near {@code wetStains} wet stains draws a scavenger, by the region's band; zero below the threshold. */
	public static double scavengerChance(int wetStains, int band) {
		if (wetStains < SCAVENGER_STAINS) return 0.0D;
		return Math.min(1.0D, SCAVENGER_BASE_CHANCE + SCAVENGER_CHANCE_PER_BAND * Math.max(0, band));
	}

	/** Whether stained sod in a region of {@code band} turns back to grass on a tick that rolled {@code roll}. */
	public static boolean sodRecovers(int band, float roll) {
		return band <= SOD_RECOVERY_BAND && roll < SOD_RECOVERY_CHANCE;
	}

	/** The waters a pool lies in: home waters first (a reef swamp is still the Court's), then a swamp, else open water. */
	public static HunterWaters hunterWaters(boolean homeWaters, boolean swamp) {
		if (homeWaters) return HunterWaters.HOME;
		return swamp ? HunterWaters.SWAMP : HunterWaters.OPEN;
	}

	/** The least depth of water a hunter of {@code waters} rises from. */
	public static int hunterDepth(HunterWaters waters) {
		return waters == HunterWaters.SWAMP ? SWAMP_HUNTER_DEPTH : HUNTER_DEPTH;
	}

	/** Whether a chum pool of {@code strength} draws a hunter: strong enough, and night or an uneasy region. */
	public static boolean poolDrawsHunter(double strength, boolean night, int band) {
		return strength >= HUNTER_POOL_STRENGTH && (night || band >= UNEASY);
	}
}
