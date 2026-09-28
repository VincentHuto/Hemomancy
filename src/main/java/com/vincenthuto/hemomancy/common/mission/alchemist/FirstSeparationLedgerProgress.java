package com.vincenthuto.hemomancy.common.mission.alchemist;

/** Player-visible completion state for the required First Separation sequence. */
public record FirstSeparationLedgerProgress(
		boolean briefed,
		boolean centrifugeAcquired,
		boolean sampleAcquired,
		boolean separationStarted,
		boolean enzymeRecovered,
		boolean rewardClaimed,
		boolean concentratedBloodPending,
		boolean initiateReached) {

	public int completedSteps() {
		int completed = 0;
		if (briefed) completed++;
		if (centrifugeAcquired) completed++;
		if (sampleAcquired) completed++;
		if (separationStarted) completed++;
		if (enzymeRecovered) completed++;
		if (rewardClaimed) completed++;
		if (initiateReached) completed++;
		return completed;
	}

	public int totalSteps() {
		return 7;
	}

	public boolean complete() {
		return completedSteps() == totalSteps();
	}
}
