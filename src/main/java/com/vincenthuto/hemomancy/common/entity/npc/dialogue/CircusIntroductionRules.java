package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

final class CircusIntroductionRules {
	static final int MINIMUM_DEGREE = 4;

	private CircusIntroductionRules() {
	}

	static Introduction introductionFor(int degree, boolean discovered) {
		if (degree < MINIMUM_DEGREE) return Introduction.HIDDEN;
		return discovered ? Introduction.DISCOVERED : Introduction.UNDISCOVERED;
	}

	static boolean canRequestWaybill(int degree, boolean carryingWaybill) {
		return degree >= MINIMUM_DEGREE && !carryingWaybill;
	}

	enum Introduction {
		HIDDEN,
		UNDISCOVERED,
		DISCOVERED
	}
}
