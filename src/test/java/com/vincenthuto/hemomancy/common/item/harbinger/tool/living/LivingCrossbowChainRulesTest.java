package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class LivingCrossbowChainRulesTest {
	private static final UUID NEAR = new UUID(0L, 1L);
	private static final UUID FAR = new UUID(0L, 2L);

	@Test
	void threeHopsDealSeventyFiveFiftyAndTwentyFivePercent() {
		assertEquals(7.5F, LivingCrossbowChainRules.damageForHop(10.0F, 0), 0.0001F);
		assertEquals(5.0F, LivingCrossbowChainRules.damageForHop(10.0F, 1), 0.0001F);
		assertEquals(2.5F, LivingCrossbowChainRules.damageForHop(10.0F, 2), 0.0001F);
		assertEquals(0.0F, LivingCrossbowChainRules.damageForHop(10.0F, 3), 0.0001F);
	}

	@Test
	void nextHopChoosesNearestVisibleUnvisitedHostileWithinFiveBlocks() {
		var candidates = List.of(
				new LivingCrossbowChainRules.Candidate(FAR, 4.0D, 0.0D, 0.0D, true, true),
				new LivingCrossbowChainRules.Candidate(NEAR, 2.0D, 0.0D, 0.0D, true, true),
				new LivingCrossbowChainRules.Candidate(new UUID(0L, 3L), 1.0D, 0.0D, 0.0D, false, true));

		assertEquals(NEAR, LivingCrossbowChainRules.nextHop(candidates, 0.0D, 0.0D, 0.0D, Set.of()).id());
		assertEquals(FAR, LivingCrossbowChainRules.nextHop(candidates, 0.0D, 0.0D, 0.0D, Set.of(NEAR)).id());
	}

	@Test
	void invisibleOrOutOfRangeCandidatesCannotContinueTheChain() {
		var candidates = List.of(
				new LivingCrossbowChainRules.Candidate(NEAR, 2.0D, 0.0D, 0.0D, true, false),
				new LivingCrossbowChainRules.Candidate(FAR, 5.01D, 0.0D, 0.0D, true, true));

		assertNull(LivingCrossbowChainRules.nextHop(candidates, 0.0D, 0.0D, 0.0D, Set.of()));
	}
}
