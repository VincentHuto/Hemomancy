package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ManipulationEquipHelper {
	public static final String BLOOD_ABSORPTION = "blood_absorption";
	public static final String BLOOD_PROJECTION = "blood_projection";
	public static final String CONJURE_SICKLE = "conjure_sickle";

	private ManipulationEquipHelper() {
	}

	public static boolean equipNameIfPossible(List<String> equippedNames, String manipName, int maxSlots) {
		if (equippedNames == null || manipName == null || manipName.isEmpty()) {
			return false;
		}
		boolean changed = normalizeEquippedNames(equippedNames);
		if (ManipulationRetirementRules.isRetiredManipulation(manipName)) {
			return changed;
		}
		if (CONJURE_SICKLE.equals(manipName)) {
			if (!equippedNames.contains(manipName)) {
				equippedNames.add(manipName);
				return true;
			}
			return changed;
		}
		if (isFixedMechanicalManip(manipName)) {
			return changed;
		}
		if (equippedNames.contains(manipName)) {
			return changed;
		}
		if (countNormalEquippedNames(equippedNames) >= maxSlots) {
			return changed;
		}
		equippedNames.add(manipName);
		return true;
	}

	public static boolean unequipNameIfAllowed(List<String> equippedNames, String manipName) {
		if (equippedNames == null || manipName == null || manipName.isEmpty()) {
			return false;
		}
		boolean changed = normalizeEquippedNames(equippedNames);
		if (isFixedMechanicalManip(manipName)) {
			return changed;
		}
		return equippedNames.remove(manipName) || changed;
	}

	public static boolean normalizeEquippedNames(List<String> equippedNames) {
		if (equippedNames == null) {
			return false;
		}
		List<String> original = new ArrayList<>(equippedNames);
		boolean hadSickle = equippedNames.contains(CONJURE_SICKLE);
		LinkedHashSet<String> normalNames = new LinkedHashSet<>();
		for (String name : equippedNames) {
			if (name != null && !name.isEmpty() && !isFixedMechanicalManip(name)
					&& !ManipulationRetirementRules.isRetiredManipulation(name)) {
				normalNames.add(name);
			}
		}
		equippedNames.clear();
		equippedNames.add(BLOOD_ABSORPTION);
		equippedNames.add(BLOOD_PROJECTION);
		if (hadSickle) equippedNames.add(CONJURE_SICKLE);
		equippedNames.addAll(normalNames);
		return !original.equals(equippedNames);
	}

	public static int countNormalEquippedNames(List<String> equippedNames) {
		if (equippedNames == null) {
			return 0;
		}
		int count = 0;
		for (String name : equippedNames) {
			if (name != null && !name.isEmpty() && !isFixedMechanicalManip(name)
					&& !ManipulationRetirementRules.isRetiredManipulation(name)) {
				count++;
			}
		}
		return count;
	}

	public static boolean isFixedMechanicalManip(String manipName) {
		return BLOOD_ABSORPTION.equals(manipName) || BLOOD_PROJECTION.equals(manipName)
				|| CONJURE_SICKLE.equals(manipName);
	}
}
