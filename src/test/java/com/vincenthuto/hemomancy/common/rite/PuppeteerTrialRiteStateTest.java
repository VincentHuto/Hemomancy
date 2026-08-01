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
		rite.updatePuppeteerTrialHealth(7.0F, 28.0F);
		rite.incrementPuppeteerTrialMissingTicks();

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertEquals(CardinalRitePhase.PUPPET_TRIAL, copy.getPhase());
		assertTrue(copy.isPuppeteerTrialManifested());
		assertFalse(copy.isPuppeteerTrialDefeated());
		assertEquals("veinwing_vulture", copy.getPuppeteerTrialSummonName());
		assertEquals(entity, copy.getPuppeteerTrialEntityId());
		assertEquals(crossbar, copy.getPuppeteerTrialCrossbarId());
		assertEquals(0.75F, copy.getPuppeteerTrialProgress(), 0.0001F);
		assertEquals(1, copy.getPuppeteerTrialMissingTicks());
	}

	@Test
	void matchingDefeatMovesTheTrialIntoCulminationExactlyOnce() {
		UUID entity = UUID.randomUUID();
		ActiveCardinalRite rite = ActiveCardinalRite.puppeteerTrial(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/puppeteer_trial_marrow_spitter"), 5);
		rite.beginPuppeteerTrial("marrow_spitter", entity, UUID.randomUUID());

		assertTrue(rite.markPuppeteerTrialDefeated(entity));
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		assertTrue(rite.isPuppeteerTrialDefeated());
		assertFalse(rite.markPuppeteerTrialDefeated(entity));
		assertFalse(rite.markPuppeteerTrialDefeated(UUID.randomUUID()));
	}
}
