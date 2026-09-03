package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

public final class CircusIntroductionRulesTest {
	public static void main(String[] args) {
		assert CircusIntroductionRules.introductionFor(3, false)
				== CircusIntroductionRules.Introduction.HIDDEN;
		assert CircusIntroductionRules.introductionFor(4, false)
				== CircusIntroductionRules.Introduction.UNDISCOVERED;
		assert CircusIntroductionRules.introductionFor(4, true)
				== CircusIntroductionRules.Introduction.DISCOVERED;
		assert CircusIntroductionRules.canRequestWaybill(4, false);
		assert !CircusIntroductionRules.canRequestWaybill(3, false);
		assert !CircusIntroductionRules.canRequestWaybill(4, true);
	}
}
