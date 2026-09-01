package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardinalRiteFootprintRulesTest {

	@Test
	void grandOrdealCeremonyExtendsBeyondItsMatchedFloor() {
		float radius = CardinalRiteFootprintRules.enclosingRadius(
				6.0F, 7.5F,
				List.of(new BlockPos(0, 0, 9)),
				List.of(new BlockPos(10, 0, 0)),
				List.of(new BlockPos(11, 0, 0)));

		assertEquals(11.75F, radius, 0.0001F,
				"the Grand floor must not cap distant support-sigil nodes");
	}

	@Test
	void completeFootprintScalesAcrossEveryRiteTier() {
		assertEquals(5.75F, enclosingTierRadius(3.5F, 4.5F, 5), 0.0001F, "minor");
		assertEquals(7.75F, enclosingTierRadius(4.5F, 5.5F, 7), 0.0001F, "lesser");
		assertEquals(9.75F, enclosingTierRadius(5.5F, 6.5F, 9), 0.0001F, "greater");
		assertEquals(11.75F, enclosingTierRadius(6.5F, 7.5F, 11), 0.0001F, "grand");
	}

	@Test
	void unresolvedSupportSigilStillReservesItsSocket() {
		float radius = CardinalRiteFootprintRules.enclosingRadius(
				4.5F, 5.5F, List.of(),
				List.of(new BlockPos(8, 0, 0)), List.of());

		assertEquals(8.75F, radius, 0.0001F,
				"an unknown sigil must not make its authored support station disappear");
	}

	@Test
	void fallbackAndFloorRemainMinimumBounds() {
		assertEquals(9.0F, CardinalRiteFootprintRules.enclosingRadius(
				9.0F, 7.5F, List.of(), List.of(), List.of()), 0.0001F);
		assertEquals(7.5F, CardinalRiteFootprintRules.enclosingRadius(
				4.0F, 7.5F, List.of(), List.of(), List.of()), 0.0001F);
	}

	@Test
	void awakenedSigilOrbitScalesAcrossTheFullRitualFootprint() {
		float smallFootprint = 3.75F;
		float largeFootprint = 11.75F;

		float smallOrbit = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(smallFootprint);
		float largeOrbit = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(largeFootprint);

		assertEquals(2.5F, smallOrbit, 0.0001F);
		assertEquals(10.5F, largeOrbit, 0.0001F);
		assertTrue(largeOrbit > 3.6F, "large rites must not retain the legacy orbit cap");
	}

	@Test
	void awakenedSigilOrbitLeavesClearanceInsideTheFaneShell() {
		float footprint = 11.75F;
		float orbit = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(footprint);

		assertTrue(orbit + CardinalRiteFootprintRules.AWAKENED_SIGIL_CLEARANCE <= footprint);
	}

	@Test
	void awakenedSigilOrbitKeepsAMinimumUsefulRadiusForTinyFootprints() {
		assertEquals(CardinalRiteFootprintRules.MIN_AWAKENED_SIGIL_ORBIT_RADIUS,
				CardinalRiteFootprintRules.awakenedSigilOrbitRadius(0.0F), 0.0001F);
	}

	private static float enclosingTierRadius(float fallback, float floor, int farthestPoint) {
		return CardinalRiteFootprintRules.enclosingRadius(
				fallback, floor,
				List.of(new BlockPos(farthestPoint - 2, 0, 0)),
				List.of(new BlockPos(farthestPoint - 1, 0, 0)),
				List.of(new BlockPos(farthestPoint, 0, 0)));
	}
}
