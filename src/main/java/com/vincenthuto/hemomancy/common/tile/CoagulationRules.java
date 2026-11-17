package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.config.HemoConfigValues;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

/**
 * Living blood (core-loop Track 3): blood is fuel only while something alive holds it. In an inert container it
 * stays fresh for {@link #FRESH_WINDOW_TICKS} after it was last fed, then a share of what is still fresh clots each
 * minute, and an hour after feeding all of it is clot. Clot is not fuel; it comes out as Sanguine Formations, at a
 * poor yield from a neglected tank and at the best yield from a Sanguine Condenser, which clots blood on purpose.
 */
public final class CoagulationRules {
	/** Five minutes fresh after the last feeding (CL-5). */
	public static final long FRESH_WINDOW_TICKS = 6_000L;
	/** Past the window, 10% of what is still fresh clots each minute (CL-5). */
	public static final double CLOT_SHARE_PER_MINUTE = 0.10D;
	public static final long MINUTE_TICKS = 1_200L;
	/** An hour after the last feeding nothing in an inert container is fresh. */
	public static final long FULL_CLOT_TICKS = 72_000L;
	/** A neglected tank gives one Formation for this much clot; the Condenser does far better. */
	public static final double NEGLECT_ML_PER_FORMATION = 400.0D;
	/** Below this much clot a tank is clean; the smear is not worth a Formation. */
	public static final double CLOT_EPSILON = 1.0E-6D;

	private CoagulationRules() {
	}

	/** Share of blood fed {@code elapsed} ticks ago that is still fresh. */
	public static double freshFraction(long elapsed) {
		if (elapsed <= FRESH_WINDOW_TICKS) return 1.0D;
		if (elapsed >= FULL_CLOT_TICKS) return 0.0D;
		double minutes = (elapsed - FRESH_WINDOW_TICKS) / (double) MINUTE_TICKS;
		return Math.pow(1.0D - CLOT_SHARE_PER_MINUTE, minutes);
	}

	/**
	 * How much of {@code fresh} clots between two reads, {@code fromElapsed} and {@code toElapsed} ticks after the
	 * last feeding. Settling in many small steps clots exactly as much as settling once.
	 */
	public static double clotBetween(double fresh, long fromElapsed, long toElapsed) {
		if (fresh <= 0.0D || toElapsed <= fromElapsed) return 0.0D;
		double from = freshFraction(Math.max(0L, fromElapsed));
		if (from <= 0.0D) return fresh;
		double kept = freshFraction(toElapsed) / from;
		return fresh * (1.0D - Math.clamp(kept, 0.0D, 1.0D));
	}

	/**
	 * How the blood reads, for screens and the Vascular Status Gauge: 1 while freshly fed, falling to 0 at the end of
	 * the fresh window. Shown as colour draining from the blood, never as a countdown.
	 */
	public static double freshness(long elapsed) {
		if (elapsed <= 0L) return 1.0D;
		return Math.clamp(1.0D - elapsed / (double) FRESH_WINDOW_TICKS, 0.0D, 1.0D);
	}

	/** A vessel's bottled stamp, rounded down to the minute, so vessels bottled close together stack (FX-2). */
	public static long bottledStamp(long gameTime) {
		return Math.floorDiv(gameTime, MINUTE_TICKS) * MINUTE_TICKS;
	}

	/** What distilling a clotting vessel gives: the recipe's count by the vessel's fresh share (FX-2). */
	public static int freshCount(int count, double freshShare) {
		return (int) Math.round(count * Math.clamp(freshShare, 0.0D, 1.0D));
	}

	// ── Reviving clot (CQ-6, LP-3, LP-4) ─────────────────────────────────────────

	/**
	 * One pour into a tank: {@code spent} mL went to waking clot and is used up, {@code woken} mL of clot came back as
	 * fresh blood, and {@code fresh} mL went in as fresh blood. The pour took {@link #taken}.
	 */
	public record Pour(double spent, double woken, double fresh) {
		public static final Pour NONE = new Pour(0.0D, 0.0D, 0.0D);

		public double taken() {
			return spent + fresh;
		}
	}

	public static final double DEFAULT_REVIVE_SHARE = 0.5D;
	public static final double DEFAULT_REVIVE_YIELD = 2.0D;
	public static final double DEFAULT_SUTURED_REVIVE_YIELD = 4.0D;

	/** Share of a pour that goes to the clot while a tank holds clot (NQ-01-2: half). */
	public static double reviveShare() {
		return HemoConfigValues.get(HemoServerConfig.CLOT_REVIVE_SHARE, DEFAULT_REVIVE_SHARE);
	}

	/** Clot each spent mL of a Projection wakes (NQ-01-2: 2). */
	public static double reviveYield() {
		return HemoConfigValues.get(HemoServerConfig.CLOT_REVIVE_YIELD, DEFAULT_REVIVE_YIELD);
	}

	/** Clot each spent mL of a suture's draw wakes (NQ-01-7: 4). */
	public static double suturedReviveYield() {
		return HemoConfigValues.get(HemoServerConfig.SUTURED_CLOT_REVIVE_YIELD, DEFAULT_SUTURED_REVIVE_YIELD);
	}

	/**
	 * How a pour of {@code offered} mL splits in a tank with {@code room} mL free and {@code clot} mL of clot. While
	 * there is clot, {@code share} of the pour wakes it, each spent mL waking {@code yield} mL; the rest goes in fresh.
	 * What does not fit as fresh blood also goes to the clot, so a tank with no room spends the whole pour reviving
	 * (NQ-01-8). Once the clot is gone the rest of the pour is fresh.
	 */
	public static Pour pour(double offered, double room, double clot, double share, double yield) {
		if (offered <= 0.0D) return Pour.NONE;
		room = Math.max(0.0D, room);
		double needed = clot > CLOT_EPSILON && yield > 0.0D ? clot / yield : 0.0D;
		double spent = Math.min(needed, offered * Math.clamp(share, 0.0D, 1.0D));
		double fresh = Math.min(room, offered - spent);
		spent += Math.min(needed - spent, offered - spent - fresh);
		return new Pour(spent, Math.min(clot, spent * yield), fresh);
	}

	/**
	 * The age of a tank's fresh blood after {@code added} mL at age {@code addedAge} joins {@code held} mL at age
	 * {@code age}: the ages blend by volume, so a small top-up barely changes an old tank (NQ-01-3).
	 */
	public static long blendedAge(double held, long age, double added, long addedAge) {
		held = Math.max(0.0D, held);
		added = Math.max(0.0D, added);
		if (held + added <= 0.0D) return 0L;
		return Math.round((held * Math.max(0L, age) + added * Math.max(0L, addedAge)) / (held + added));
	}

	/** Whole Formations a neglected tank gives for {@code clottedMl}. */
	public static int neglectFormations(double clottedMl) {
		return clottedMl <= CLOT_EPSILON ? 0 : (int) Math.floor(clottedMl / NEGLECT_ML_PER_FORMATION + 1.0E-9D);
	}

	// ── The Condenser: deliberate clotting at the best yield ─────────────────────

	/** Blood one Condenser Formation takes at each installed stage (0 = Sanguine Cauldron). */
	public static double condenserMlPerFormation(int stage) {
		return switch (clampStage(stage)) {
			case 0 -> 150.0D;
			case 1 -> 125.0D;
			default -> 100.0D;
		};
	}

	/** Only a bare cauldron can lose a formation; Sanguine Crystallization steadies it by 5% a level. */
	public static double collapseChance(int stage, int crystallizationLevel) {
		return clampStage(stage) == 0 ? Math.max(0.0D, 0.25D - 0.05D * crystallizationLevel) : 0.0D;
	}

	/** The finished Condenser sometimes sets a second Formation from the same blood. */
	public static double bonusChance(int stage) {
		return clampStage(stage) == 2 ? 0.25D : 0.0D;
	}

	private static int clampStage(int stage) {
		return Math.clamp(stage, 0, 2);
	}
}
