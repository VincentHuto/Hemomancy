package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class IchorianSigilPreviewInteractionTest {

	@Test
	void morphCycleHoldsBothEndpointsAndReturnsToItsStartingPose() {
		assertSample(0, 0.0F, 0.0F);
		assertSample(19, 0.0F, 19.0F);
		assertSample(20, 0.0F, 20.0F);
		assertSample(40, 20.0F, 40.0F);
		assertSample(60, 40.0F, 60.0F);
		assertSample(99, 40.0F, 99.0F);
		assertSample(100, 40.0F, 100.0F);
		assertSample(120, 20.0F, 120.0F);
		assertSample(140, 0.0F, 140.0F);
		assertSample(159, 0.0F, 159.0F);
		assertSample(160, 0.0F, 160.0F);
	}

	@Test
	void partialTicksProduceSmoothMorphSamples() {
		IchorianSigilPreviewCycle.Sample sample = IchorianSigilPreviewCycle.sample(40, 0.5F);
		assertEquals(20.5F, sample.morphAgeTicks(), 0.0001F);
		assertEquals(40.5F, sample.animationAgeTicks(), 0.0001F);
	}

	@Test
	void sigilRowsOnlyHitInsideExpandedVisibleRowBounds() {
		assertEquals(-1, IchorianSigilSidebarLayout.rowIndexAt(
				15, 43, 10, 20, 100, false, 3));
		assertEquals(-1, IchorianSigilSidebarLayout.rowIndexAt(
				9, 43, 10, 20, 100, true, 3));
		assertEquals(0, IchorianSigilSidebarLayout.rowIndexAt(
				15, 43, 10, 20, 100, true, 3));
		assertEquals(1, IchorianSigilSidebarLayout.rowIndexAt(
				15, 59, 10, 20, 100, true, 3));
		assertEquals(2, IchorianSigilSidebarLayout.rowIndexAt(
				15, 75, 10, 20, 100, true, 3));
		assertEquals(-1, IchorianSigilSidebarLayout.rowIndexAt(
				15, 91, 10, 20, 100, true, 3));
	}

	private static void assertSample(long elapsedTicks, float expectedMorphAge, float expectedAnimationAge) {
		IchorianSigilPreviewCycle.Sample sample = IchorianSigilPreviewCycle.sample(elapsedTicks);
		assertEquals(expectedMorphAge, sample.morphAgeTicks(), 0.0001F);
		assertEquals(expectedAnimationAge, sample.animationAgeTicks(), 0.0001F);
	}
}
