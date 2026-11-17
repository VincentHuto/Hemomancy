package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** CN-9, NQ-01-16: who may stand in for an offline owner at a sutured machine. */
class BloodRoutingStandInRulesTest {
	@Test
	void kinStandInOnlyWhenAliveInitiatedAwakeAndNear() {
		assertEquals(16.0D, BloodRoutingRules.STAND_IN_RANGE);
		assertTrue(BloodRoutingRules.mayStandIn(true, true, true, 16.0D * 16.0D));
		assertFalse(BloodRoutingRules.mayStandIn(true, true, true, 16.5D * 16.5D), "too far");
		assertFalse(BloodRoutingRules.mayStandIn(false, true, true, 1.0D), "dead");
		assertFalse(BloodRoutingRules.mayStandIn(true, false, true, 1.0D), "below the third Degree");
		assertFalse(BloodRoutingRules.mayStandIn(true, true, false, 1.0D), "blood asleep");
	}
}
