package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintControllerRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphSpec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The telegraph pass's timing (Kit API section 18): reveal window, the reveal floor, the commit flash, hazard fades. */
class SaintTelegraphTimingTest {
	private static final BossMoveTimeline AREA = new BossMoveTimeline(30, 10, 20);

	@Test
	void aFootprintShowsFromItsRevealToTheCommitAndThroughTheActiveSpanWhenAsked() {
		assertFalse(SaintTelegraphTiming.visible(9.9F, 10, AREA, true), "before the reveal");
		assertTrue(SaintTelegraphTiming.visible(10.0F, 10, AREA, false));
		assertTrue(SaintTelegraphTiming.visible(29.9F, 10, AREA, false), "until the commit");
		assertFalse(SaintTelegraphTiming.visible(30.0F, 10, AREA, false), "gone at the commit when not shown while active");
		assertTrue(SaintTelegraphTiming.visible(39.9F, 10, AREA, true), "through the active span");
		assertFalse(SaintTelegraphTiming.visible(40.0F, 10, AREA, true), "never in the recovery");
	}

	@Test
	void aDeedThatHidesTheTellStillLeavesTheRevealFloor() {
		TelegraphSpec hidden = new TelegraphSpec(null, null, 29, 0, true);
		int area = SaintControllerRules.revealTick(hidden, AREA, SaintMove.Kind.AREA);
		assertEquals(10, area, "an area footprint shows 20 ticks before its commit");
		assertTrue(SaintTelegraphTiming.visible(area, area, AREA, true));
		int melee = SaintControllerRules.revealTick(hidden, AREA, SaintMove.Kind.MELEE);
		assertEquals(18, melee, "a melee footprint shows 12 ticks before its commit");
	}

	@Test
	void theRevealFirmsTowardTheCommitAndTheFlashFadesOverFourTicks() {
		assertEquals(0.0F, SaintTelegraphTiming.reveal(10.0F, 10, 30), 1.0E-6F);
		assertEquals(0.5F, SaintTelegraphTiming.reveal(20.0F, 10, 30), 1.0E-6F);
		assertEquals(1.0F, SaintTelegraphTiming.reveal(35.0F, 10, 30), 1.0E-6F);
		assertEquals(0.0F, SaintTelegraphTiming.commitFlash(29.0F, 30, 4.0F), 1.0E-6F);
		assertEquals(1.0F, SaintTelegraphTiming.commitFlash(30.0F, 30, 4.0F), 1.0E-6F);
		assertEquals(0.5F, SaintTelegraphTiming.commitFlash(32.0F, 30, 4.0F), 1.0E-6F);
		assertEquals(0.0F, SaintTelegraphTiming.commitFlash(34.0F, 30, 4.0F), 1.0E-6F);
	}

	@Test
	void aHazardStandsThenFadesFromItsExpiryOrItsLife() {
		assertEquals(1.0F, SaintTelegraphTiming.hazardFade(50.0F, 100, -1, false, 0), 1.0E-6F);
		assertEquals(0.5F, SaintTelegraphTiming.hazardFade(105.0F, 100, -1, false, 0), 1.0E-6F, "natural fade over 10");
		assertEquals(0.75F, SaintTelegraphTiming.hazardFade(42.0F, 100, 8, true, 40), 1.0E-6F, "expired at 40 over 8");
		assertEquals(0.0F, SaintTelegraphTiming.hazardFade(60.0F, 100, 8, true, 40), 1.0E-6F);
		assertEquals(0.0F, SaintTelegraphTiming.hazardFade(41.0F, 100, 0, true, 40), 1.0E-6F, "an instant expiry");
	}
}
