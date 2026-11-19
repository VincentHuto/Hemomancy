package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.FinalePhase;

class FinaleRulesTest {
	@Test
	void phasesFollowHealthAndTime() {
		assertEquals(FinalePhase.RAFTERS, FinaleRules.next(FinalePhase.CROSSING, 60, 1.0F));
		assertEquals(FinalePhase.CROSSING, FinaleRules.next(FinalePhase.CROSSING, 59, 1.0F));
		assertEquals(FinalePhase.RAFTERS, FinaleRules.next(FinalePhase.RAFTERS, 500, 0.51F));
		assertEquals(FinalePhase.REVEAL, FinaleRules.next(FinalePhase.RAFTERS, 500, 0.5F));
		assertEquals(FinalePhase.REVEAL, FinaleRules.next(FinalePhase.REVEAL, 49, 0.4F));
		assertEquals(FinalePhase.DESCENT, FinaleRules.next(FinalePhase.REVEAL, 50, 0.4F));
		assertEquals(FinalePhase.KNEEL, FinaleRules.next(FinalePhase.KNEEL, 900, 0.0F));
	}

	@Test
	void veilOpensStrongAndThinsAsHeIsPushed() {
		// The copy passes for the real tent through the crossing; the veil is gone once it ends.
		assertEquals(0.0F, FinaleRules.veilStrength(FinalePhase.CROSSING, 10, 1.0F), 1.0E-4F);
		assertEquals(0.0F, FinaleRules.veilStrength(FinalePhase.ENDED, 0, 0.0F), 1.0E-4F);
		// It rises over the opening to very strong at full health.
		assertEquals(0.0F, FinaleRules.veilStrength(FinalePhase.RAFTERS, 0, 1.0F), 1.0E-4F);
		float rising = -1.0F;
		for (int tick = 0; tick <= 100; tick += 5) {
			float veil = FinaleRules.veilStrength(FinalePhase.RAFTERS, tick, 1.0F);
			assertTrue(veil >= rising - 1.0E-4F, "the veil rises over the opening: " + tick);
			rising = veil;
		}
		assertEquals(0.95F, FinaleRules.veilStrength(FinalePhase.RAFTERS, 100, 1.0F), 1.0E-4F);
		// Then it only weakens as he is pushed: about 0.55 as he comes down, about 0.25 at the end, never gone.
		assertEquals(0.55F, FinaleRules.veilStrength(FinalePhase.RAFTERS, 400, 0.5F), 1.0E-4F);
		assertEquals(0.55F, FinaleRules.veilStrength(FinalePhase.REVEAL, 10, 0.5F), 1.0E-4F);
		assertEquals(0.25F, FinaleRules.veilStrength(FinalePhase.DESCENT, 400, 0.0F), 1.0E-4F);
		assertEquals(0.25F, FinaleRules.veilStrength(FinalePhase.KNEEL, 0, 0.0F), 1.0E-4F);
		float last = 2.0F;
		for (int step = 0; step <= 100; step++) {
			float health = 1.0F - step / 100.0F;
			FinalePhase phase = health > FinaleRules.DESCENT_AT ? FinalePhase.RAFTERS : FinalePhase.DESCENT;
			float veil = FinaleRules.veilStrength(phase, 400, health);
			assertTrue(veil <= last + 1.0E-4F, "the veil only thins as he is pushed: " + health);
			assertTrue(veil > 0.0F, "his Chamber never leaves the tent mid-fight: " + health);
			last = veil;
		}
	}

	@Test
	void kneelCatchesAnyLethalHit() {
		for (FinalePhase phase : new FinalePhase[]{FinalePhase.RAFTERS, FinalePhase.REVEAL, FinalePhase.DESCENT})
			assertTrue(FinaleRules.kneelCatches(phase, 20.0F, 5.0F), phase.name());
		assertFalse(FinaleRules.kneelCatches(FinalePhase.DESCENT, 4.0F, 5.0F));
		assertFalse(FinaleRules.kneelCatches(FinalePhase.KNEEL, 20.0F, 5.0F));
	}

	@Test
	void onlyTheOwnersBlowAfterTheSettleKills() {
		assertFalse(FinaleRules.mayKill(FinalePhase.KNEEL, 29, true));
		assertFalse(FinaleRules.mayKill(FinalePhase.KNEEL, 30, false));
		assertTrue(FinaleRules.mayKill(FinalePhase.KNEEL, 30, true));
		assertFalse(FinaleRules.mayKill(FinalePhase.DESCENT, 300, true));
	}

	@Test
	void chargeCapsAndEmpowers() {
		assertEquals(100, FinaleRules.charge(90, 1));
		assertEquals(40, FinaleRules.charge(0, 2));
		assertFalse(FinaleRules.mayEmpower(49));
		assertTrue(FinaleRules.mayEmpower(50));
	}

	/** F4: at the kneel only the owner's own blow kills; an arrow in flight or a summon of theirs does not. */
	@Test
	void onlyTheOwnersOwnBlowKillsAtTheKneel() {
		assertTrue(FinaleRules.ownBlow(true, true), "the owner's melee or own cast");
		assertFalse(FinaleRules.ownBlow(true, false), "the owner's projectile or summon (caused by them, dealt by another)");
		assertFalse(FinaleRules.ownBlow(false, false), "anyone else");
		assertFalse(FinaleRules.ownBlow(false, true), "a blow dealt by the owner for someone else");
	}
}
