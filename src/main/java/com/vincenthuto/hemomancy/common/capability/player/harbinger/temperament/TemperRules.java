package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;

/**
 * The numbers of Temper, the small spendable pool per school that sits beside Temperament (core loop Track 4). Pure:
 * no level, no player. Temperament is what a Harbinger has done and is never spent; Temper is what they carry now.
 *
 * <ul>
 * <li>Capacity comes from alignment: {@code 20 + alignment x 0.6}, held between 20 and 80.</li>
 * <li>Absorbing from a living creature gives {@link #TEMPER_PER_ML} per mL drawn, split evenly over its schools.</li>
 * <li>A cast or pulse pays in its primary school: from the pool, else one catalyst dissolves into the pool and the
 * cost is paid from that, else the effect runs dry. Nothing is ever refused for want of Temper.</li>
 * <li>An active scar of the school cuts the burn by a quarter per tier and slowly refills the pool to a floor of
 * {@code 5 x tier}.</li>
 * <li>Death keeps half (CL-11).</li>
 * </ul>
 */
public final class TemperRules {
	public static final float BASE_CAP = 20.0F;
	public static final float CAP_PER_ALIGNMENT = 0.6F;
	public static final float MAX_CAP = 80.0F;
	/**
	 * LP-14, NQ-04-8: what a held form burns a second, by its base price in mL a second. Up to the first edge burns the
	 * first rate, up to the second the second, and so on; anything dearer burns the last.
	 */
	static final double[] BURN_BAND_EDGES = { 10.0D, 25.0D, 45.0D };
	static final float[] BURN_PER_SECOND = { 0.5F, 1.0F, 1.5F, 2.0F };
	/** LP-14, NQ-04-8: an R cast's Temper cost by rank, Humilis to Perfectus. */
	static final float[] CAST_COST_BY_RANK = { 2.0F, 3.5F, 5.0F, 6.5F, 8.0F };
	/** Temper gained per mL drawn from a living creature, shared over the creature's schools. */
	public static final float TEMPER_PER_ML = 1.0F;
	/** What the floor of an active scar refills per second, per tier, while the pool sits below it. */
	public static final float FLOOR_REFILL_PER_SECOND_PER_TIER = 0.1F;
	/** CL-11: what death keeps. */
	public static final float KEPT_ON_DEATH = 0.5F;
	/** What a Will taken whole by Absorption leaves in its school, per tier (core-loop Track 11). */
	public static final float WILL_PER_TIER = 10.0F;

	private TemperRules() {
	}

	/** What the effect is: a Sculpting pulse or a combat cast. They run dry differently (CL-10). */
	public enum Kind {
		SCULPTING(1.5D, 0.75F),
		CAST(1.25D, 0.9F);

		private final double dryCostMultiplier;
		private final float dryStrength;

		Kind(double dryCostMultiplier, float dryStrength) {
			this.dryCostMultiplier = dryCostMultiplier;
			this.dryStrength = dryStrength;
		}

		/** The blood multiplier a dry effect pays. */
		public double dryCostMultiplier() {
			return dryCostMultiplier;
		}

		/** The strength a dry effect works at. */
		public float dryStrength() {
			return dryStrength;
		}
	}

	/** How a charge was met. */
	public enum Outcome {
		/** Paid from the pool: full strength, normal cost. */
		TEMPERED,
		/** The pool was short; a catalyst dissolved into it and the cost was paid from that. */
		CATALYST,
		/** Nothing to pay with: the effect costs more blood and works softer. */
		DRY
	}

	/**
	 * One charge worked out against a pool. {@code burn} is what leaves the pool, {@code catalystWorth} what a dissolved
	 * catalyst adds first (0 unless {@link Outcome#CATALYST}).
	 */
	public record Plan(Outcome outcome, float burn, float catalystWorth) {
		public static final Plan FREE = new Plan(Outcome.TEMPERED, 0.0F, 0.0F);

		public boolean dry() {
			return outcome == Outcome.DRY;
		}
	}

	/** How much of {@code school} this alignment lets a Harbinger carry. */
	public static float cap(float alignment) {
		if (!Float.isFinite(alignment)) return BASE_CAP;
		return Math.clamp(BASE_CAP + Math.max(0.0F, alignment) * CAP_PER_ALIGNMENT, BASE_CAP, MAX_CAP);
	}

	/** A combat cast's Temper cost by rank: Humilis 2, Mediocritas 3.5, Summa 5, Magister 6.5, Perfectus 8. */
	public static float castCost(EnumManipulationRank rank) {
		if (rank == null) return CAST_COST_BY_RANK[0];
		return CAST_COST_BY_RANK[Math.clamp(rank.ordinal(), 0, CAST_COST_BY_RANK.length - 1)];
	}

	/** What a held form whose base price is {@code mlPerSecond} burns a second: 0.5, 1, 1.5 or 2 by price band. */
	public static float burnPerSecond(double mlPerSecond) {
		for (int band = 0; band < BURN_BAND_EDGES.length; band++) {
			if (!(mlPerSecond > BURN_BAND_EDGES[band])) return BURN_PER_SECOND[band];
		}
		return BURN_PER_SECOND[BURN_PER_SECOND.length - 1];
	}

	/**
	 * One pulse's Temper cost: the form's {@link #burnPerSecond} spread over its cadence, so a per-tick haul burns a
	 * twentieth of the second's burn each tick.
	 */
	public static float pulseCost(int cadence, double mlPerSecond) {
		return burnPerSecond(mlPerSecond) * Math.max(1, cadence) / 20.0F;
	}

	/** What a scar of {@code tier} (0 for none) takes off the burn: a quarter per tier. */
	public static float burnReduction(int scarTier) {
		return Math.clamp(scarTier, 0, 3) * 0.25F;
	}

	/** {@code cost} after an active scar of {@code tier} in the same school. */
	public static float scarred(float cost, int scarTier) {
		return Math.max(0.0F, cost) * (1.0F - burnReduction(scarTier));
	}

	/** The floor an active scar of {@code tier} keeps the pool at: {@code 5 x tier}. */
	public static float scarFloor(int scarTier) {
		return 5.0F * Math.clamp(scarTier, 0, 3);
	}

	/** The pool after {@code seconds} of an active scar of {@code tier} refilling it toward its floor. */
	public static float refillTowardFloor(float pool, int scarTier, float seconds) {
		float floor = scarFloor(scarTier);
		if (pool >= floor || seconds <= 0.0F) return pool;
		return Math.min(floor, pool + FLOOR_REFILL_PER_SECOND_PER_TIER * scarTier * seconds);
	}

	/** Temper per school from {@code ml} drawn from a creature with {@code schools} schools. */
	public static float fromCreature(double ml, int schools) {
		if (schools <= 0 || !(ml > 0.0D)) return 0.0F;
		return (float) (ml * TEMPER_PER_ML / schools);
	}

	/** Temper a Will of {@code tier} leaves in its school when it is absorbed whole: an enzyme's worth per tier. */
	public static float fromWill(int tier) {
		return tier <= 0 ? 0.0F : WILL_PER_TIER * tier;
	}

	/** What a pool keeps through death (CL-11). */
	public static float afterDeath(float pool) {
		return Math.max(0.0F, pool) * KEPT_ON_DEATH;
	}

	/**
	 * Works out a charge of {@code cost} against {@code pool}: the pool first, then one catalyst worth
	 * {@code catalystWorth} (0 when none is carried) dissolved into the pool up to its cap, then dry. A dry charge
	 * leaves the pool as it is.
	 */
	public static Plan plan(float pool, float cost, float catalystWorth, float cap) {
		if (!(cost > 0.0F)) return Plan.FREE;
		float held = Math.max(0.0F, pool);
		if (held >= cost) return new Plan(Outcome.TEMPERED, cost, 0.0F);
		if (catalystWorth > 0.0F && Math.min(Math.max(cap, held), held + catalystWorth) >= cost) {
			return new Plan(Outcome.CATALYST, cost, catalystWorth);
		}
		return new Plan(Outcome.DRY, 0.0F, 0.0F);
	}

	/** The pool once {@code plan} is settled: a catalyst first fills it (no further than the cap), then the burn. */
	public static float settle(float pool, Plan plan, float cap) {
		float held = Math.max(0.0F, pool);
		return switch (plan.outcome()) {
			case TEMPERED -> Math.max(0.0F, held - plan.burn());
			case CATALYST -> Math.max(0.0F, Math.min(Math.max(cap, held), held + plan.catalystWorth()) - plan.burn());
			case DRY -> held;
		};
	}

	/** The blood multiplier for an effect of {@code kind} met as {@code plan}. */
	public static double costMultiplier(Kind kind, Plan plan) {
		return plan.dry() ? kind.dryCostMultiplier() : 1.0D;
	}

	/** The strength an effect of {@code kind} met as {@code plan} works at. */
	public static float strength(Kind kind, Plan plan) {
		return plan.dry() ? kind.dryStrength() : 1.0F;
	}
}
