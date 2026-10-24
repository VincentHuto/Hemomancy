package com.vincenthuto.hemomancy.client.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantIntroClientStateTest {
	private static MycophantIntroClientState at(int kind, int ticks) {
		MycophantIntroClientState state = new MycophantIntroClientState();
		state.start(kind, 7);
		for (int i = 0; i < ticks; i++) state.tick();
		return state;
	}

	@Test
	void wakingOpensItsEyesLyingDownThenRises() {
		MycophantIntroClientState start = at(MycophantIntroClientState.WAKING, 0);
		assertEquals(0.0F, start.lidOpen(0.0F), 1.0E-5F);
		assertEquals(26.0F, start.rollDegrees(0.0F), 1.0E-5F);
		assertTrue(start.pitchDegrees(0.0F) > 0.0F);
		assertEquals(MycophantIntroClientState.LIE, start.bodyStage());
		// a heavy blink: half open, then nearly shut again, before the eyes open for good
		assertTrue(at(MycophantIntroClientState.WAKING, 24).lidOpen(0.0F) > at(MycophantIntroClientState.WAKING, 32).lidOpen(0.0F));
		assertEquals(MycophantIntroClientState.KNEEL, at(MycophantIntroClientState.WAKING, 40).bodyStage());
		MycophantIntroClientState risen = at(MycophantIntroClientState.WAKING, 72);
		assertEquals(1.0F, risen.lidOpen(0.0F), 1.0E-5F);
		assertEquals(0.0F, risen.rollDegrees(0.0F), 1.0E-5F);
		assertEquals(0.0F, risen.pitchDegrees(0.0F), 1.0E-5F);
		assertEquals(MycophantIntroClientState.STAND, risen.bodyStage());
		assertEquals(0.0F, at(MycophantIntroClientState.WAKING, 115).dimAlpha(0.0F), 1.0E-5F);
	}

	@Test
	void fibersPullTautThenSnapOneByOne() {
		MycophantIntroClientState held = at(MycophantIntroClientState.WAKING, 74);
		assertEquals(1.0F, held.fiberAlpha(0.0F), 1.0E-5F);
		assertTrue(held.fiberTension(0.0F) > at(MycophantIntroClientState.WAKING, 20).fiberTension(0.0F));
		for (int f = 0; f < MycophantIntroClientState.FIBERS; f++) assertEquals(0.0F, held.fiberBreak(f, 0.0F), 1.0E-5F);
		// staggered: not every strand parts on the same tick
		boolean staggered = false;
		for (int f = 1; f < MycophantIntroClientState.FIBERS; f++)
			staggered |= MycophantIntroClientState.fiberSnapTick(f) != MycophantIntroClientState.fiberSnapTick(0);
		assertTrue(staggered);
		assertTrue(at(MycophantIntroClientState.WAKING, 80).fiberBreak(0, 0.0F) > 0.0F);
		assertEquals(0.0F, at(MycophantIntroClientState.WAKING, 120).fiberAlpha(0.0F), 1.0E-5F);
		assertTrue(at(MycophantIntroClientState.WAKING, 77).shakeAmplitude(0.0F) > 0.0F);
	}

	@Test
	void captureLacesTheViewPullsThePlayerDownAndClosesTheirEyes() {
		MycophantIntroClientState start = at(MycophantIntroClientState.CAPTURE, 0);
		assertEquals(1.0F, start.lidOpen(0.0F), 1.0E-5F);
		assertEquals(0.0F, start.fiberGrowth(0, 0.0F), 1.0E-5F);
		assertTrue(start.inputBound());
		MycophantIntroClientState taken = at(MycophantIntroClientState.CAPTURE, MycophantIntroClientState.CAPTURE_TICKS);
		for (int f = 0; f < MycophantIntroClientState.FIBERS; f++) assertEquals(1.0F, taken.fiberGrowth(f, 0.0F), 1.0E-5F);
		assertEquals(0.0F, taken.lidOpen(0.0F), 1.0E-5F);
		assertEquals(MycophantIntroClientState.LIE, taken.bodyStage());
		assertTrue(taken.pitchDegrees(0.0F) < 0.0F);
		// the waking picks up from the same dark, lying down
		taken.start(MycophantIntroClientState.WAKING, 7);
		assertEquals(0.0F, taken.lidOpen(0.0F), 1.0E-5F);
		assertEquals(MycophantIntroClientState.LIE, taken.bodyStage());
	}

	@Test
	void inputIsBoundUntilControlReturns() {
		assertTrue(at(MycophantIntroClientState.WAKING, 129).inputBound());
		assertFalse(at(MycophantIntroClientState.WAKING, 130).inputBound());
		assertFalse(at(MycophantIntroClientState.REMATCH, 0).inputBound());
	}

	@Test
	void roarShakes() {
		assertTrue(at(MycophantIntroClientState.WAKING, 155).shakeAmplitude(0.0F) > 1.5F);
		assertTrue(at(MycophantIntroClientState.REMATCH, 25).shakeAmplitude(0.0F) > 1.5F);
		assertEquals(0.0F, at(MycophantIntroClientState.WAKING, 300).shakeAmplitude(0.0F), 1.0E-5F);
	}

	@Test
	void rematchHasNoWakingLook() {
		for (int t = 0; t < 80; t += 5) {
			MycophantIntroClientState rematch = at(MycophantIntroClientState.REMATCH, t);
			assertEquals(1.0F, rematch.lidOpen(0.0F), 1.0E-5F);
			assertEquals(0.0F, rematch.dimAlpha(0.0F), 1.0E-5F);
			assertEquals(0.0F, rematch.fiberAlpha(0.0F), 1.0E-5F);
			assertEquals(0.0F, rematch.rollDegrees(0.0F), 1.0E-5F);
			assertEquals(MycophantIntroClientState.STAND, rematch.bodyStage());
		}
	}

	@Test
	void watchingPulseCreepsInAndOutWithoutTouchingTheScene() {
		MycophantIntroClientState state = new MycophantIntroClientState();
		state.start(MycophantIntroClientState.WATCH, 2);
		for (int i = 0; i < 18; i++) state.tick();
		assertTrue(state.watch(0.0F) > 0.5F);
		assertTrue(state.vignette(0.0F) > 0.5F);
		assertFalse(state.inputBound());
		assertEquals(MycophantIntroClientState.STAND, state.bodyStage());
		for (int i = 0; i < 80; i++) state.tick();
		assertEquals(0.0F, state.watch(0.0F), 1.0E-5F);
		assertFalse(state.isActive());
	}

	@Test
	void bloomVeilFadesAndClearStopsEverything() {
		MycophantIntroClientState state = new MycophantIntroClientState();
		state.start(MycophantIntroClientState.BLOOM, 1);
		assertEquals(1.0F, state.bloomVeil(0.0F), 1.0E-5F);
		for (int i = 0; i < 40; i++) state.tick();
		assertEquals(0.0F, state.bloomVeil(0.0F), 1.0E-5F);
		MycophantIntroClientState waking = at(MycophantIntroClientState.WAKING, 10);
		waking.start(MycophantIntroClientState.CLEAR, 0);
		assertEquals(1.0F, waking.lidOpen(0.0F), 1.0E-5F);
		assertEquals(0.0F, waking.dimAlpha(0.0F), 1.0E-5F);
		assertFalse(waking.inputBound());
		assertFalse(waking.isActive());
		assertEquals(MycophantIntroClientState.STAND, waking.bodyStage());
	}
}
