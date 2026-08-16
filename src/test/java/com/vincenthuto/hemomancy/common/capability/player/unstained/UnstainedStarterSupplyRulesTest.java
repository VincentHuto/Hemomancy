package com.vincenthuto.hemomancy.common.capability.player.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UnstainedStarterSupplyRulesTest {
	@Test
	void eachPlayerCanClaimOnlyWhatTheirStarterInventoryIsMissing() {
		assertEquals(new UnstainedStarterSupplyRules.Grant(2, true),
				UnstainedStarterSupplyRules.grantFor(false, 0, false));
		assertEquals(new UnstainedStarterSupplyRules.Grant(1, false),
				UnstainedStarterSupplyRules.grantFor(false, 1, true));
		assertEquals(UnstainedStarterSupplyRules.Grant.NONE,
				UnstainedStarterSupplyRules.grantFor(true, 0, false));
	}

	@Test
	void perPlayerClaimSurvivesSaveAndReload() {
		var original = new UnstainedProgress();
		original.setClaimedChurchStarterSupply(true);
		var restored = new UnstainedProgress();

		restored.deserializeNBT(null, original.serializeNBT(null));

		assertEquals(true, restored.hasClaimedChurchStarterSupply());
	}
}
