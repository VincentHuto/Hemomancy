package com.vincenthuto.hemomancy.gametest.journey;

public record HemoJourneyResult(boolean passed, HemoJourneyStage stage, String message) {
	public static HemoJourneyResult fail(HemoJourneyStage stage, String message) {
		return new HemoJourneyResult(false, stage, message);
	}
}
