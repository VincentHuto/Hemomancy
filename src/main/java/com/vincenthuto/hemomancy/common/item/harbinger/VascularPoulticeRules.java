package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class VascularPoulticeRules {
	public static final float MAX_SECTION_HEALTH = 100F;
	public static final float DEFAULT_HEAL_AMOUNT = 12F;
	public static final int COOLDOWN_TICKS = 40;

	private VascularPoulticeRules() {
	}

	public static Optional<EnumVeinSections> mostDamagedSection(Map<EnumVeinSections, Float> vascularSystem) {
		if (vascularSystem == null || vascularSystem.isEmpty()) {
			return Optional.empty();
		}
		EnumVeinSections worst = null;
		float worstHealth = MAX_SECTION_HEALTH;
		for (EnumVeinSections section : EnumVeinSections.values()) {
			float health = vascularSystem.getOrDefault(section, MAX_SECTION_HEALTH);
			if (health < worstHealth) {
				worst = section;
				worstHealth = health;
			}
		}
		return Optional.ofNullable(worst);
	}

	public static Optional<HealResult> healMostDamaged(Map<EnumVeinSections, Float> vascularSystem, float healAmount) {
		if (healAmount <= 0F) {
			return Optional.empty();
		}
		Optional<EnumVeinSections> section = mostDamagedSection(vascularSystem);
		if (section.isEmpty()) {
			return Optional.empty();
		}
		EnumVeinSections healedSection = section.get();
		float before = vascularSystem.getOrDefault(healedSection, MAX_SECTION_HEALTH);
		float after = Math.min(MAX_SECTION_HEALTH, before + healAmount);
		if (after <= before) {
			return Optional.empty();
		}
		vascularSystem.put(healedSection, after);
		return Optional.of(new HealResult(healedSection, after - before, before, after));
	}

	public static List<HealResult> healTwoMostDamaged(Map<EnumVeinSections, Float> vascularSystem, float healAmount) {
		List<HealResult> results = new ArrayList<>(2);
		for (int i = 0; i < 2; i++) {
			Optional<EnumVeinSections> next = mostDamagedExcluding(vascularSystem, results);
			if (next.isEmpty()) break;
			EnumVeinSections section = next.get();
			float before = vascularSystem.getOrDefault(section, MAX_SECTION_HEALTH);
			float after = Math.min(MAX_SECTION_HEALTH, before + healAmount);
			vascularSystem.put(section, after);
			results.add(new HealResult(section, after - before, before, after));
		}
		return List.copyOf(results);
	}

	private static Optional<EnumVeinSections> mostDamagedExcluding(Map<EnumVeinSections, Float> vascularSystem,
			List<HealResult> previous) {
		if (vascularSystem == null || vascularSystem.isEmpty()) return Optional.empty();
		EnumVeinSections worst = null;
		float worstHealth = MAX_SECTION_HEALTH;
		for (EnumVeinSections section : EnumVeinSections.values()) {
			boolean alreadyHealed = previous.stream().anyMatch(result -> result.section() == section);
			float health = vascularSystem.getOrDefault(section, MAX_SECTION_HEALTH);
			if (!alreadyHealed && health < worstHealth) {
				worst = section;
				worstHealth = health;
			}
		}
		return Optional.ofNullable(worst);
	}

	public record HealResult(EnumVeinSections section, float amountHealed, float before, float after) {
	}
}
