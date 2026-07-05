package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

public record MaxBloodContribution(String sourceId, String label, Category category, double amount,
		String condition) {
	public enum Category {
		SKILL,
		ORGAN,
		RITE,
		SCAR,
		OTHER
	}

	public MaxBloodContribution {
		sourceId = sourceId == null ? "" : sourceId;
		label = label == null ? sourceId : label;
		category = category == null ? Category.OTHER : category;
		amount = finiteOrZero(amount);
		condition = condition == null ? "" : condition;
	}

	public static MaxBloodContribution bonus(String sourceId, String label, Category category, double amount) {
		return new MaxBloodContribution(sourceId, label, category, Math.max(0.0D, amount), "");
	}

	public static MaxBloodContribution penalty(String sourceId, String label, Category category, double amount) {
		return new MaxBloodContribution(sourceId, label, category, -Math.max(0.0D, amount), "");
	}

	public double positiveAmount() {
		return Math.max(0.0D, amount);
	}

	public double negativeAmount() {
		return Math.max(0.0D, -amount);
	}

	private static double finiteOrZero(double value) {
		return Double.isFinite(value) ? value : 0.0D;
	}
}
