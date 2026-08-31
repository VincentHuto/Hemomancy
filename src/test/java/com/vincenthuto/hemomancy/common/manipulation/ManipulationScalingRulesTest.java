package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManipulationScalingRulesTest {
	@Test
	void crimsonCoronationScalesFromOneToEightLances() {
		assertEquals(1, ManipulationScalingRules.scaledCount(1, 8, 1, 80));
		assertEquals(4, ManipulationScalingRules.scaledCount(1, 8, 40, 80));
		assertEquals(8, ManipulationScalingRules.scaledCount(1, 8, 80, 80));
	}

	@Test
	void synapticStormParalysisScalesFromTenToSixtyTicks() {
		assertEquals(10, ManipulationScalingRules.scaledInt(10, 60, 0, 60));
		assertEquals(35, ManipulationScalingRules.scaledInt(10, 60, 30, 60));
		assertEquals(60, ManipulationScalingRules.scaledInt(10, 60, 90, 60));
	}

	@Test
	void eclipseWellScalesRadiusAndLifetime() {
		assertEquals(2.0D, ManipulationScalingRules.scaled(2.0D, 7.0D, 0, 80), 0.0001D);
		assertEquals(4.5D, ManipulationScalingRules.scaled(2.0D, 7.0D, 40, 80), 0.0001D);
		assertEquals(200, ManipulationScalingRules.scaledInt(40, 200, 80, 80));
	}
}
