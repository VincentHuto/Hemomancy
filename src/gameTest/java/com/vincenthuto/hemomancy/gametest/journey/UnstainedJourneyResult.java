package com.vincenthuto.hemomancy.gametest.journey;

public record UnstainedJourneyResult(boolean passed, UnstainedJourneyStage stage, String message) {
	public static UnstainedJourneyResult fail(UnstainedJourneyStage stage, String message) {
		return new UnstainedJourneyResult(false, stage, message);
	}
}
