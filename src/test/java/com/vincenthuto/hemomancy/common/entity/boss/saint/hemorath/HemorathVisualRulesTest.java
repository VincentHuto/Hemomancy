package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathVisualRulesTest {
	@Test
	void theStagesEmergeAtZeroAQuarterAndJustOverHalf() {
		assertEquals(0.6F, HemorathVisualRules.stage(Stage.CORDS, 0.0F), 1.0E-5F, "the cords rest thin");
		assertEquals(0.0F, HemorathVisualRules.stage(Stage.LOBES, 0.25F));
		assertTrue(HemorathVisualRules.stage(Stage.LOBES, 0.27F) > 0.0F);
		assertEquals(0.0F, HemorathVisualRules.stage(Stage.MASS, 0.55F));
		assertTrue(HemorathVisualRules.stage(Stage.MASS, 0.58F) > 0.0F);
		assertEquals(0.0F, HemorathVisualRules.stage(Stage.ARMS, 0.3F), "arm vessels wait for the Filling state");
	}

	@Test
	void aShownStageOnlyScalesBetweenPointEightAndOnePointOneFive() {
		for (Stage stage : new Stage[]{Stage.LOBES, Stage.MASS, Stage.ARMS}) {
			float from = stage == Stage.LOBES ? HemorathVisualRules.LOBES_FROM : stage == Stage.MASS
					? HemorathVisualRules.MASS_FROM : HemorathVisualRules.ARMS_FROM;
			for (float f = from + HemorathVisualRules.BUD; f <= 1.0F; f += 0.01F) {
				float scale = HemorathVisualRules.stage(stage, f);
				assertTrue(scale >= 0.8F - 1.0E-4F && scale <= 1.15F + 1.0E-4F, stage + " at " + f + ": " + scale);
			}
		}
	}

	@Test
	void aPatchOvershootsThenSettles() {
		assertEquals(0.0F, HemorathVisualRules.patch(-1.0F));
		assertEquals(0.0F, HemorathVisualRules.patch(0.0F), 1.0E-5F);
		float peak = 0.0F;
		for (float t = 0; t <= HemorathVisualRules.PATCH_ERUPT; t += 0.25F) peak = Math.max(peak, HemorathVisualRules.patch(t));
		assertEquals(HemorathVisualRules.PATCH_OVERSHOOT, peak, 0.02F);
		assertEquals(1.0F, HemorathVisualRules.patch(HemorathVisualRules.PATCH_ERUPT + 4), 1.0E-5F);
	}

	@Test
	void ribsPartWithTheMeterAndTheBodyWidens() {
		assertEquals(0.0F, HemorathVisualRules.ribSpread(0.1F), 1.0E-5F);
		assertTrue(HemorathVisualRules.ribSpread(0.5F) > 0.05F);
		assertEquals(HemorathVisualRules.RIB_SPREAD, HemorathVisualRules.ribSpread(1.0F), 1.0E-5F);
		assertEquals(0.9F, HemorathVisualRules.width(0.0F), 1.0E-5F);
		assertEquals(1.4F, HemorathVisualRules.width(0.7F), 1.0E-5F);
		assertEquals(2.0F, HemorathVisualRules.RENDER_SCALE, "two texels a unit at 2.0 is 16 px a block");
	}

	@Test
	void theGlowRisesWithTheMeterAndTheFeedingOpenings() {
		assertEquals(0.15F, HemorathVisualRules.glow(0.0F, false), 1.0E-5F);
		assertEquals(0.75F, HemorathVisualRules.glow(1.0F, false), 1.0E-5F);
		assertEquals(0.9F, HemorathVisualRules.glow(0.2F, true), 1.0E-5F);
		assertTrue(HemorathVisualRules.beatRate(1.0F) > HemorathVisualRules.beatRate(0.0F));
		assertTrue(HemorathVisualRules.heartbeat(5.0F, 0.5F) >= 0.0F && HemorathVisualRules.heartbeat(5.0F, 0.5F) <= 1.0F);
	}

	@Test
	void authoredTimeStretchesOnlyTheWindUp() {
		assertEquals(14.0F, HemorathVisualRules.authoredTime(12.0F, 12, 14), 1.0E-5F);
		assertEquals(7.0F, HemorathVisualRules.authoredTime(6.0F, 12, 14), 1.0E-5F);
		assertEquals(20.0F, HemorathVisualRules.authoredTime(18.0F, 12, 14), 1.0E-5F);
		assertEquals(5.0F, HemorathVisualRules.heldTime(5.0F));
		assertEquals(10.0F, HemorathVisualRules.heldTime(15.0F));
		assertEquals(14.0F, HemorathVisualRules.heldTime(22.0F));
	}
}
