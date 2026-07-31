package com.vincenthuto.hemomancy.common.rite;

public final class CardinalRiteStructureConsumptionRules {
	private CardinalRiteStructureConsumptionRules() {
	}

	public static boolean fromNullable(Boolean configured) {
		return Boolean.TRUE.equals(configured);
	}

	public static boolean shouldConsume(boolean configured, boolean committedSuccess) {
		return configured && committedSuccess;
	}
}
