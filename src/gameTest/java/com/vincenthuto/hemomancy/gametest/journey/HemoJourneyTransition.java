package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import java.util.EnumSet;

/** Pure transition rule shared by the controller and headless semantic tests. */
public final class HemoJourneyTransition {
	private static final EnumSet<HemoJourneyStage> MAIN_STAGES = EnumSet.of(
			HemoJourneyStage.MORTAL_DISPLAY, HemoJourneyStage.SANGUINE_INITIATION,
			HemoJourneyStage.FIRST_BLOODCRAFT_PROOFS,
			HemoJourneyStage.FORMATION_PROJECTED, HemoJourneyStage.LIBER_CRAFTED,
			HemoJourneyStage.VICAR_REWARD, HemoJourneyStage.DEGREE_2_REACHED,
			HemoJourneyStage.ALCHEMIST_BRIEFING, HemoJourneyStage.CENTRIFUGE_PREPARED,
			HemoJourneyStage.SEPARATION_STARTED, HemoJourneyStage.ENZYME_RECOVERED,
			HemoJourneyStage.ALCHEMIST_REWARD, HemoJourneyStage.FIRST_DISTILLATION,
			HemoJourneyStage.CONCENTRATED_BLOOD_REST, HemoJourneyStage.WOVEN_VESSEL_TURN_IN,
			HemoJourneyStage.FIRST_MEMORY_WOVEN, HemoJourneyStage.ADEPT_RITE,
			HemoJourneyStage.VEIN_MASON_LESSON, HemoJourneyStage.FIRST_SCAR_CARVED,
			HemoJourneyStage.FIRST_SCAR_LEARNED, HemoJourneyStage.FIRST_EFFIGY_PATTERN,
			HemoJourneyStage.FIRST_EFFIGY_LOADOUT, HemoJourneyStage.VEIN_MASON_REWARD,
			HemoJourneyStage.ILLUMINATUS_RITE, HemoJourneyStage.FOUNDING_FANE,
			HemoJourneyStage.SANCTIFIED_RITE, HemoJourneyStage.CHAMBER_RETURNED,
			HemoJourneyStage.COVENANT_THRONE_BOUND, HemoJourneyStage.COVENANT_VIGIL,
			HemoJourneyStage.ARCHON_RITE, HemoJourneyStage.QLIPHOTH_COMMUNION,
			HemoJourneyStage.APOTHEOS_CHOICE, HemoJourneyStage.APOTHEOS_RITE,
			HemoJourneyStage.SILENT_REFUSAL, HemoJourneyStage.COMPLETE);
	private HemoJourneyTransition() {
	}

	public static boolean shouldVerify(HemoJourneyStage current, String verifiedStageId) {
		return verifiedStageId == null || !current.id().equals(verifiedStageId);
	}

	public static HemoJourneyStage next(HemoJourneyStage current, boolean verificationPassed,
			boolean preparationSucceeded) {
		return next(current, verificationPassed, preparationSucceeded, EnumArchonPath.NONE);
	}

	public static HemoJourneyStage next(HemoJourneyStage current, boolean verificationPassed,
			boolean preparationSucceeded, EnumArchonPath path) {
		return next(current, verificationPassed, preparationSucceeded, path, false);
	}

	public static HemoJourneyStage next(HemoJourneyStage current, boolean verificationPassed,
			boolean preparationSucceeded, EnumArchonPath path, boolean mainOnly) {
		if (!verificationPassed || !preparationSucceeded || current == HemoJourneyStage.COMPLETE) return current;
		if (current == HemoJourneyStage.APOTHEOS_CHOICE && path == EnumArchonPath.SILENT_PENDING) {
			return HemoJourneyStage.SILENT_REFUSAL;
		}
		if (current == HemoJourneyStage.APOTHEOS_RITE) return HemoJourneyStage.COMPLETE;
		HemoJourneyStage[] stages = HemoJourneyStage.values();
		for (int index = current.ordinal() + 1; index < stages.length; index++) {
			if (!mainOnly || MAIN_STAGES.contains(stages[index])) return stages[index];
		}
		return HemoJourneyStage.COMPLETE;
	}
}
