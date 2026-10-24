package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

public record PuppeteerSummonDefinition(
		String name,
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
