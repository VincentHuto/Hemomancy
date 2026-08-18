package com.vincenthuto.hemomancy.common.mission;

import java.util.Locale;

public final class ArtificerProgressionRules {
	public enum ForkFamily {
		NONE, BARBED, CHITINITE, PRISMATIC;

		public String serializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		public static ForkFamily fromSerializedName(String value) {
			if (value == null || value.isBlank()) return NONE;
			try {
				return valueOf(value.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				return NONE;
			}
		}
	}

	public enum D7Lineage {
		NONE, SILENT_ARCHON, EDACIOUS, SHEOLIC, PHANTASMAL;

		public String serializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		public static D7Lineage fromSerializedName(String value) {
			if (value == null || value.isBlank()) return NONE;
			try {
				return valueOf(value.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				return NONE;
			}
		}
	}

	public enum Step {
		LOCKED, BRIEFING, PLACE_ARMATURE, CONSECRATE, CORNERSTONE, FIRST_UPGRADE,
		RECOVER_BRANCH, INSPECTION, CORRESPONDENCE, MATERIAL_REWARD, FULL_SET, DEMONSTRATION, LEARN_FORMS,
		FITTING, COMPLETE
	}

	private ArtificerProgressionRules() {
	}

	public static boolean canDemonstrate(boolean briefed, boolean correspondenceComplete,
			boolean upgraded, boolean matchingSet) {
		return briefed && correspondenceComplete && upgraded && matchingSet;
	}

	public static int packSteps(Step... steps) {
		int packed = 0;
		for (int i = 0; i < steps.length && i < 7; i++) packed |= (steps[i].ordinal() & 0xF) << i * 4;
		return packed;
	}

	public static Step unpackStep(int packed, int index) {
		int ordinal = packed >>> index * 4 & 0xF;
		return ordinal < Step.values().length ? Step.values()[ordinal] : Step.LOCKED;
	}

	public static Step nextThreeAnswers(boolean briefed, boolean upgraded, boolean branchKnown, boolean inspected,
			boolean counseled, boolean fullSet, boolean demonstrated, boolean fitting) {
		if (fitting) return Step.COMPLETE;
		if (!briefed) return Step.BRIEFING;
		if (!upgraded) return Step.FIRST_UPGRADE;
		if (!branchKnown) return Step.RECOVER_BRANCH;
		if (!inspected) return Step.INSPECTION;
		if (!counseled) return Step.CORRESPONDENCE;
		if (!fullSet) return Step.FULL_SET;
		if (!demonstrated) return Step.DEMONSTRATION;
		return Step.FITTING;
	}

	public static Step nextWornVow(boolean briefed, boolean placed, boolean upgraded,
			boolean inspected, boolean fullSet, boolean fitting) {
		if (fitting) return Step.COMPLETE;
		if (!briefed) return Step.BRIEFING;
		if (!placed) return Step.PLACE_ARMATURE;
		if (!upgraded) return Step.FIRST_UPGRADE;
		if (!inspected) return Step.INSPECTION;
		if (!fullSet) return Step.FULL_SET;
		return Step.FITTING;
	}

	public static Step nextCrimsonVestment(boolean briefed, boolean consecrated, boolean inspected,
			boolean counseled, boolean upgraded, boolean fullSet, boolean demonstrated, boolean fitting) {
		if (fitting) return Step.COMPLETE;
		if (!briefed) return Step.BRIEFING;
		if (!consecrated) return Step.CONSECRATE;
		if (!inspected) return Step.INSPECTION;
		if (!counseled) return Step.CORRESPONDENCE;
		if (!upgraded) return Step.FIRST_UPGRADE;
		if (!fullSet) return Step.FULL_SET;
		if (!demonstrated) return Step.DEMONSTRATION;
		return Step.FITTING;
	}

	public static Step nextAssumedLimb(boolean briefed, boolean grafted, boolean inspected,
			boolean demonstrated, int knownForms, boolean fitting) {
		if (fitting) return Step.COMPLETE;
		if (!briefed) return Step.BRIEFING;
		if (!grafted) return Step.FIRST_UPGRADE;
		if (!inspected) return Step.INSPECTION;
		if (!demonstrated) return Step.DEMONSTRATION;
		if (knownForms < 7) return Step.LEARN_FORMS;
		return Step.FITTING;
	}

	public static Step nextWeightOfFrame(boolean briefed, boolean cornerstone, boolean upgraded, boolean branchKnown,
			boolean inspected, boolean rewarded, boolean fullSet, boolean demonstrated, boolean fitting) {
		if (fitting) {
			if (!branchKnown) return Step.RECOVER_BRANCH;
			return rewarded ? Step.COMPLETE : Step.MATERIAL_REWARD;
		}
		if (!briefed) return Step.BRIEFING;
		if (!cornerstone) return Step.CORNERSTONE;
		if (!upgraded) return Step.FIRST_UPGRADE;
		if (!branchKnown) return Step.RECOVER_BRANCH;
		if (!inspected) return Step.INSPECTION;
		if (!rewarded) return Step.MATERIAL_REWARD;
		if (!fullSet) return Step.FULL_SET;
		if (!demonstrated) return Step.DEMONSTRATION;
		return Step.FITTING;
	}
}
