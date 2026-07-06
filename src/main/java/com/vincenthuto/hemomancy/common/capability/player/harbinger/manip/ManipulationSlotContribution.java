package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

public record ManipulationSlotContribution(String sourceId, String label, Category category, int amount,
		String condition) {
	public enum Category {
		BASE,
		DEGREE,
		SKILL,
		OTHER
	}

	public ManipulationSlotContribution {
		sourceId = sourceId == null ? "" : sourceId;
		label = label == null ? sourceId : label;
		category = category == null ? Category.OTHER : category;
		condition = condition == null ? "" : condition;
	}

	public static ManipulationSlotContribution amount(String sourceId, String label, Category category, int amount) {
		return new ManipulationSlotContribution(sourceId, label, category, amount, "");
	}
}
