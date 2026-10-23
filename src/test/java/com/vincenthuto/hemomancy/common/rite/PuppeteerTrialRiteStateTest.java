package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class PuppeteerTrialRiteStateTest {
	@Test
	void puppeteerTrialStateRoundTripsWithoutLosingIdentityOrProgress() {
		UUID caster = UUID.randomUUID();
		UUID entity = UUID.randomUUID();
		UUID crossbar = UUID.randomUUID();
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(caster, BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/puppeteer_trial_veinwing_vulture"), 5);

		rite.beginPuppeteerTrial("veinwing_vulture", entity, crossbar);
		rite.puppeteerTrial().updateHealth(7.0F, 28.0F);
		rite.puppeteerTrial().incrementMissingTicks();

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertEquals(CardinalRitePhase.PUPPET_TRIAL, copy.getPhase());
		assertTrue(copy.puppeteerTrial().isManifested());
		assertFalse(copy.puppeteerTrial().isDefeated());
		assertEquals("veinwing_vulture", copy.puppeteerTrial().summonName());
		assertEquals(entity, copy.puppeteerTrial().entityId());
		assertEquals(crossbar, copy.puppeteerTrial().crossbarId());
		assertEquals(0.75F, copy.puppeteerTrial().progress(), 0.0001F);
		assertEquals(1, copy.puppeteerTrial().missingTicks());
	}

	@Test
	void matchingDefeatMovesTheTrialIntoCulminationExactlyOnce() {
		UUID entity = UUID.randomUUID();
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/puppeteer_trial_marrow_spitter"), 5);
		rite.beginPuppeteerTrial("marrow_spitter", entity, UUID.randomUUID());

		assertTrue(rite.markPuppeteerTrialDefeated(entity));
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		assertTrue(rite.puppeteerTrial().isDefeated());
		assertFalse(rite.markPuppeteerTrialDefeated(entity));
		assertFalse(rite.markPuppeteerTrialDefeated(UUID.randomUUID()));
	}
}
