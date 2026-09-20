package com.vincenthuto.hemomancy.common.item.harbinger;

public final class BloodSamplingRules {
	private BloodSamplingRules() {
	}

	public static BloodSamplingResult evaluate(boolean alreadyFilled, boolean livingTarget, boolean alive,
			boolean invulnerable, boolean hasEntityTypeId) {
		return evaluate(alreadyFilled, livingTarget, alive, invulnerable, hasEntityTypeId, false, false);
	}

	public static BloodSamplingResult evaluate(boolean alreadyFilled, boolean livingTarget, boolean alive,
			boolean invulnerable, boolean hasEntityTypeId, boolean requiresLivingSyringe, boolean usingLivingSyringe) {
		if (alreadyFilled) return BloodSamplingResult.ALREADY_FILLED;
		if (!livingTarget) return BloodSamplingResult.INVALID_TARGET;
		if (!alive || invulnerable) return BloodSamplingResult.INSUFFICIENT_CONDITION;
		if (!hasEntityTypeId) return BloodSamplingResult.FAILED;
		if (requiresLivingSyringe && !usingLivingSyringe) return BloodSamplingResult.REQUIRES_LIVING_SYRINGE;
		return BloodSamplingResult.SUCCESS;
	}
}
