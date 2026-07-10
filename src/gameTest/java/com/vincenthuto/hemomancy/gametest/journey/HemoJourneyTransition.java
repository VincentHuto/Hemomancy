package com.vincenthuto.hemomancy.gametest.journey;

/** Pure transition rule shared by the controller and headless semantic tests. */
public final class HemoJourneyTransition {
	private HemoJourneyTransition() {
	}

	public static boolean shouldVerify(HemoJourneyStage current, String verifiedStageId) {
		return verifiedStageId == null || !current.id().equals(verifiedStageId);
	}

	public static HemoJourneyStage next(HemoJourneyStage current, boolean verificationPassed,
			boolean preparationSucceeded) {
		if (!verificationPassed || !preparationSucceeded || current == HemoJourneyStage.COMPLETE) return current;
		return HemoJourneyStage.values()[current.ordinal() + 1];
	}
}
