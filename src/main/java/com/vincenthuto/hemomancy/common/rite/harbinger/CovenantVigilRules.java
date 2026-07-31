package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Reward tuning for the Grand helper rite. Kept separate from the event
 * handler so the promise made by the authored rite remains explicit.
 */
final class CovenantVigilRules {
	static final int REWARD_DURATION_TICKS = 12_000;
	static final int RESISTANCE_AMPLIFIER = 0;
	static final int REGENERATION_AMPLIFIER = 0;

	private CovenantVigilRules() {
	}
}
