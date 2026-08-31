package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ManipulationRetirementRules {
	private static final Set<String> RETIRED_MANIPULATION_IDS = Set.of(
			"blood_lamp",
			"crimson_harvest",
			"hemosynthesis",
			"vital_reservoir",
			"sanguine_excavation",
			"ferric_resonance",
			"glacial_bastion",
			"blood_eclipse_mantle",
			"crimson_sight",
			"glacial_circulation",
			"ferric_transmutation",
			"vigil_of_glass",
			"hematic_ballast",
			"summon_thrall");

	private static final Set<String> RETIRED_MEMORY_ITEM_IDS = Set.of(
			"memory_conjure_living_staff",
			"memory_blood_absorption",
			"memory_blood_projection",
			"memory_blood_lamp",
			"crude_memory_blood_lamp",
			"memory_crimson_harvest",
			"crude_memory_crimson_harvest",
			"memory_hemosynthesis",
			"memory_vital_reservoir",
			"memory_sanguine_excavation",
			"memory_ferric_resonance",
			"memory_glacial_bastion",
			"memory_blood_eclipse_mantle",
			"memory_crimson_sight",
			"memory_glacial_circulation",
			"memory_ferric_transmutation",
			"memory_vigil_of_glass",
			"memory_hematic_ballast",
			"memory_summon_thrall");

	private ManipulationRetirementRules() {
	}

	public static boolean isRetiredManipulation(BloodManipulation manipulation) {
		return manipulation != null && isRetiredManipulation(manipulation.getName());
	}

	public static boolean isRetiredManipulation(String manipulationId) {
		return manipulationId != null && RETIRED_MANIPULATION_IDS.contains(manipulationId.trim());
	}

	public static boolean isRetiredMemoryItem(Item item) {
		if (item == null) {
			return false;
		}
		return isRetiredMemoryItemId(BuiltInRegistries.ITEM.getKey(item).getPath());
	}

	public static boolean isRetiredMemoryItem(Item item, BloodManipulation manipulation) {
		return isRetiredMemoryItem(item) || isRetiredManipulation(manipulation);
	}

	public static boolean isRetiredMemoryItemId(String itemId) {
		return itemId != null && RETIRED_MEMORY_ITEM_IDS.contains(itemId.trim());
	}

	public static boolean sanitizeKnownManipulations(IKnownManipulations known) {
		if (known == null) {
			return false;
		}
		boolean changed = removeRetiredKnownManipulations(known.getKnownManips());
		changed |= sanitizeEquippedManipulations(known);
		changed |= sanitizeLoadouts(known);
		changed |= sanitizeSelectedManipulation(known);
		return changed;
	}

	private static boolean removeRetiredKnownManipulations(LinkedHashMap<BloodManipulation, ManipLevel> knownManips) {
		if (knownManips == null || knownManips.isEmpty()) {
			return false;
		}
		int before = knownManips.size();
		knownManips.entrySet().removeIf(entry -> isRetiredManipulation(entry.getKey()));
		return before != knownManips.size();
	}

	private static boolean sanitizeEquippedManipulations(IKnownManipulations known) {
		List<String> before = new ArrayList<>(known.getEquippedManipNames());
		known.setEquippedManipNames(before);
		return !before.equals(known.getEquippedManipNames());
	}

	private static boolean sanitizeLoadouts(IKnownManipulations known) {
		List<ManipulationLoadout> loadouts = known.getLoadouts();
		if (loadouts == null || loadouts.isEmpty()) {
			return false;
		}
		List<ManipulationLoadout> sanitized = new ArrayList<>();
		for (int i = 0; i < loadouts.size(); i++) {
			ManipulationLoadout loadout = loadouts.get(i);
			if (loadout == null) {
				sanitized.add(ManipulationLoadout.empty(i));
			} else {
				sanitized.add(ManipulationLoadout.of(loadout.name(), loadout.selectedManipName(),
						loadout.manipNames(), i));
			}
		}
		if (loadouts.equals(sanitized)) {
			return false;
		}
		known.setLoadouts(sanitized);
		return true;
	}

	private static boolean sanitizeSelectedManipulation(IKnownManipulations known) {
		BloodManipulation selected = safeSelectedManipulation(known);
		if (selected == null || selected == BloodManipulation.BLANK) {
			return false;
		}
		if (!isRetiredManipulation(selected) && containsManipName(known.getKnownManips(), selected.getName())) {
			return false;
		}
		BloodManipulation fallback = firstActiveManipulation(known);
		known.setSelectedManip(fallback != null ? fallback : BloodManipulation.BLANK);
		return true;
	}

	private static BloodManipulation safeSelectedManipulation(IKnownManipulations known) {
		try {
			return known.getSelectedManip();
		} catch (RuntimeException ignored) {
			return BloodManipulation.BLANK;
		}
	}

	private static BloodManipulation firstActiveManipulation(IKnownManipulations known) {
		LinkedHashMap<BloodManipulation, ManipLevel> knownManips = known.getKnownManips();
		if (knownManips == null || knownManips.isEmpty()) {
			return null;
		}
		Set<String> equipped = new LinkedHashSet<>(known.getEquippedManipNames());
		for (String name : equipped) {
			BloodManipulation manipulation = findKnownByName(knownManips, name);
			if (manipulation != null && !isRetiredManipulation(manipulation)) {
				return manipulation;
			}
		}
		for (BloodManipulation manipulation : knownManips.keySet()) {
			if (manipulation != null && !isRetiredManipulation(manipulation)) {
				return manipulation;
			}
		}
		return null;
	}

	private static BloodManipulation findKnownByName(LinkedHashMap<BloodManipulation, ManipLevel> knownManips,
			String name) {
		if (name == null || name.isBlank()) {
			return null;
		}
		for (BloodManipulation manipulation : knownManips.keySet()) {
			if (manipulation != null && name.equals(manipulation.getName())) {
				return manipulation;
			}
		}
		return null;
	}

	private static boolean containsManipName(Map<BloodManipulation, ManipLevel> knownManips, String name) {
		if (knownManips == null || name == null || name.isBlank()) {
			return false;
		}
		for (BloodManipulation manipulation : knownManips.keySet()) {
			if (manipulation != null && name.equals(manipulation.getName())) {
				return true;
			}
		}
		return false;
	}
}
