package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.*;

public final class KnownManipulationUpkeep {


	private KnownManipulationUpkeep() {
	}






	public static boolean sanitizeKnownManipulations(IKnownManipulations known) {
		if (known == null) {
			return false;
		}
		boolean changed = adoptRegisteredMetadata(known);
		changed |= sanitizeEquippedManipulations(known);
		changed |= sanitizeLoadouts(known);
		changed |= sanitizeSelectedManipulation(known);
		if (changed) known.markSyncDirty();
		return changed;
	}

	private static boolean adoptRegisteredMetadata(IKnownManipulations known) {
		boolean changed = false;
		if (known.getKnownManips() != null) {
			for (BloodManipulation manipulation : known.getKnownManips().keySet()) changed |= adoptRegistered(manipulation);
		}
		return changed | adoptRegistered(known.getSelectedManip());
	}

	private static boolean adoptRegistered(BloodManipulation manipulation) {
		if (manipulation == null || manipulation.getName() == null) return false;
		var id = net.minecraft.resources.ResourceLocation.tryBuild(com.vincenthuto.hemomancy.Hemomancy.MOD_ID, manipulation.getName());
		return id != null && manipulation.adoptRegisteredMetadata(
				com.vincenthuto.hemomancy.common.init.ManipulationInit.MANIPS_TYPE_REGISTRY.get(id));
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
		if (containsManipName(known.getKnownManips(), selected.getName())) {
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
			if (manipulation != null) {
				return manipulation;
			}
		}
		for (BloodManipulation manipulation : knownManips.keySet()) {
			if (manipulation != null) {
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
