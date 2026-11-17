package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public final class CircusIntroductionRules {
	public static final int MINIMUM_DEGREE = 4;

	private CircusIntroductionRules() {
	}

	/** One Degree before the Waybill: the Mnemonist names the Circus and its country, and writes nothing yet. */
	public static final int TEASER_DEGREE = MINIMUM_DEGREE - 1;

	static Introduction introductionFor(int degree, boolean discovered) {
		if (degree == TEASER_DEGREE) return Introduction.TEASER;
		if (degree < MINIMUM_DEGREE) return Introduction.HIDDEN;
		return discovered ? Introduction.DISCOVERED : Introduction.UNDISCOVERED;
	}

	static boolean canRequestWaybill(int degree, boolean carryingWaybill) {
		return degree >= MINIMUM_DEGREE && !carryingWaybill;
	}

	enum Introduction {
		HIDDEN,
		TEASER,
		UNDISCOVERED,
		DISCOVERED
	}
}
