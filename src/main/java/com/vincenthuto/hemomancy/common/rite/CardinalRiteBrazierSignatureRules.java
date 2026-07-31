package com.vincenthuto.hemomancy.common.rite;

import java.util.Map;

public final class CardinalRiteBrazierSignatureRules {
	private CardinalRiteBrazierSignatureRules() {
	}

	public static boolean exactMatch(Map<String, Integer> required, Map<String, Integer> offered) {
		return required != null && offered != null && required.equals(offered);
	}
}
