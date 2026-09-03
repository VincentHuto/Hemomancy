package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircusPerformerRulesTest {
	@Test
	void performsOnlyForNearbyPlayersWhilePeacefulAndStanding() {
		assertTrue(CircusPerformerRules.shouldPerform(24.0D * 24.0D, false, false));
		assertFalse(CircusPerformerRules.shouldPerform(24.0D * 24.0D + 0.01D, false, false));
		assertFalse(CircusPerformerRules.shouldPerform(1.0D, true, false));
		assertFalse(CircusPerformerRules.shouldPerform(1.0D, false, true));
	}

	@Test
	void troupeWarningPrecedesRetaliation() {
		assertFalse(CircusPerformerRules.warningComplete(19));
		assertTrue(CircusPerformerRules.warningComplete(20));
	}

	@Test
	void distantThreatClearsOnlyAfterTenSeconds() {
		assertFalse(CircusPerformerRules.shouldClearThreat(true, true, 48.0D * 48.0D + 1.0D, 199));
		assertTrue(CircusPerformerRules.shouldClearThreat(true, true, 48.0D * 48.0D + 1.0D, 200));
		assertFalse(CircusPerformerRules.shouldClearThreat(true, true, 4.0D, 400));
		assertTrue(CircusPerformerRules.shouldClearThreat(false, true, 4.0D, 0));
		assertTrue(CircusPerformerRules.shouldClearThreat(true, false, 4.0D, 0));
	}

	@Test
	void ordinaryDamageDownsButAdministrativeDamageKills() {
		assertTrue(CircusPerformerRules.shouldEnterDowned(4.0F, 4.0F, false));
		assertFalse(CircusPerformerRules.shouldEnterDowned(4.0F, 4.0F, true));
		assertFalse(CircusPerformerRules.shouldEnterDowned(4.0F, 3.9F, false));
	}

	@Test
	void vaultRequiresLoadedBorderedSupportedCollisionFreeDestination() {
		assertTrue(CircusPerformerRules.isSafeVault(true, true, true, true));
		assertFalse(CircusPerformerRules.isSafeVault(false, true, true, true));
		assertFalse(CircusPerformerRules.isSafeVault(true, false, true, true));
		assertFalse(CircusPerformerRules.isSafeVault(true, true, false, true));
		assertFalse(CircusPerformerRules.isSafeVault(true, true, true, false));
	}

	@Test
	void fireConeRejectsTargetsBehindOrBeyondFiveBlocks() {
		assertTrue(CircusPerformerRules.insideCone(0.0D, 1.0D, 0.0D, 4.0D));
		assertFalse(CircusPerformerRules.insideCone(0.0D, 1.0D, 0.0D, 5.01D));
		assertFalse(CircusPerformerRules.insideCone(0.0D, 1.0D, 0.0D, -2.0D));
		assertFalse(CircusPerformerRules.insideCone(0.0D, 1.0D, 4.0D, 1.0D));
	}

	@Test
	void attackSummonsBetweenTwoAndFourDolls() {
		assertEquals(2, CircusPerformerRules.dollCount(0));
		assertEquals(3, CircusPerformerRules.dollCount(1));
		assertEquals(4, CircusPerformerRules.dollCount(2));
	}
}
