package com.vincenthuto.hemomancy.common.mission.unstained;

import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances.Observance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class UnstainedNovitiateVowsTest {
	@Test
	void healthyVowsUnlockInOrderWithoutBeginningPurification() {
		UnstainedProgress progress = new UnstainedProgress();
		assertTrue(UnstainedObservances.isAvailable(progress, Observance.NOVITIATE_GATHER_REMEDIES));
		assertFalse(UnstainedObservances.isAvailable(progress, Observance.NOVITIATE_GENTLE_SEPARATION));

		progress.setClaimedObservances(Observance.NOVITIATE_GATHER_REMEDIES.mask());
		assertTrue(UnstainedObservances.isAvailable(progress, Observance.NOVITIATE_GENTLE_SEPARATION));
		assertFalse(progress.hasBegunPurification());
	}

	@Test
	void bloodedPurificationDoesNotExposeHealthyVows() {
		UnstainedProgress progress = new UnstainedProgress();
		progress.setBegunPurification(true);

		assertFalse(UnstainedObservances.isAvailable(progress, Observance.NOVITIATE_GATHER_REMEDIES));
		assertTrue(UnstainedObservances.isAvailable(progress, Observance.GATHER_GHOST_PIPE));
	}
}
