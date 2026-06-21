package com.vincenthuto.hemomancy.common.summon;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class PuppeteerSummonDefinitions {
	public static final String VEINWING_VULTURE = "veinwing_vulture";
	public static final String MARROW_SPITTER = "marrow_spitter";
	public static final String GOREBOUND_HULK = "gorebound_hulk";
	public static final String MNEMONIST_PUPPET = "mnemonist_puppet";

	private static final List<PuppeteerSummonDefinition> DEFINITIONS = List.of(
			new PuppeteerSummonDefinition(VEINWING_VULTURE, "Flying Striker", 3,
					14.0, 4.0, 0.36, 28, 18,
					"hemomancy.summon.veinwing_vulture.lore"),
			new PuppeteerSummonDefinition(MARROW_SPITTER, "Ranged Support", 3,
					22.0, 5.0, 0.24, 38, 12,
					"hemomancy.summon.marrow_spitter.lore"),
			new PuppeteerSummonDefinition(GOREBOUND_HULK, "Heavy Bruiser", 4,
					55.0, 9.0, 0.18, 56, 8,
					"hemomancy.summon.gorebound_hulk.lore"),
			new PuppeteerSummonDefinition(MNEMONIST_PUPPET, "Memory Echo", 5,
					26.0, 3.0, 0.26, 64, 16,
					"hemomancy.summon.mnemonist_puppet.lore")
	).stream().sorted(Comparator.comparingInt(PuppeteerSummonDefinition::requiredDegree)
			.thenComparing(PuppeteerSummonDefinition::name)).toList();

	private PuppeteerSummonDefinitions() {
	}

	public static List<PuppeteerSummonDefinition> all() {
		return DEFINITIONS;
	}

	public static Optional<PuppeteerSummonDefinition> byName(String name) {
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		for (PuppeteerSummonDefinition definition : DEFINITIONS) {
			if (definition.name().equals(name)) {
				return Optional.of(definition);
			}
		}
		return Optional.empty();
	}

	public static List<PuppeteerSummonDefinition> unlockedByDegree(int degree) {
		return DEFINITIONS.stream()
				.filter(definition -> definition.requiredDegree() <= degree)
				.toList();
	}
}
