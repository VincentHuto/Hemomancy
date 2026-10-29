package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaintEnrageRulesTest {
	@Test
	void onlyAForcedAwakeningEnrages() {
		assertTrue(SaintEnrageRules.enraged("FORCED"));
		assertFalse(SaintEnrageRules.enraged("PROPER"));
		assertFalse(SaintEnrageRules.enraged("NONE"));
	}

	@Test
	void theSaintFightsWithEveryDeedSkipped() {
		List<ChapelRules.Kind> skipped = List.of(ChapelRules.Kind.DOWNFALL, ChapelRules.Kind.REPOSE);
		assertEquals(0.5, SaintEnrageRules.healthBonus(skipped), 1e-9, "skip the Downfall and it fights with its fall");
		assertEquals(0.15 + 0.2, SaintEnrageRules.speedBonus(skipped), 1e-9, "skip the Repose and it never pauses");
		assertEquals(0.3, SaintEnrageRules.damageBonus(), 1e-9);
		assertTrue(SaintEnrageRules.resists(List.of(ChapelRules.Kind.BEHOLDING)));
		assertTrue(SaintEnrageRules.regenerates(List.of(ChapelRules.Kind.LITANY)));
		assertEquals(0.0, SaintEnrageRules.healthBonus(List.of()), 1e-9);
	}

	@Test
	void aForcedFightLeavesOnlyAFragment() {
		assertTrue(SaintEnrageRules.fragmentOnly("FORCED"));
		assertFalse(SaintEnrageRules.fragmentOnly("PROPER"));
		assertEquals(3, SaintEnrageRules.FRAGMENTS_PER_RESIDUUM);
	}

	@Test
	void anApotheosWhoSlipsPastTheDoorStillWakesItEnraged() {
		assertEquals("FORCED", SaintEnrageRules.effectiveMode("NONE", true));
		assertEquals("NONE", SaintEnrageRules.effectiveMode("NONE", false));
		assertEquals("PROPER", SaintEnrageRules.effectiveMode("PROPER", true));
	}

	@Test
	void onlyARealDoorRecordIsWrittenBackToTheTomb() {
		assertEquals("FORCED", SaintEnrageRules.recordToKeep("FORCED"));
		assertEquals("PROPER", SaintEnrageRules.recordToKeep("PROPER"));
		assertEquals(null, SaintEnrageRules.recordToKeep("NONE"),
				"an Apotheos wake of an unopened tomb must not be remembered as a shattered door");
	}
}
