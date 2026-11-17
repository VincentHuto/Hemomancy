package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Crimson Tithe's numbers (D-1, canon memory rework spec 3.1): Hemorath's Chained Debt and Marrow Siphon made the
 * player's. A held chain drink into a vessel of 0 to 1,000 that leaks by Hemorath's own curve at a player's scale and
 * empties in a star of blood and iron lanes on release. No Minecraft types, so the JVM tests read it whole.
 */
public final class CrimsonTitheRules {
	/** Paid once when the hold begins, over the per-second upkeep the channel pays. */
	public static final double OPEN_COST = 150.0D;
	/** The channel's upkeep in mL a second: 10 per 5-tick draw pulse. */
	public static final double UPKEEP_PER_SECOND = 40.0D;
	public static final int PULSE_TICKS = 5;
	public static final int LONGEST_HOLD = 160;
	public static final int RELATCH_TICKS = 20;
	public static final int COOLDOWN_TICKS = 400;
	public static final double REACH = 10.0D;
	/** A chain breaks when its body leaves this far, or stays out of sight for {@link #SIGHT_GRACE} ticks. */
	public static final double BREAK_REACH = 12.0D;
	public static final int SIGHT_GRACE = 10;
	/** A body Obscured from the caster cannot be latched past this far. */
	public static final double OBSCURED_REACH = 4.0D;
	public static final int MAX_CHAINS = 5;
	public static final double CAPACITY = 1000.0D;

	public static final float LATCH_DAMAGE = 3.0F;
	public static final float DRAW_DAMAGE = 1.5F;
	public static final double BLOOD_DRAW = 25.0D, BLOODLESS_DRAW = 10.0D;
	/** What a blood-active player pays in blood each pulse instead of the draw's health (NQ-08-12). */
	public static final double PLAYER_BLOOD_TAKEN = 25.0D;
	/** A Searing body's blood is cauterised: it gives this share. */
	public static final double SEARING_SHARE = 0.6D;
	/** A boss gives this share and its chain never pulls. */
	public static final double BOSS_SHARE = 0.5D;
	public static final double HEAL_PER_DRAWN = 1.0D / 50.0D;
	public static final double MAX_HEAL_PER_HOLD = 10.0D;
	/** Chained to Hemorath himself, each pulse feeds this much into his meter and nothing into the vessel. */
	public static final float HEMORATH_FEED = 40.0F;
	/** A chained body is hauled toward the caster this far a tick; a Lodestone body 1.5 times as far. */
	public static final double PULL = 0.033D, LODESTONE_PULL = 0.05D;

	public static final double HOLLOW = 0.35D, BRIMMING = 0.70D;
	/** A Brimming caster moves this much slower and takes this much more damage. */
	public static final double BRIMMING_SLOW = 0.20D, BRIMMING_HURT = 0.20D;
	public static final double LEAK_PER_IRON = 150.0D;
	public static final int MAX_IRON = 4;
	/** The vessel counts as full above this fill: one pulse's leak below the brim. */
	public static final double FULL = 0.95D;
	/** A full vessel held this long bursts on the caster. */
	public static final int OVERFILL_TICKS = 20;
	public static final float OVERFILL_DAMAGE = 4.0F;
	public static final double OVERFILL_BLOOD_SHARE = 0.05D;

	public static final int BASE_LANES = 6, MAX_LANES = 10;
	public static final double LANE_HALF_WIDTH = 0.6D;
	public static final double LANE_KNOCKBACK = 0.6D;
	public static final double PRESSURE_BONUS_PER_LEVEL = 2.0D;
	/** The release's timeline: lanes emerge, spikes erupt near to far, blood floods and lingers, the trenches close. */
	public static final int EMERGE_END = 3, SPIKES_END = 10, FLOOD_END = 30, CLOSE_END = 50;
	public static final int FLOODED_SLOWNESS = 60;

	private CrimsonTitheRules() {
	}

	/** How full the vessel is, 0 to 1. */
	public static double fill(double vessel) {
		return HemoEasing.clamp01(vessel / CAPACITY);
	}

	/** Blood the vessel loses each tick at {@code fill}: 0.6 nearly empty, 6.6 full. */
	public static double leak(double fill) {
		double f = HemoEasing.clamp01(fill);
		return CAPACITY * (0.0006D + 0.006D * f * f);
	}

	public static int iron(double leaked) {
		return (int) Math.min(MAX_IRON, Math.floor(Math.max(0.0D, leaked) / LEAK_PER_IRON));
	}

	public static boolean brimming(double fill) {
		return fill > BRIMMING;
	}

	public static boolean full(double fill) {
		return fill >= FULL;
	}

	public static boolean hollow(double fill) {
		return fill < HOLLOW;
	}

	/** What one pulse pours into the vessel from a body. */
	public static double drawn(boolean bloodless, boolean searing, boolean boss) {
		double base = bloodless ? BLOODLESS_DRAW : BLOOD_DRAW;
		if (searing) base *= SEARING_SHARE;
		if (boss) base *= BOSS_SHARE;
		return base;
	}

	/** The vessel after {@code amount} pours in, never over capacity. */
	public static double pour(double vessel, double amount) {
		return Math.min(CAPACITY, Math.max(0.0D, vessel) + Math.max(0.0D, amount));
	}

	/** The heal this pulse gives, given what it drew and what the hold has healed so far. */
	public static double heal(double drawnThisPulse, double healedSoFar) {
		return Math.max(0.0D, Math.min(drawnThisPulse * HEAL_PER_DRAWN, MAX_HEAL_PER_HOLD - healedSoFar));
	}

	public static int lanes(int iron) {
		return Math.min(MAX_LANES, BASE_LANES + Math.max(0, iron));
	}

	public static double laneLength(double fill) {
		return 3.0D + 7.0D * HemoEasing.clamp01(fill);
	}

	public static float releaseDamage(double fill) {
		return (float) (4.0D + 12.0D * HemoEasing.clamp01(fill));
	}

	public static double pressureBonus(int levels) {
		return PRESSURE_BONUS_PER_LEVEL * Math.max(0, levels);
	}

	/** The angle of lane {@code index} of {@code lanes}, from the release's first lane at {@code start} radians. */
	public static double laneAngle(double start, int index, int lanes) {
		return start + index * Math.PI * 2.0D / Math.max(1, lanes);
	}

	/**
	 * Whether a point {@code dx, dz} from the caster lies in one of the lanes (the wedges between them are safe):
	 * within the lane's length along its axis and within its half-width across it.
	 */
	public static boolean inLane(double dx, double dz, double start, int lanes, double length) {
		for (int i = 0; i < lanes; i++) {
			double angle = laneAngle(start, i, lanes);
			double along = dx * Math.cos(angle) + dz * Math.sin(angle);
			double across = -dx * Math.sin(angle) + dz * Math.cos(angle);
			if (along >= 0.0D && along <= length && Math.abs(across) <= LANE_HALF_WIDTH) return true;
		}
		return false;
	}

	/** A spike's rise along a lane, 0 to 1: they erupt near to far between {@link #EMERGE_END} and {@link #SPIKES_END}. */
	public static double spikeRise(double age, double alongFraction) {
		double start = EMERGE_END + (SPIKES_END - EMERGE_END) * HemoEasing.clamp01(alongFraction) * 0.7D;
		return HemoEasing.clamp01((age - start) / 3.0D) * (1.0D - HemoEasing.clamp01((age - FLOOD_END) / (CLOSE_END - FLOOD_END)));
	}

	/** How far the trenches stand open, 0 to 1: they open by the end of the emergence and close over the last 20 ticks. */
	public static double trenchOpen(double age) {
		return HemoEasing.clamp01(age / EMERGE_END) * (1.0D - HemoEasing.clamp01((age - FLOOD_END) / (CLOSE_END - FLOOD_END)));
	}

	/** How much blood stands in the trenches, 0 to 1. */
	public static double flood(double age) {
		return HemoEasing.clamp01((age - SPIKES_END) / 6.0D) * (1.0D - HemoEasing.clamp01((age - FLOOD_END) / (CLOSE_END - FLOOD_END)));
	}

	public static boolean flooding(double age) {
		return age >= SPIKES_END && age < FLOOD_END;
	}
}
