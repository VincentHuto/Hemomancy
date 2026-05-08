package com.vincenthuto.hemomancy.common.summon;

public record PuppeteerSummonDefinition(
		String name,
		String role,
		int requiredDegree,
		double baseHealth,
		double baseDamage,
		double movementSpeed,
		int threadSummonCost,
		int threadUpkeepPerMinute,
		String loreKey
) {
	public String translationKey() {
		return "entity.hemomancy." + name;
	}

	public String roleTranslationKey() {
		return "hemomancy.summon." + name + ".role";
	}
}
