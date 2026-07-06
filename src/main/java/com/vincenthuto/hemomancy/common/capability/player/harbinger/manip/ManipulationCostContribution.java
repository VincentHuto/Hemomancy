package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

public record ManipulationCostContribution(String sourceId, String label, Category category, double multiplier,
		String condition) {
	public enum Category {
		SKILL,
		MASTERY,
		TENDENCY,
		EFFECT,
		RITE,
		WORLD,
		TOOL,
		OTHER
	}

	public ManipulationCostContribution {
		sourceId = sourceId == null ? "" : sourceId;
		label = label == null ? sourceId : label;
		category = category == null ? Category.OTHER : category;
		multiplier = sanitizeMultiplier(multiplier);
		condition = condition == null ? "" : condition;
	}

	public static ManipulationCostContribution multiplier(String sourceId, String label, Category category,
			double multiplier) {
		return new ManipulationCostContribution(sourceId, label, category, multiplier, "");
	}

	public static ManipulationCostContribution multiplier(String sourceId, String label, Category category,
			double multiplier, String condition) {
		return new ManipulationCostContribution(sourceId, label, category, multiplier, condition);
	}

	public boolean isDiscount() {
		return multiplier < 1.0D;
	}

	public boolean isSurcharge() {
		return multiplier > 1.0D;
	}

	private static double sanitizeMultiplier(double value) {
		if (!Double.isFinite(value)) {
			return 1.0D;
		}
		return Math.max(0.0D, value);
	}
}
