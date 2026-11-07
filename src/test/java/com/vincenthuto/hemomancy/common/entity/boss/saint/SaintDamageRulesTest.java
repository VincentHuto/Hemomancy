package com.vincenthuto.hemomancy.common.entity.boss.saint;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The kit damage pipeline: enrage scaling, factors stacking with the opening, the floor of 1, the floor reached once. */
final class SaintDamageRulesTest {
	@Test
	void theEnrageReachesEveryMove() {
		assertEquals(13.0F, SaintMoves.scaled(10.0F, 11.7D, 9.0D), 1.0E-5F, "+30% attack: 9 x 1.3");
		assertEquals(10.0F, SaintMoves.scaled(10.0F, 9.0D, 9.0D));
		assertEquals(10.0F, SaintMoves.scaled(10.0F, 4.0D, 0.0D), "no attack base: the table's number");
	}

	@Test
	void factorsStackWithTheOpening() {
		assertEquals(0.45F, SaintDamageRules.factor(0.3F, 1.5F), 1.0E-6F, "Velorum: 30% outside, x1.5 in a window");
		assertEquals(1.0F, SaintDamageRules.factor(1.0F, 1.0F));
		assertEquals(0.0F, SaintDamageRules.factor(-1.0F, 2.0F), "never negative");
	}

	@Test
	void theFloorIsNeverBelowOne() {
		assertEquals(1.0F, SaintDamageRules.floor(0.0F));
		assertEquals(60.0F, SaintDamageRules.floor(60.0F));
		assertEquals(60.0F, SaintDamageRules.kept(70.0F, 40.0F, 60.0F), "a hit stops at the floor");
		assertEquals(50.0F, SaintDamageRules.kept(50.0F, 45.0F, 60.0F), "a floor above the health never heals");
		assertEquals(65.0F, SaintDamageRules.kept(70.0F, 65.0F, 60.0F));
		assertEquals(1.0F, SaintDamageRules.kept(5.0F, -20.0F, SaintDamageRules.floor(1.0F)), "a Saint is never killed");
	}

	@Test
	void aFloorIsReachedOnce() {
		assertTrue(SaintDamageRules.reached(60.0F, 60.0F, -1.0F));
		assertFalse(SaintDamageRules.reached(60.0F, 60.0F, 60.0F), "already reached");
		assertTrue(SaintDamageRules.reached(25.0F, 25.0F, 60.0F), "the next floor counts again");
		assertFalse(SaintDamageRules.reached(70.0F, 60.0F, -1.0F));
	}

	@Test
	void hitsAreClassifiedByHowTheyArrived() {
		assertEquals(SaintOpening.Delivery.CHANNEL, SaintDamageRules.delivery(true, true, true, true));
		assertEquals(SaintOpening.Delivery.SCHOOL_CAST, SaintDamageRules.delivery(false, true, true, true));
		assertEquals(SaintOpening.Delivery.PROJECTILE, SaintDamageRules.delivery(false, false, true, true));
		assertEquals(SaintOpening.Delivery.MELEE, SaintDamageRules.delivery(false, false, false, true));
		assertEquals(SaintOpening.Delivery.OTHER, SaintDamageRules.delivery(false, false, false, false));
	}

	@Test
	void deedsReadTheirMask() {
		SaintDeeds proper = SaintDeeds.of("OPENED", java.util.List.of("DOWNFALL"));
		assertFalse(proper.forced());
		assertEquals(0, proper.mask(), "a proper awakening lends nothing");
		SaintDeeds forced = SaintDeeds.of("FORCED", java.util.List.of("DOWNFALL", "REPOSE", "NOT_A_CHAPEL"));
		assertTrue(forced.forced());
		assertTrue(forced.skipped(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.DOWNFALL));
		assertFalse(forced.skipped(com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.LITANY));
		assertTrue(SaintDeeds.ofMask(forced.mask()).skipped(
				com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules.Kind.REPOSE), "the synced mask reads back");
		assertTrue(SaintDeeds.of("FORCED", java.util.List.of()).forced(), "forced with nothing skipped still reads forced");
		assertTrue(SaintDeeds.ofMask(SaintDeeds.of("FORCED", java.util.List.of()).mask()).forced());
	}
}
