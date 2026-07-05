package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;

import java.util.List;

public final class BloodFlowRulesTest {
	private BloodFlowRulesTest() {
	}

	public static void main(String[] args) {
		directSourcesStackAndNetAgainstDrains();
		circulationSourcesShareBandwidth();
		disabledCirculationPassesThrough();
		appliedSnapshotPreservesRecordedCappedValues();
		skillRegenAndArmorRegenReportSeparately();
		sanguineSiphonAndFungalScarShareCap();
		bloodlineTrickleAndAutoDrawKeepPolarityAndConditions();
	}

	private static void directSourcesStackAndNetAgainstDrains() {
		BloodFlowSnapshot snapshot = BloodFlowRules.snapshot(List.of(
				BloodFlowContribution.direct("base", "Base Regen", Category.BODY, 0.05D),
				BloodFlowContribution.direct("surge", "Sanguine Surge", Category.SKILL, 1.0D),
				BloodFlowContribution.direct("blood_loss", "Blood Loss", Category.EFFECT, -0.5D)),
				6.0D, 0.0D, 20, true);

		assertDouble("positive flow totals direct regen", 1.05D, snapshot.positivePerTick());
		assertDouble("negative flow totals direct drains", 0.5D, snapshot.negativePerTick());
		assertDouble("net flow is positives minus negatives", 0.55D, snapshot.netPerTick());
	}

	private static void circulationSourcesShareBandwidth() {
		BloodFlowSnapshot snapshot = BloodFlowRules.snapshot(List.of(
				BloodFlowContribution.circulation("armor", "Hematic Iron", Category.ARMOR, 0.2D),
				BloodFlowContribution.circulation("scar", "Rooted Scar", Category.SCAR, 0.2D)),
				6.0D, 0.0D, 20, true);

		BloodFlowContribution armor = snapshot.contribution("armor").orElseThrow();
		BloodFlowContribution scar = snapshot.contribution("scar").orElseThrow();

		assertDouble("first capped source is granted fully", 0.2D, armor.appliedPerTick());
		assertDouble("second capped source is clipped by remaining bandwidth", 0.1D, scar.appliedPerTick());
		assertDouble("positive total reflects applied capped income", 0.3D, snapshot.positivePerTick());
		assertDouble("circulation used is tracked in window units", 6.0D, snapshot.circulationUsedInWindow());
		assertDouble("circulation remaining is tracked in window units", 0.0D, snapshot.circulationRemainingInWindow());
	}

	private static void disabledCirculationPassesThrough() {
		BloodFlowSnapshot snapshot = BloodFlowRules.snapshot(List.of(
				BloodFlowContribution.circulation("armor", "Hematic Iron", Category.ARMOR, 0.2D),
				BloodFlowContribution.circulation("scar", "Rooted Scar", Category.SCAR, 0.2D)),
				6.0D, 0.0D, 20, false);

		assertDouble("disabled circulation applies all requested income", 0.4D, snapshot.positivePerTick());
		assertDouble("disabled circulation records no cap usage", 0.0D, snapshot.circulationUsedInWindow());
	}

	private static void appliedSnapshotPreservesRecordedCappedValues() {
		BloodFlowSnapshot snapshot = BloodFlowRules.appliedSnapshot(List.of(
				BloodFlowContribution.circulation("armor", "Hematic Iron", Category.ARMOR, 0.2D)
						.withAppliedPerTick(0.2D, ""),
				BloodFlowContribution.circulation("scar", "Rooted Scar", Category.SCAR, 0.2D)
						.withAppliedPerTick(0.05D, "Circulation limited")),
				6.0D, 5.0D, 20, true);

		assertDouble("recorded applied positives are preserved", 0.25D, snapshot.positivePerTick());
		assertDouble("existing window use is preserved", 5.0D, snapshot.circulationUsedInWindow());
		assertDouble("existing window remaining is preserved", 1.0D, snapshot.circulationRemainingInWindow());
	}

	private static void skillRegenAndArmorRegenReportSeparately() {
		BloodFlowSnapshot snapshot = BloodFlowRules.snapshot(List.of(
				BloodFlowContribution.direct("sanguine_surge", "Sanguine Surge", Category.SKILL, 1.0D),
				BloodFlowContribution.circulation("hematic_iron_set", "Hematic Iron Set", Category.ARMOR, 0.1D)),
				6.0D, 0.0D, 20, true);

		assertDouble("skill and armor positives stack after cap math", 1.1D, snapshot.positivePerTick());
		assertDouble("skill source stays direct", 1.0D,
				snapshot.contribution("sanguine_surge").orElseThrow().appliedPerTick());
		assertDouble("armor source reports applied flow", 0.1D,
				snapshot.contribution("hematic_iron_set").orElseThrow().appliedPerTick());
	}

	private static void sanguineSiphonAndFungalScarShareCap() {
		BloodFlowSnapshot snapshot = BloodFlowRules.snapshot(List.of(
				BloodFlowContribution.circulation("sanguine_siphon", "Sanguine Siphon", Category.EFFECT, 0.25D),
				BloodFlowContribution.circulation("rhizovitta_rooted", "Rhizovitta Rooted", Category.SCAR, 0.25D)),
				6.0D, 0.0D, 20, true);

		assertDouble("siphon uses available cap first", 0.25D,
				snapshot.contribution("sanguine_siphon").orElseThrow().appliedPerTick());
		assertDouble("scar income is clipped by remaining cap", 0.05D,
				snapshot.contribution("rhizovitta_rooted").orElseThrow().appliedPerTick());
		assertDouble("capped positive total reflects applied values", 0.3D, snapshot.positivePerTick());
	}

	private static void bloodlineTrickleAndAutoDrawKeepPolarityAndConditions() {
		BloodFlowSnapshot snapshot = BloodFlowRules.appliedSnapshot(List.of(
				BloodFlowContribution.direct("bloodline_trickle", "Bloodline Trickle", Category.BLOODLINE, -0.25D),
				BloodFlowContribution.direct("bloodline_auto_draw", "Bloodline Auto-Draw", Category.BLOODLINE,
						1.0D, 0.5D, "Pool limited")),
				6.0D, 0.0D, 20, true);

		assertDouble("bloodline draw contributes positive flow", 0.5D, snapshot.positivePerTick());
		assertDouble("bloodline trickle contributes negative flow", 0.25D, snapshot.negativePerTick());
		assertDouble("bloodline net is positive minus negative", 0.25D, snapshot.netPerTick());
		if (!"Pool limited".equals(snapshot.contribution("bloodline_auto_draw").orElseThrow().condition())) {
			throw new AssertionError("bloodline auto-draw should preserve condition text");
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
