package com.vincenthuto.hemomancy.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden values for every {@link HemoEasing} function. The inputs are exact binary fractions, so each expected value is
 * exact and compared without tolerance: a changed formula, clamp or argument order fails here.
 */
final class HemoEasingTest {
	@Test
	void clamp01KeepsTheUnitRange() {
		assertEquals(0.0F, HemoEasing.clamp01(-0.5F));
		assertEquals(0.375F, HemoEasing.clamp01(0.375F));
		assertEquals(1.0F, HemoEasing.clamp01(1.5F));
		assertTrue(Float.isNaN(HemoEasing.clamp01(Float.NaN)));
		assertEquals(0.0D, HemoEasing.clamp01(-0.5D));
		assertEquals(0.375D, HemoEasing.clamp01(0.375D));
		assertEquals(1.0D, HemoEasing.clamp01(1.5D));
	}

	@Test
	void smoothstepIsTheClampedCubic() {
		assertEquals(0.0F, HemoEasing.smoothstep(-1.0F));
		assertEquals(0.15625F, HemoEasing.smoothstep(0.25F));
		assertEquals(0.5F, HemoEasing.smoothstep(0.5F));
		assertEquals(0.84375F, HemoEasing.smoothstep(0.75F));
		assertEquals(1.0F, HemoEasing.smoothstep(4.0F));
		assertEquals(0.15625D, HemoEasing.smoothstep(0.25D));
		assertEquals(0.84375D, HemoEasing.smoothstep(0.75D));
		assertEquals(1.0D, HemoEasing.smoothstep(4.0D));
	}

	@Test
	void smoothstepIsNotTheQuintic() {
		// Mth.smoothstep(0.25) is the quintic 0.103515625; the cubic is 0.15625.
		assertEquals(0.103515625F, HemoEasing.smootherstep(0.25F));
		assertEquals(0.5F, HemoEasing.smootherstep(0.5F));
		assertEquals(0.0F, HemoEasing.smootherstep(-1.0F));
		assertEquals(1.0F, HemoEasing.smootherstep(2.0F));
	}

	@Test
	void edgeSmoothstepRemapsAndHardStepsOnEqualEdges() {
		assertEquals(0.15625F, HemoEasing.smoothstep(2.0F, 4.0F, 2.5F));
		assertEquals(0.5F, HemoEasing.smoothstep(2.0F, 4.0F, 3.0F));
		assertEquals(0.15625F, HemoEasing.smoothstep(4.0F, 2.0F, 3.5F), "reversed edges fall instead of rise");
		assertEquals(0.0F, HemoEasing.smoothstep(1.0F, 1.0F, 0.5F));
		assertEquals(1.0F, HemoEasing.smoothstep(1.0F, 1.0F, 1.0F));
		assertEquals(0.15625D, HemoEasing.smoothstep(2.0D, 4.0D, 2.5D));
		assertEquals(0.0D, HemoEasing.smoothstep(1.0D, 1.0D, 0.5D));
		assertEquals(1.0D, HemoEasing.smoothstep(1.0D, 1.0D, 2.0D));
	}

	@Test
	void unclampedSmoothstepOvershootsOutsideTheUnitRange() {
		assertEquals(0.15625F, HemoEasing.smoothstepUnclamped(0.25F));
		assertEquals(-4.0F, HemoEasing.smoothstepUnclamped(2.0F));
		assertEquals(5.0F, HemoEasing.smoothstepUnclamped(-1.0F));
		assertEquals(0.84375D, HemoEasing.smoothstepUnclamped(0.75D));
		assertEquals(-4.0D, HemoEasing.smoothstepUnclamped(2.0D));
	}

	@Test
	void lerpTakesStartEndAmount() {
		assertEquals(10.0F, HemoEasing.lerp(10.0F, 20.0F, 0.0F));
		assertEquals(20.0F, HemoEasing.lerp(10.0F, 20.0F, 1.0F));
		assertEquals(3.0F, HemoEasing.lerp(2.0F, 6.0F, 0.25F));
		assertEquals(5.0F, HemoEasing.lerp(6.0F, 2.0F, 0.25F));
		assertEquals(8.0F, HemoEasing.lerp(2.0F, 6.0F, 1.5F), "the amount is not clamped");
		assertEquals(3.0D, HemoEasing.lerp(2.0D, 6.0D, 0.25D));
		assertEquals(-2.0D, HemoEasing.lerp(2.0D, 6.0D, -1.0D));
	}

	@Test
	void crossfadeWeighsBothEnds() {
		assertEquals(3.0F, HemoEasing.crossfade(2.0F, 6.0F, 0.25F));
		assertEquals(2.0F, HemoEasing.crossfade(2.0F, 6.0F, 0.0F));
		assertEquals(6.0F, HemoEasing.crossfade(2.0F, 6.0F, 1.0F));
		assertEquals(3.0D, HemoEasing.crossfade(2.0D, 6.0D, 0.25D));
		assertEquals(10.0D, HemoEasing.crossfade(2.0D, 6.0D, 2.0D));
	}

	@Test
	void cubicEasesAreClamped() {
		assertEquals(0.875F, HemoEasing.easeOutCubic(0.5F));
		assertEquals(0.0F, HemoEasing.easeOutCubic(-1.0F));
		assertEquals(1.0F, HemoEasing.easeOutCubic(2.0F));
		assertEquals(0.875D, HemoEasing.easeOutCubic(0.5D));
		assertEquals(0.125F, HemoEasing.easeInCubic(0.5F));
		assertEquals(1.0F, HemoEasing.easeInCubic(2.0F));
		assertEquals(0.125D, HemoEasing.easeInCubic(0.5D));
		assertEquals(0.0625D, HemoEasing.easeInOutCubic(0.25D));
		assertEquals(0.5D, HemoEasing.easeInOutCubic(0.5D));
		assertEquals(0.9375D, HemoEasing.easeInOutCubic(0.75D));
		assertEquals(1.0D, HemoEasing.easeInOutCubic(3.0D));
	}

	@Test
	void pulseRisesThenDecaysToNothing() {
		assertEquals(0.0F, HemoEasing.pulse(-1.0F, 2.0F, 10.0F));
		assertEquals(0.5F, HemoEasing.pulse(1.0F, 2.0F, 10.0F));
		assertEquals(1.0F, HemoEasing.pulse(2.0F, 2.0F, 10.0F));
		assertEquals(0.25F, HemoEasing.pulse(6.0F, 2.0F, 10.0F));
		assertEquals(0.0F, HemoEasing.pulse(10.0F, 2.0F, 10.0F));
		assertEquals(1.0F, HemoEasing.pulse(0.0F, 0.0F, 4.0F), "no attack starts at full strength");
	}
}
