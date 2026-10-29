package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PossessionRulesTest {
	@Test
	void onlyTheNineVanillaSurrogatesCount() {
		assertEquals(SurrogateForm.BAT, SurrogateForm.fromEntityTypeId("minecraft:bat"));
		assertEquals(SurrogateForm.GOLEM, SurrogateForm.fromEntityTypeId("minecraft:iron_golem"));
		assertEquals(SurrogateForm.ARMOR_STAND, SurrogateForm.fromEntityTypeId("minecraft:armor_stand"));
		assertNull(SurrogateForm.fromEntityTypeId("hemomancy:facsimile_bat"), "a facsimile is never a surrogate");
		assertNull(SurrogateForm.fromEntityTypeId("minecraft:cave_spider"));
		assertEquals(9, SurrogateForm.values().length);
	}

	@Test
	void formsCarryTheirPoolsAndBodies() {
		assertEquals(3, SurrogateForm.BAT.pool());
		assertEquals(14, SurrogateForm.GOLEM.pool());
		assertEquals(10, SurrogateForm.HORSE.pool());
		assertTrue(SurrogateForm.BAT.flies() && SurrogateForm.BLAZE.flies() && !SurrogateForm.WOLF.flies());
		assertTrue(SurrogateForm.DONKEY.keepsInventory() && !SurrogateForm.WOLF.keepsInventory());
		assertTrue(SurrogateForm.ENDERMAN.waterHurts() && SurrogateForm.BLAZE.waterHurts());
		assertTrue(SurrogateForm.ARMOR_STAND.wearsStandGear());
		assertTrue(SurrogateForm.GOLEM.speed() < 0 && SurrogateForm.WOLF.speed() > 0);
	}

	@Test
	void hitsCostConcentrationAndTheShellTakesThemFirst() {
		assertEquals(1f, PossessionRules.hitCost(2f));
		assertEquals(2f, PossessionRules.hitCost(6f));
		PossessionRules.Hit shelled = PossessionRules.absorb(2, 5f, 1f);
		assertEquals(1, shelled.shell());
		assertEquals(5f, shelled.concentration());
		assertFalse(shelled.broken());
		PossessionRules.Hit bare = PossessionRules.absorb(0, 2f, 2f);
		assertEquals(0f, bare.concentration());
		assertTrue(bare.broken());
		assertFalse(PossessionRules.absorb(0, 3f, 1f).broken());
	}

	@Test
	void theRiteRefusesTheBrokenTheBusyAndTheUnsevered() {
		assertTrue(PossessionRules.mayBegin(true, false, false));
		assertFalse(PossessionRules.mayBegin(false, false, false));
		assertFalse(PossessionRules.mayBegin(true, true, false), "Mind Broken is the rite's cooldown");
		assertFalse(PossessionRules.mayBegin(true, false, true));
	}

	@Test
	void concentrationRegeneratesOnlyWithAmbergris() {
		assertEquals(0f, PossessionRules.regenPer100(false));
		assertEquals(1f, PossessionRules.regenPer100(true));
		assertEquals(6000, PossessionRules.MIND_BROKEN_TICKS);
		assertEquals(3, PossessionRules.SHELL_HITS);
	}

	@Test
	void aHitInsideTheInvulnerabilityWindowCostsNothing() {
		assertTrue(PossessionRules.ignoresHit(15, false), "berry bushes and lava tick far faster than hits land");
		assertFalse(PossessionRules.ignoresHit(10, false));
		assertFalse(PossessionRules.ignoresHit(15, true), "a cooldown-bypassing source still lands");
	}

	@Test
	void theGolemCanStillClimbABlock() {
		assertTrue(SurrogateForm.GOLEM.jump() >= -0.1, "a 0.42 jump cut by 40% cannot clear one block");
	}
}
