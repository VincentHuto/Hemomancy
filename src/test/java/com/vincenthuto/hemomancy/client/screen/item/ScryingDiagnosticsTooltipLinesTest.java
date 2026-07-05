package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotContribution;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotSnapshot;

import java.util.List;

public final class ScryingDiagnosticsTooltipLinesTest {
	private ScryingDiagnosticsTooltipLinesTest() {
	}

	public static void main(String[] args) {
		maxBloodTooltipListsPositiveAndNegativeSources();
		bloodFlowTooltipShowsAppliedAndRequestedRates();
		manipulationCostTooltipShowsMultiplierSources();
		manipulationSlotTooltipShowsSlotSourcesAndCap();
	}

	private static void maxBloodTooltipListsPositiveAndNegativeSources() {
		MaxBloodSnapshot snapshot = MaxBloodRules.snapshot(5000.0D, List.of(
				MaxBloodContribution.bonus("capacity", "Capacity",
						MaxBloodContribution.Category.SKILL, 1500.0D),
				MaxBloodContribution.bonus("spleen", "Spleen",
						MaxBloodContribution.Category.ORGAN, 3000.0D),
				MaxBloodContribution.penalty("scar_heart", "Scar Heart",
						MaxBloodContribution.Category.SCAR, 100.0D)));

		List<String> lines = ScryingDiagnosticsTooltipLines.maxBloodMods(snapshot);

		assertContains(lines, "Skill - Capacity: +1500 mL");
		assertContains(lines, "Organ - Spleen: +3000 mL");
		assertContains(lines, "Scar - Scar Heart: -100 mL");
	}

	private static void bloodFlowTooltipShowsAppliedAndRequestedRates() {
		BloodFlowSnapshot snapshot = new BloodFlowSnapshot(List.of(
				new BloodFlowContribution("base_regen", "Base Regen",
						BloodFlowContribution.Category.BODY, 0.5D, 0.5D, false, ""),
				new BloodFlowContribution("sanguine_siphon", "Sanguine Siphon",
						BloodFlowContribution.Category.EFFECT, 1.0D, 0.25D, true, "Circulation capped"),
				new BloodFlowContribution("blood_loss", "Blood Loss",
						BloodFlowContribution.Category.EFFECT, -2.0D, -2.0D, false, "")),
				0.75D, 2.0D, -1.25D, 100.0D, 25.0D, 75.0D, 20, true);

		List<String> lines = ScryingDiagnosticsTooltipLines.bloodFlowMods(snapshot);

		assertContains(lines, "Body - Base Regen: +0.50 mL/t");
		assertContains(lines, "Effect - Sanguine Siphon: +0.25/+1.00 mL/t [Circulation capped]");
		assertContains(lines, "Effect - Blood Loss: -2.00 mL/t");
	}

	private static void manipulationCostTooltipShowsMultiplierSources() {
		ManipulationCostSnapshot snapshot = ManipulationCostRules.snapshot(200.0D, List.of(
				ManipulationCostContribution.multiplier("efficiency", "Efficiency",
						ManipulationCostContribution.Category.SKILL, 0.8D),
				ManipulationCostContribution.multiplier("pome_corruption", "Pome Corruption",
						ManipulationCostContribution.Category.RITE, 1.24D)));

		List<String> lines = ScryingDiagnosticsTooltipLines.manipulationCostMods(snapshot);

		assertContains(lines, "Skill - Efficiency: x0.80");
		assertContains(lines, "Rite - Pome Corruption: x1.24");
		assertContains(lines, "Effective Cost: 148.80 mL");
	}

	private static void manipulationSlotTooltipShowsSlotSourcesAndCap() {
		ManipulationSlotSnapshot snapshot = ManipulationSlotRules.snapshot(List.of(
				ManipulationSlotContribution.amount("base", "Base Slots",
						ManipulationSlotContribution.Category.BASE, 3),
				ManipulationSlotContribution.amount("degree", "Initiatory Degree",
						ManipulationSlotContribution.Category.DEGREE, 4),
				ManipulationSlotContribution.amount("skill", "Manipulation Slots",
						ManipulationSlotContribution.Category.SKILL, 5)),
				9);

		List<String> lines = ScryingDiagnosticsTooltipLines.manipulationSlotMods(snapshot);

		assertContains(lines, "Base - Base Slots: +3");
		assertContains(lines, "Degree - Initiatory Degree: +4");
		assertContains(lines, "Skill - Manipulation Slots: +5");
		assertContains(lines, "Cap: 9 slots, 3 capped off");
	}

	private static void assertContains(List<String> lines, String expected) {
		if (!lines.contains(expected)) {
			throw new AssertionError("Expected tooltip lines to contain '" + expected + "' but got " + lines);
		}
	}
}
