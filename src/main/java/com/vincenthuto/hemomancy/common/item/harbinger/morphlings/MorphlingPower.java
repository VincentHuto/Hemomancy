package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

/**
 * One rung of a strain's power ladder, as numbers {@code MorphlingBalanceBandsTest} checks against the spec's balance
 * rules. {@code cooldownTicks} is the away-from-home recovery, {@code homeCooldownTicks} the at-home one (equal when
 * home does not change it). A {@link Kind#CONDITIONAL} power's cooldown is only a re-trigger limit and is not banded.
 */
public record MorphlingPower(String id, int stage, Kind kind, double costMl, int cooldownTicks, int homeCooldownTicks) {
	public enum Kind {
		/** Scales from Unfed; no cooldown. */
		PASSIVE,
		/** Always on while its condition holds, or limited only per target or per proc (balance rule 2). */
		CONDITIONAL,
		/** A Developing, Mature or Apex power with a cooldown in its stage's band. */
		REACTIVE,
		/** A paid Primal active: 250-450 mL, 12-90 s. */
		PRIMAL,
		/** A Primal that triggers on a substantial kill instead of being paid for (Whale Fall). */
		TRIGGERED_PRIMAL,
		/** A Last Rite death-save: 500-650 mL on the shared 10-minute cooldown. */
		DEATH_SAVE
	}

	public static MorphlingPower passive(String id) {
		return new MorphlingPower(id, 0, Kind.PASSIVE, 0.0D, 0, 0);
	}

	public static MorphlingPower conditional(String id, int stage, int limitTicks) {
		return new MorphlingPower(id, stage, Kind.CONDITIONAL, 0.0D, limitTicks, limitTicks);
	}

	public static MorphlingPower reactive(String id, int stage, int cooldownTicks, int homeCooldownTicks) {
		return new MorphlingPower(id, stage, Kind.REACTIVE, 0.0D, cooldownTicks, homeCooldownTicks);
	}

	public static MorphlingPower reactive(String id, int stage, int cooldownTicks) {
		return reactive(id, stage, cooldownTicks, cooldownTicks);
	}

	public static MorphlingPower primal(String id, double costMl, int cooldownTicks) {
		return new MorphlingPower(id, PrimalMorphlingRules.PRIMAL_LEVEL, Kind.PRIMAL, costMl, cooldownTicks, cooldownTicks);
	}

	public static MorphlingPower triggeredPrimal(String id, int cooldownTicks) {
		return new MorphlingPower(id, PrimalMorphlingRules.PRIMAL_LEVEL, Kind.TRIGGERED_PRIMAL, 0.0D, cooldownTicks,
				cooldownTicks);
	}

	public static MorphlingPower deathSave(String id, int stage, double costMl) {
		return new MorphlingPower(id, stage, Kind.DEATH_SAVE, costMl, MorphlingBalanceBands.LAST_RITE_COOLDOWN_TICKS,
				MorphlingBalanceBands.LAST_RITE_COOLDOWN_TICKS);
	}
}
