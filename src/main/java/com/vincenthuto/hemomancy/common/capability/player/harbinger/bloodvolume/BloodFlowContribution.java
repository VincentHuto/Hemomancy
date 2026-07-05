package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public record BloodFlowContribution(String sourceId, String label, Category category, double requestedPerTick,
		double appliedPerTick, boolean circulationLimited, String condition) {
	public enum Category {
		BODY,
		SKILL,
		EFFECT,
		ARMOR,
		MORPHLING,
		SCAR,
		BLOODLINE,
		WORLD,
		ORGAN,
		TOOL,
		VESSEL,
		OTHER
	}

	public BloodFlowContribution {
		sourceId = sourceId == null ? "" : sourceId;
		label = label == null ? sourceId : label;
		category = category == null ? Category.OTHER : category;
		requestedPerTick = finiteOrZero(requestedPerTick);
		appliedPerTick = finiteOrZero(appliedPerTick);
		condition = condition == null ? "" : condition;
	}

	public static BloodFlowContribution direct(String sourceId, String label, Category category,
			double requestedPerTick) {
		return direct(sourceId, label, category, requestedPerTick, requestedPerTick, "");
	}

	public static BloodFlowContribution direct(String sourceId, String label, Category category,
			double requestedPerTick, double appliedPerTick, String condition) {
		return new BloodFlowContribution(sourceId, label, category, requestedPerTick, appliedPerTick, false,
				condition);
	}

	public static BloodFlowContribution circulation(String sourceId, String label, Category category,
			double requestedPerTick) {
		return new BloodFlowContribution(sourceId, label, category, Math.max(0.0D, requestedPerTick), 0.0D,
				true, "");
	}

	public BloodFlowContribution withAppliedPerTick(double appliedPerTick, String condition) {
		return new BloodFlowContribution(sourceId, label, category, requestedPerTick, appliedPerTick,
				circulationLimited, condition);
	}

	public double positiveAppliedPerTick() {
		return Math.max(0.0D, appliedPerTick);
	}

	public double negativeAppliedPerTick() {
		return Math.max(0.0D, -appliedPerTick);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
