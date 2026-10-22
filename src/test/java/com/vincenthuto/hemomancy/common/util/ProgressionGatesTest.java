package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** S-6 / D-3: degree gates at every boundary, and the Unstained stage ladder at each of its thresholds. */
final class ProgressionGatesTest {
	@Test
	void hasDegreeIsReachedAtTheDegreeAndStaysReached() {
		for (int actual = 0; actual <= 8; actual++) {
			InitiatoryDegree state = degree(actual);
			for (int gate = 0; gate <= 9; gate++) {
				assertEquals(actual >= gate, ProgressionGates.hasDegree(state, gate), "D" + actual + " vs gate " + gate);
				assertEquals(actual == gate, ProgressionGates.isDegree(state, gate), "D" + actual + " is " + gate);
			}
		}
	}

	@Test
	void theRewrittenComparisonsKeepTheirMeaning() {
		// The literal comparisons became: > n -> hasDegree(n + 1), <= n -> !hasDegree(n + 1), < n -> !hasDegree(n).
		for (int actual = 0; actual <= 8; actual++) {
			InitiatoryDegree state = degree(actual);
			for (int n = 0; n <= 8; n++) {
				assertEquals(actual > n, ProgressionGates.hasDegree(state, n + 1));
				assertEquals(actual <= n, !ProgressionGates.hasDegree(state, n + 1));
				assertEquals(actual < n, !ProgressionGates.hasDegree(state, n));
				assertEquals(actual != n, !ProgressionGates.isDegree(state, n));
			}
		}
	}

	@Test
	void unstainedStagesFollowPurityThenClarity() {
		UnstainedProgress progress = new UnstainedProgress();
		assertEquals(0, ProgressionGates.unstainedStage(progress));
		progress.setBegunPurification(true);
		assertEquals(1, stageAtPurity(progress, 0f));
		assertEquals(1, stageAtPurity(progress, 24.99f));
		assertEquals(2, stageAtPurity(progress, 25f));
		assertEquals(3, stageAtPurity(progress, 50f));
		assertEquals(4, stageAtPurity(progress, 75f));
		assertEquals(4, stageAtPurity(progress, 99.99f));
		assertEquals(5, stageAtPurity(progress, 100f));

		progress.setClarityUnlocked(true);
		assertEquals(5, stageAtClarity(progress, 0f));
		assertEquals(5, stageAtClarity(progress, 24.99f));
		assertEquals(6, stageAtClarity(progress, 25f));
		assertEquals(7, stageAtClarity(progress, 50f));
		assertEquals(7, stageAtClarity(progress, 99.99f));
		assertEquals(8, stageAtClarity(progress, 100f));
	}

	@Test
	void unlockedClarityOutranksPurity() {
		UnstainedProgress progress = new UnstainedProgress();
		progress.setClarityUnlocked(true);
		progress.setPurity(10f);
		assertEquals(5, ProgressionGates.unstainedStage(progress));
	}

	private static int stageAtPurity(UnstainedProgress progress, float purity) {
		progress.setPurity(purity);
		return ProgressionGates.unstainedStage(progress);
	}

	private static int stageAtClarity(UnstainedProgress progress, float clarity) {
		progress.setClarity(clarity);
		return ProgressionGates.unstainedStage(progress);
	}

	private static InitiatoryDegree degree(int number) {
		InitiatoryDegree state = new InitiatoryDegree();
		state.setDegreeNumber(number);
		return state;
	}
}
