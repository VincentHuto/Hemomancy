package com.vincenthuto.hemomancy.common.vein;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EarthenVeinFeedingVortexProfileTest {
	@Test
	void satedVeinChangesFromBlackToASlowerRedInwardSpiral() {
		var feeding = EarthenVeinFeedingVortexProfile.forPhase(EarthenVeinTravelPhase.FEEDING, 1.0F);
		var ready = EarthenVeinFeedingVortexProfile.forPhase(EarthenVeinTravelPhase.READY, 1.0F);

		assertEquals(EarthenVeinFeedingVortexProfile.Effect.DARK_GLOW, feeding.effect());
		assertEquals(EarthenVeinFeedingVortexProfile.Effect.BLOOD_GLOW, ready.effect());
		assertTrue(ready.angularSpeed() < feeding.angularSpeed());
		assertTrue(ready.inwardSpeed() < feeding.inwardSpeed());
		assertTrue(feeding.graspingTendrils());
		assertTrue(ready.graspingTendrils());
	}

	@Test
	void ejectionUsesTheAuthoredDarkGlowWithoutVanillaSmoke() {
		var ejection = EarthenVeinFeedingVortexProfile.forPhase(EarthenVeinTravelPhase.EJECTING, 0.5F);

		assertEquals(EarthenVeinFeedingVortexProfile.Effect.DARK_GLOW, ejection.effect());
		assertTrue(ejection.graspingTendrils());
	}
}
