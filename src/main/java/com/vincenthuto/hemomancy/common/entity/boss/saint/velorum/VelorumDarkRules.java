package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.function.BiPredicate;

/**
 * The liquid dark's pure rules (design revision 2026-11-06, R.2 to R.4): the lantern's radius of cold light {@code L}
 * against the dark, the pools each source leaves (cap, growth, life), the Gutter's flood, the 45% cap on the boss
 * floor, who stands in the dark, and what the dark does to the stillness meter. The lantern and pool radii use the same
 * numbers and curves as {@code VelorumVisualRules}, so the edge the client draws is the edge the server tests.
 * JVM-tested.
 */
public final class VelorumDarkRules {
	/** {@code L} at rest by phase. */
	public static final float[] LANTERN = {6.0F, 5.0F, 4.0F};
	/** The Long Night's wind-up shrinks {@code L} to this by its commit. */
	public static final float NIGHT_LIGHT = 3.0F;
	/** The Guttering Lantern takes {@code L} to 1 within its first 10 ticks. */
	public static final float GUTTER_LIGHT = 1.0F;
	public static final int GUTTER_FALL = 10;
	/** After the Gutter closes {@code L} returns to the phase value over 20 ticks. */
	public static final int GUTTER_RETURN = 20;
	/** Relit, the lantern flares to 8 for 20 ticks, then eases back over 10. */
	public static final float RELIT_LIGHT = 8.0F;
	public static final int RELIT_FLARE = 20;
	public static final int RELIT_EASE = 10;
	/** The Eclipse starts from at most this radius as the fingers close. */
	public static final float ECLIPSE_FROM = 4.0F;

	/** The Gutter's flood: every live pool grows +0.25 a tick toward 7, and eases back to its own curve over 40. */
	public static final double FLOOD_RATE = 0.25D;
	public static final double FLOOD_CAP = 7.0D;
	public static final int FLOOD_RETURN = 40;

	/** Outside the Eclipse the dark covers at most 45% of the boss floor; past it the oldest pool fades over 20. */
	public static final double COVERAGE_CAP = 0.45D;
	public static final int EXPIRE_TICKS = 20;

	/** An awake ally's thawed blood is warm: the dark does not hold within 1.5 blocks of one. */
	public static final double WARMTH = 1.5D;

	/** The dark multiplies the standing, sneaking and item-use fill terms, and the drain. */
	public static final float FILL_FACTOR = 1.4F;
	public static final float DRAIN_FACTOR = 0.9F;

	/** The gloam the Long Night, the Gloam Cuts and the Shadow Step's arrival put on whom they strike. */
	public static final int GLOAM_TICKS = 60;

	/** What leaves a pool: the Long Night, the Shadow Step, a Gloam Cut, the Procession. */
	public enum Source { LONG_NIGHT, STEP, CUT, PROCESSION }

	public static final int LONG_NIGHT_POOLS = 4;
	private static final int[] LONG_NIGHT_LIFE = {300, 400, 500};
	private static final int[] CUT_LIFE = {160, 160, 240};
	public static final int STEP_LIFE = 200;
	/** The Procession's rising pool lives the move's span and 100 more; each shade's strip 120. */
	public static final int PROCESSION_EXTRA = 100;
	public static final int STRIP_LIFE = 120;
	public static final double STRIP_HALF_WIDTH = 0.6D;

	private VelorumDarkRules() {
	}

	private static int index(int phase) {
		return Math.max(0, Math.min(2, phase - 1));
	}

	// ── the lantern ──────────────────────────────────────────────────

	public static float lanternRest(int phase) {
		return LANTERN[index(phase)];
	}

	/**
	 * {@code L}, the lantern's radius of cold light round the floor point under it. {@code nightBuild} is the Long
	 * Night's wind-up, 0 to 1 (1 through its active span, 0 otherwise); {@code gutter} the ticks into an open Guttering
	 * Lantern (-1 when none is open); {@code sinceGutter} the ticks since one closed (-1 for none); {@code eclipse} how
	 * far the Eclipse has put the light out, 0 to 1; {@code relit} the ticks since the Eclipse was relit (-1 for none).
	 */
	public static float lanternRadius(int phase, float nightBuild, float gutter, float sinceGutter, float eclipse,
			float relit) {
		float rest = lanternRest(phase);
		if (relit >= 0.0F && relit < RELIT_FLARE + RELIT_EASE) {
			float back = HemoEasing.clamp01((relit - RELIT_FLARE) / RELIT_EASE);
			return HemoEasing.lerp(RELIT_LIGHT, rest, HemoEasing.smoothstep(back));
		}
		if (eclipse > 0.0F) return HemoEasing.lerp(Math.min(rest, ECLIPSE_FROM), 0.0F, HemoEasing.clamp01(eclipse));
		if (gutter >= 0.0F) {
			float fall = HemoEasing.smoothstep(HemoEasing.clamp01(gutter / GUTTER_FALL));
			return HemoEasing.lerp(Math.min(rest, NIGHT_LIGHT), GUTTER_LIGHT, fall);
		}
		if (sinceGutter >= 0.0F && sinceGutter < GUTTER_RETURN) {
			return HemoEasing.lerp(GUTTER_LIGHT, rest, HemoEasing.smoothstep(sinceGutter / GUTTER_RETURN));
		}
		if (nightBuild > 0.0F) return HemoEasing.lerp(rest, NIGHT_LIGHT, HemoEasing.clamp01(nightBuild));
		return rest;
	}

	/** The dark cannot hold inside {@code L}: a point this far from the lantern's floor point is lit. */
	public static boolean lit(double fromLantern, double lantern) {
		return fromLantern < lantern;
	}

	// ── pools ────────────────────────────────────────────────────────

	/** The radius a source's pool spreads to: 3.5 for the Long Night in phase 1, 4.5 after; 2.5, 1.5 and 2.5. */
	public static double cap(Source source, int phase) {
		return switch (source) {
			case LONG_NIGHT -> phase <= 1 ? 3.5D : 4.5D;
			case STEP, PROCESSION -> 2.5D;
			case CUT -> 1.5D;
		};
	}

	/** A source's pool life by phase; the Procession's is its move's span plus 100 (the caller adds the span). */
	public static int life(Source source, int phase) {
		return switch (source) {
			case LONG_NIGHT -> LONG_NIGHT_LIFE[index(phase)];
			case STEP -> STEP_LIFE;
			case CUT -> CUT_LIFE[index(phase)];
			case PROCESSION -> PROCESSION_EXTRA;
		};
	}

	/** How many ticks a pool of cap radius {@code cap} grows over (R.3's table: 30 for the Long Night, 8, then 6). */
	public static int growTicks(double cap) {
		if (cap >= 3.4D) return 30;
		if (cap >= 2.4D) return 8;
		return 6;
	}

	/** A pool's own radius at {@code age}, grown over its source's ticks with an ease-out. */
	public static double ownRadius(double cap, double age) {
		if (age <= 0.0D) return 0.0D;
		return cap * HemoEasing.easeOutCubic(HemoEasing.clamp01(age / growTicks(cap)));
	}

	/**
	 * A pool's radius through the Gutter's flood: {@code flood} ticks of +0.25 a tick toward 7 while it gutters
	 * ({@code sinceFlood} -1), easing back to its own curve over 40 ticks once it closed ({@code flood} is then the
	 * Gutter's whole length).
	 */
	public static double floodedRadius(double radius, double flood, double sinceFlood) {
		double grown = Math.min(FLOOD_CAP, radius + FLOOD_RATE * Math.max(0.0D, flood));
		if (sinceFlood < 0.0D) return flood > 0.0D ? Math.max(radius, grown) : radius;
		return HemoEasing.lerp(Math.max(radius, grown), radius,
				HemoEasing.smoothstep(HemoEasing.clamp01(sinceFlood / FLOOD_RETURN)));
	}

	/** The pool's radius at {@code age}, through any flood ({@code flood} 0 and {@code sinceFlood} -1 for none). */
	public static double poolRadius(double cap, double age, double flood, double sinceFlood) {
		double own = ownRadius(cap, age);
		if (flood <= 0.0D || sinceFlood >= FLOOD_RETURN) return own;
		return floodedRadius(own, flood, sinceFlood);
	}

	/**
	 * The fraction of the box ({@code minX..maxX}, {@code minZ..maxZ}, block cells) whose cell centres are dark, by
	 * {@code dark(x, z)}: how much of the boss floor the pools cover.
	 */
	public static double coverage(double minX, double minZ, double maxX, double maxZ, BiPredicate<Double, Double> dark) {
		int cells = 0, covered = 0;
		for (double x = Math.floor(minX) + 0.5D; x < maxX; x += 1.0D) {
			for (double z = Math.floor(minZ) + 0.5D; z < maxZ; z += 1.0D) {
				cells++;
				if (dark.test(x, z)) covered++;
			}
		}
		return cells == 0 ? 0.0D : (double) covered / cells;
	}

	/** The leash ring: the boss floor and the block round it; the dark never runs into the aisles past it. */
	public static final double LEASH = 1.0D;

	/** A pool's centre held on the leash box ({@code minX..maxX}, {@code minZ..maxZ}), half a block in: {x, z}. */
	public static double[] onLeash(double x, double z, double minX, double minZ, double maxX, double maxZ) {
		return new double[]{Math.max(minX + 0.5D, Math.min(maxX - 0.5D, x)), Math.max(minZ + 0.5D, Math.min(maxZ - 0.5D, z))};
	}

	/** Whether a point stands on the leash box, where the dark can hold. */
	public static boolean leashed(double x, double z, double minX, double minZ, double maxX, double maxZ) {
		return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
	}

	/** Whether the pools, as they stand, cover more of the boss floor than the cap allows. */
	public static boolean overCap(double coverage) {
		return coverage > COVERAGE_CAP + 1.0E-9D;
	}

	// ── who stands in the dark ───────────────────────────────────────

	/**
	 * A challenger is in the dark inside a live pool, outside {@code L}, away from an awake ally's warmth and without
	 * Searing on them. The Eclipse puts everyone in the dark but for an ally's warmth.
	 */
	public static boolean inDark(boolean inPool, boolean lit, boolean warmed, boolean searing, boolean eclipse) {
		if (warmed) return false;
		if (eclipse) return true;
		return inPool && !lit && !searing;
	}

	/** The fill's standing, sneaking and item-use terms in the dark (the Slowness and Rime terms are not multiplied). */
	public static float fillFactor(boolean dark) {
		return dark ? FILL_FACTOR : 1.0F;
	}

	public static float drainFactor(boolean dark) {
		return dark ? DRAIN_FACTOR : 1.0F;
	}
}
