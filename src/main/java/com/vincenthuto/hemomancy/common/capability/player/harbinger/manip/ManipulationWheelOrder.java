package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ManipulationWheelOrder {
	private ManipulationWheelOrder() {
	}

	public record Entry(BloodManipulation manipulation, int knownIndex) {
	}

	public static List<Entry> resolve(List<BloodManipulation> knownManipulations, List<String> equippedNames) {
		if (knownManipulations == null || equippedNames == null) return List.of();

		Map<String, Entry> knownByName = new HashMap<>();
		for (int i = 0; i < knownManipulations.size(); i++) {
			BloodManipulation manipulation = knownManipulations.get(i);
			if (manipulation != null && manipulation.getName() != null) {
				knownByName.putIfAbsent(manipulation.getName(), new Entry(manipulation, i));
			}
		}

		List<Entry> ordered = new ArrayList<>();
		for (String name : equippedNames) {
			if (ManipulationEquipHelper.isFixedMechanicalManip(name)
					|| ManipulationRetirementRules.isRetiredManipulation(name)) continue;
			Entry entry = knownByName.get(name);
			if (entry != null) ordered.add(entry);
		}
		return ordered;
	}
}
