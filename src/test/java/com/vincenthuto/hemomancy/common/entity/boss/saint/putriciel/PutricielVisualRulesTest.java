package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The look of the body, the ending's beats and the deeds: pure envelopes the server and client share. */
final class PutricielVisualRulesTest {
	@Test
	void theRotShowsNoFireAndNoChar() {
		for (float t = 0; t < 400; t += 7) {
			var look = PutricielVisualRules.look(PutricielStage.ROT, t, -1);
			assertEquals(0.0F, look.ember());
			assertEquals(0.0F, look.charAlpha());
			assertEquals(1.0F, look.growth());
			assertEquals(PutricielVisualRules.HUNCH_ROT, look.hunchDegrees());
		}
	}

	@Test
	void theIgnitionBlackensBetweenTenAndThirty() {
		assertEquals(0.0F, PutricielVisualRules.look(PutricielStage.IGNITING, 9, -1).charAlpha(), 1.0E-4F);
		assertEquals(PutricielVisualRules.CHAR_FULL, PutricielVisualRules.look(PutricielStage.IGNITING, 30, -1).charAlpha(), 1.0E-4F);
		assertEquals(PutricielVisualRules.GROWTH_BURNT, PutricielVisualRules.look(PutricielStage.IGNITING, 30, -1).growth(), 1.0E-4F);
		assertEquals(PutricielVisualRules.HUNCH_FIRE, PutricielVisualRules.look(PutricielStage.BURNING, 0, -1).hunchDegrees());
	}

	@Test
	void theEmberingRegrowsWithAnOvershoot() {
		float peak = 0;
		for (float t = 0; t <= 60; t += 1) peak = Math.max(peak, PutricielVisualRules.look(PutricielStage.EMBERING, t, -1).growth());
		assertTrue(peak > 1.0F && peak <= PutricielVisualRules.REGROW_OVERSHOOT + 0.05F, "overshoot " + peak);
		assertEquals(1.0F, PutricielVisualRules.look(PutricielStage.EMBERING, 60, -1).growth(), 1.0E-3F);
		assertEquals(0.0F, PutricielVisualRules.look(PutricielStage.EMBERING, 60, -1).charAlpha(), 1.0E-3F);
		// the seams go out with the body's embers (revision 2026-11-06, R1.6): still glowing at 15, out by 24
		assertTrue(PutricielVisualRules.look(PutricielStage.EMBERING, 15, -1).ember() > 0.05F);
		assertEquals(0.0F, PutricielVisualRules.look(PutricielStage.EMBERING, PutricielVisualRules.EMBER_FADE, -1).ember(), 1.0E-3F);
	}

	@Test
	void theEndingsBeats() {
		assertEquals(100, PutricielEndingRules.END_TRIAL);
		assertTrue(PutricielEndingRules.SPEC.resumeOnLoad(), "a reloaded ending resumes (resolution B-10)");
		assertEquals(0.9F, PutricielEndingRules.charAlpha(20), 1.0E-3F);
		assertTrue(PutricielEndingRules.ember(35) > .9F);
		assertTrue(PutricielEndingRules.face(35) > .99F, "the man's face, held 20 to 50");
		assertEquals(0.0F, PutricielEndingRules.face(10), 1.0E-3F);
		assertEquals(0.2F, PutricielEndingRules.growth(50), 1.0E-3F);
		assertEquals(0.0F, PutricielEndingRules.sink(70), 1.0E-3F);
		assertEquals(1.0F, PutricielEndingRules.sink(100), 1.0E-3F);
		assertEquals(PutricielEndingRules.ember(35), PutricielVisualRules.look(PutricielStage.BURNING, 0, 35).ember(), 1.0E-6F);
	}

	@Test
	void theTremorShowsOnlyAbove75InTheRot() {
		assertEquals(0.0F, PutricielVisualRules.tremor(0.7F, true));
		assertTrue(PutricielVisualRules.tremor(0.9F, true) > 0.0F);
		assertEquals(0.0F, PutricielVisualRules.tremor(0.9F, false));
	}

	@Test
	void onlyFireBurstsCarryFire() {
		assertTrue(PutricielVisualRules.fireBurst(PutricielVisualRules.FIRE_IMPACT));
		assertTrue(PutricielVisualRules.fireBurst(PutricielVisualRules.ABSOLVE));
		assertFalse(PutricielVisualRules.fireBurst(PutricielVisualRules.ROT_IMPACT));
		assertFalse(PutricielVisualRules.fireBurst(PutricielVisualRules.REST));
		assertEquals(1, PutricielVisualRules.fade(0, 40));
		assertEquals(0, PutricielVisualRules.fade(40, 40));
	}

	@Test
	void theDeedsReadTheMask() {
		int all = SaintDeeds.FORCED | 1 << ChapelRules.Kind.LITANY.ordinal() | 1 << ChapelRules.Kind.DOWNFALL.ordinal()
				| 1 << ChapelRules.Kind.REPOSE.ordinal();
		assertTrue(PutricielDeeds.litany(all) && PutricielDeeds.downfall(all) && PutricielDeeds.repose(all));
		assertFalse(PutricielDeeds.repose(0));
		assertEquals(30, PutricielDeeds.recovery(40, all, true));
		assertEquals(40, PutricielDeeds.recovery(40, all, false));
		assertTrue(PutricielDeeds.communionBreaks(0, false));
		assertFalse(PutricielDeeds.communionBreaks(all, false), "Repose: only a Flammeus hit breaks it");
		assertTrue(PutricielDeeds.communionBreaks(all, true));
	}
}
