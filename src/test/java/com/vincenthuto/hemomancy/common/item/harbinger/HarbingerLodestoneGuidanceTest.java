package com.vincenthuto.hemomancy.common.item.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HarbingerLodestoneGuidanceTest {

	@Test
	void vignetteStrengthensAsPlayerTurnsTowardTarget() {
		double facing = HarbingerLodestoneGuidance.alignment(0.0F, 0.0D, 100.0D);
		double turning = HarbingerLodestoneGuidance.alignment(45.0F, 0.0D, 100.0D);
		double perpendicular = HarbingerLodestoneGuidance.alignment(90.0F, 0.0D, 100.0D);
		double away = HarbingerLodestoneGuidance.alignment(180.0F, 0.0D, 100.0D);

		assertEquals(1.0D, facing, 0.0001D);
		assertTrue(turning > perpendicular);
		assertEquals(0.0D, perpendicular, 0.0001D);
		assertEquals(0.0D, away, 0.0001D);
	}

	@Test
	void minecraftYawFacesEastAtNegativeNinetyDegrees() {
		assertEquals(1.0D, HarbingerLodestoneGuidance.alignment(-90.0F, 100.0D, 0.0D), 0.0001D);
	}
}
