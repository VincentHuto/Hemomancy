package com.vincenthuto.hemomancy.gametest.journey;

public record CircusJourneyResult(boolean passed, CircusJourneyStage stage, String message) {
	static CircusJourneyResult fail(CircusJourneyStage stage, String message) {
		return new CircusJourneyResult(false, stage, message);
	}
}
