package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class PuppeteerTrialRiteRulesTest {
	@Test
	void mediumMustBeAttunedToTheCasterAndControlNoActiveBodies() {
		UUID caster = UUID.randomUUID();
		assertEquals(PuppeteerTrialRiteRules.MediumStatus.READY,
				PuppeteerTrialRiteRules.mediumStatus(caster, caster, false));
		assertEquals(PuppeteerTrialRiteRules.MediumStatus.UNATTUNED,
				PuppeteerTrialRiteRules.mediumStatus(null, caster, false));
		assertEquals(PuppeteerTrialRiteRules.MediumStatus.FOREIGN,
				PuppeteerTrialRiteRules.mediumStatus(UUID.randomUUID(), caster, false));
		assertEquals(PuppeteerTrialRiteRules.MediumStatus.ACTIVE_BODIES,
				PuppeteerTrialRiteRules.mediumStatus(caster, caster, true));
	}

	@Test
	void deathCreditRequiresExactCasterEntityAndSummon() {
		UUID caster = UUID.randomUUID();
		UUID entity = UUID.randomUUID();
		assertTrue(PuppeteerTrialRiteRules.matchesDeath(
				caster, entity, "gorebound_hulk", caster, entity, "gorebound_hulk"));
		assertFalse(PuppeteerTrialRiteRules.matchesDeath(
				caster, entity, "gorebound_hulk", UUID.randomUUID(), entity, "gorebound_hulk"));
		assertFalse(PuppeteerTrialRiteRules.matchesDeath(
				caster, entity, "gorebound_hulk", caster, UUID.randomUUID(), "gorebound_hulk"));
		assertFalse(PuppeteerTrialRiteRules.matchesDeath(
				caster, entity, "gorebound_hulk", caster, entity, "marrow_spitter"));
	}

	@Test
	void missingEntityCollapsesOnlyAfterTheGraceWindow() {
		assertFalse(PuppeteerTrialRiteRules.missingEntityExpired(39));
		assertTrue(PuppeteerTrialRiteRules.missingEntityExpired(40));
	}
}
