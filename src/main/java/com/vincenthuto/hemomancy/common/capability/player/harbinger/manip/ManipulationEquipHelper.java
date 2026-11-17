package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ManipulationEquipHelper {
	public static final String BLOOD_ABSORPTION = "blood_absorption";
	public static final String BLOOD_PROJECTION = "blood_projection";
	public static final String CONJURE_SICKLE = "conjure_sickle";
	public static final String CONJURE_STAFF = "conjure_staff";

	private ManipulationEquipHelper() {
	}

	public static boolean equipNameIfPossible(List<String> equippedNames, String manipName, int maxSlots) {
		if (equippedNames == null || manipName == null || manipName.isEmpty()) {
			return false;
		}
		boolean changed = normalizeEquippedNames(equippedNames);
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
		LinkedHashSet<String> normalNames = new LinkedHashSet<>();
		for (String name : equippedNames) {
			if (name != null && !name.isEmpty() && !isMemorizationCapExempt(name)) {
				normalNames.add(name);
			}
		}
		equippedNames.clear();
		equippedNames.add(CONJURE_STAFF);
		equippedNames.addAll(normalNames);
		return !original.equals(equippedNames);
	}

	/**
	 * Projection and Absorption are the two verbs (CL-2): known from Degree 1, chosen with the verb key, never equipped
	 * or selected. A saved list that still names them drops them when normalized.
	 */
	public static boolean isVerb(String manipName) {
		return BLOOD_ABSORPTION.equals(manipName) || BLOOD_PROJECTION.equals(manipName);
	}

	public static int countNormalEquippedNames(List<String> equippedNames) {
		if (equippedNames == null) {
			return 0;
		}
		int count = 0;
		for (String name : equippedNames) {
			if (name != null && !name.isEmpty() && !isMemorizationCapExempt(name)) {
				count++;
			}
		}
		return count;
	}

	public static boolean isFixedMechanicalManip(String manipName) {
		return isMemorizationCapExempt(manipName);
	}

	public static boolean isMemorizationCapExempt(String manipName) {
		return isVerb(manipName) || CONJURE_STAFF.equals(manipName);
	}
}
