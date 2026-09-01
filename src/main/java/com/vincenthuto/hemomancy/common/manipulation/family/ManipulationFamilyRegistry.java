package com.vincenthuto.hemomancy.common.manipulation.family;

import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ManipulationFamilyRegistry {
	private static final List<ManipulationFamilyDefinition> FAMILIES = List.of(
			family("blood_binding", form("lingering_blood_binding", 1), form("chain_blood_binding", 2),
					form("blood_lattice", 4)),
			family("blood_needle", form("blood_needle_fan", 1), form("blood_needle_lance", 2)),
			family("blood_shot", form("guided_blood_shot", 1), form("hematic_mortar", 3),
					form("sanguine_halo", 4)),
			family("blood_cloud", form("expansive_blood_cloud", 1), form("pursuing_blood_cloud", 2),
					form("sanguine_tempest", 4)),
			family("scalding_updraft", form("soaring_updraft", 1), form("suspended_updraft", 2),
					form("expulsive_updraft", 3)),
			family("lignum_mortis", form("canopy_mortis", 1), form("worked_lignum", 3)),
			family("summon_avatar", form("summon_avatar_arms", 1), form("summon_avatar_armor", 2),
					form("summon_avatar_legs", 3), form("summon_avatar_complete", 4)));

	private static final Map<String, ManipulationFamilyDefinition> BY_ID = new LinkedHashMap<>();
	private static final Map<String, ManipulationFormDefinition> FORMS_BY_ID = new LinkedHashMap<>();

	static {
		for (ManipulationFamilyDefinition family : FAMILIES) {
			BY_ID.put(family.baselineId(), family);
			for (ManipulationFormDefinition form : family.forms()) {
				BY_ID.put(form.id(), family);
				FORMS_BY_ID.put(form.id(), form);
			}
		}
	}

	private ManipulationFamilyRegistry() {
	}

	public static List<ManipulationFamilyDefinition> families() {
		return FAMILIES;
	}

	public static Optional<ManipulationFamilyDefinition> family(String manipulationId) {
		return Optional.ofNullable(BY_ID.get(manipulationId));
	}

	public static Optional<ManipulationFormDefinition> form(String manipulationId) {
		return Optional.ofNullable(FORMS_BY_ID.get(manipulationId));
	}

	public static String baselineId(String manipulationId) {
		ManipulationFamilyDefinition family = BY_ID.get(manipulationId);
		return family != null ? family.baselineId() : manipulationId;
	}

	public static boolean normalizeKnown(LinkedHashMap<BloodManipulation, ManipLevel> known) {
		if (known == null || known.isEmpty()) return false;
		boolean changed = false;
		for (ManipulationFamilyDefinition family : FAMILIES) {
			ManipLevel shared = null;
			int highestLevel = 0;
			double highestXp = 0;
			for (Map.Entry<BloodManipulation, ManipLevel> entry : known.entrySet()) {
				BloodManipulation manipulation = entry.getKey();
				if (manipulation == null || BY_ID.get(manipulation.getName()) != family) continue;
				ManipLevel candidate = entry.getValue();
				if (candidate == null) continue;
				highestLevel = Math.max(highestLevel, candidate.getCurrentLevel());
				highestXp = Math.max(highestXp, candidate.getXp());
				if (shared == null || candidate.getCurrentLevel() > shared.getCurrentLevel()
						|| candidate.getCurrentLevel() == shared.getCurrentLevel()
						&& candidate.getXp() > shared.getXp()) shared = candidate;
			}
			if (shared == null) continue;
			if (shared.getCurrentLevel() != highestLevel || shared.getXp() != highestXp) {
				shared.setCurrentLevel(highestLevel);
				shared.setXp(highestXp);
				changed = true;
			}
			for (Map.Entry<BloodManipulation, ManipLevel> entry : known.entrySet()) {
				BloodManipulation manipulation = entry.getKey();
				if (manipulation != null && BY_ID.get(manipulation.getName()) == family
						&& entry.getValue() != shared) {
					entry.setValue(shared);
					changed = true;
				}
			}
		}
		return changed;
	}

	public static boolean unlockEligibleForms(LinkedHashMap<BloodManipulation, ManipLevel> known) {
		if (known == null || known.isEmpty()) return false;
		boolean changed = normalizeKnown(known);
		for (ManipulationFamilyDefinition family : FAMILIES) {
			ManipLevel shared = levelForFamily(known, family);
			if (shared == null) continue;
			changed |= addIfAvailable(known, family.baselineId(), shared);
			for (ManipulationFormDefinition form : family.forms()) {
				if (shared.getCurrentLevel() >= form.requiredLevel()) {
					changed |= addIfAvailable(known, form.id(), shared);
				}
			}
		}
		return changed;
	}

	private static ManipLevel levelForFamily(LinkedHashMap<BloodManipulation, ManipLevel> known,
			ManipulationFamilyDefinition family) {
		for (Map.Entry<BloodManipulation, ManipLevel> entry : known.entrySet()) {
			if (entry.getKey() != null && BY_ID.get(entry.getKey().getName()) == family) return entry.getValue();
		}
		return null;
	}

	private static boolean addIfAvailable(LinkedHashMap<BloodManipulation, ManipLevel> known, String id,
			ManipLevel shared) {
		for (BloodManipulation current : known.keySet()) {
			if (current != null && id.equals(current.getName())) return false;
		}
		BloodManipulation manipulation = ManipulationInit.getByName(id);
		if (manipulation == null) return false;
		known.put(manipulation, shared);
		return true;
	}

	private static ManipulationFamilyDefinition family(String baselineId, ManipulationFormDefinition... forms) {
		return new ManipulationFamilyDefinition(baselineId, List.of(forms));
	}

	private static ManipulationFormDefinition form(String id, int requiredLevel) {
		return new ManipulationFormDefinition(id, requiredLevel, requiredLevel);
	}
}
