package com.vincenthuto.hemomancy.common.rite;

public final class CardinalRiteOfferingConsumptionRules {
	private CardinalRiteOfferingConsumptionRules() {
	}

	public static boolean fromNullable(Boolean configured) {
		return configured == null || configured;
	}
}
