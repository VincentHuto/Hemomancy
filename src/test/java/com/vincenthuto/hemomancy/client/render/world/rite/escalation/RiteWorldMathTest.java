package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.client.render.world.rite.escalation.RiteWorldMath.Ending;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability.*;
import static org.junit.jupiter.api.Assertions.*;

final class RiteWorldMathTest {
	@Test
	void aFourStepRiteBringsEachLayerOnItsStep() {
		float ring = RiteWorldMath.level(1, 4), seal = RiteWorldMath.level(2, 4), procession = RiteWorldMath.level(3, 4);
		assertEquals(0.0F, RiteWorldMath.arrival(ring, RiteWorldMath.CREEP_FROM, 4), "no creep at the first ring");
		assertEquals(1.0F, RiteWorldMath.arrival(seal, RiteWorldMath.CREEP_FROM, 4), 1e-6F);
		assertEquals(1.0F, RiteWorldMath.woundPresence(seal, 4), 1e-6F, "the wound forms at the seal");
		assertEquals(0.0F, RiteWorldMath.rainLevel(seal, 4));
		assertEquals(0.3F, RiteWorldMath.rainLevel(procession, 4), 1e-6F, "rain arrives already falling");
	}

	@Test
	void anElevenStepRiteBuildsMoreGradually() {
		assertEquals(0.0F, RiteWorldMath.arrival(RiteWorldMath.level(3, 11), RiteWorldMath.CREEP_FROM, 11));
		assertEquals(1.0F, RiteWorldMath.arrival(RiteWorldMath.level(4, 11), RiteWorldMath.CREEP_FROM, 11), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.woundPresence(RiteWorldMath.level(5, 11), 11));
		assertEquals(1.0F, RiteWorldMath.woundPresence(RiteWorldMath.level(6, 11), 11), 1e-6F);
	}

	@Test
	void layersNeverShowOnTheStepBeforeTheirOwn() {
		for (int total = 2; total <= 11; total++) {
			for (int stage = 0; stage <= total; stage++) {
				float level = RiteWorldMath.level(stage, total);
				for (float from : new float[] {RiteWorldMath.CREEP_FROM, RiteWorldMath.WOUND_FROM, RiteWorldMath.RAIN_FROM}) {
					String where = total + " steps, stage " + stage + ", threshold " + from;
					float arrival = RiteWorldMath.arrival(level, from, total);
					if (level < from - 1.0e-4F) assertEquals(0.0F, arrival, where);
					else assertEquals(1.0F, arrival, 1.0e-6F, where);
				}
			}
		}
	}

	@Test
	void apotheosRainWaitsForItsOwnStep() {
		assertEquals(0.0F, RiteWorldMath.rainLevel(RiteWorldMath.level(8, 11), 11), "no rain on the eighth ring");
		assertEquals(0.0F, RiteWorldMath.stormVolume(RiteWorldMath.level(8, 11), 11, 1.0F, false));
		assertEquals(0.0F, RiteWorldMath.overflow(RiteWorldMath.level(8, 11), 11));
		assertEquals(0.3F, RiteWorldMath.rainLevel(RiteWorldMath.level(9, 11), 11), 1e-6F, "rain arrives on its own step");
	}

	@Test
	void viewerWeightFallsOffBeyondTheBoundary() {
		assertEquals(1.0F, RiteWorldMath.viewerWeight(10.0D, 8.0F));
		assertEquals(0.5F, RiteWorldMath.viewerWeight(26.0D, 8.0F), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.viewerWeight(40.0D, 8.0F));
	}

	@Test
	void fogNeverClosesInsideTwentyFourBlocks() {
		float scale = RiteWorldMath.farPlaneScale(1.0F, "storm", RUPTURING);
		assertEquals(0.52F, scale, 1e-6F);
		assertEquals(24.0F, RiteWorldMath.applyFarPlane(30.0F, scale, 1.0F), 1e-6F);
		assertEquals(104.0F, RiteWorldMath.applyFarPlane(200.0F, scale, 1.0F), 1e-4F);
		assertEquals(20.0F, RiteWorldMath.applyFarPlane(20.0F, scale, 1.0F), "fog already closer is left alone");
		assertEquals(200.0F, RiteWorldMath.applyFarPlane(200.0F, scale, 0.0F), 1e-4F);
		assertEquals(0.85F, RiteWorldMath.farPlaneScale(1.0F, "faint", STABLE), 1e-6F);
		assertEquals(0.25F, RiteWorldMath.fogMix("none"), 1e-6F);
	}

	@Test
	void creepReachesFurtherForBiggerRitesAndRecoilsUnderRupture() {
		assertEquals(4.0F, RiteWorldMath.creepReach(3.0F));
		assertEquals(6.0F, RiteWorldMath.creepReach(10.0F), 1e-6F);
		assertEquals(12.0F, RiteWorldMath.creepReach(40.0F));
		assertEquals(0.0F, RiteWorldMath.creepRadius(5.5F, 10.0F, 0.25F, 4, STABLE));
		assertEquals(5.5F + 6.0F * 0.25F, RiteWorldMath.creepRadius(5.5F, 10.0F, RiteWorldMath.level(2, 6), 6, STABLE),
				1e-5F, "the creep arrives a quarter grown");
		assertEquals(11.5F, RiteWorldMath.creepRadius(5.5F, 10.0F, 1.0F, 4, STABLE), 1e-5F);
		assertEquals(5.5F + 6.0F * 0.7F, RiteWorldMath.creepRadius(5.5F, 10.0F, 1.0F, 4, RUPTURING), 1e-5F);
	}

	@Test
	void woundAltitudeStaysUnderTheBuildLimit() {
		assertEquals(100.0D, RiteWorldMath.woundAltitude(64, 10.0F, 320), 1e-9);
		assertEquals(312.0D, RiteWorldMath.woundAltitude(290, 10.0F, 320), 1e-9);
		assertEquals(317.0D, RiteWorldMath.woundAltitude(305, 10.0F, 320), 1e-9,
				"never closer than twelve blocks over the Focus");
		assertEquals(-24.0D, RiteWorldMath.woundAltitude(-60, 10.0F, 320), 1e-9);
	}

	@Test
	void woundGrowsFromItsFormation() {
		assertEquals(8.0F, RiteWorldMath.woundRadius(10.0F, 0.5F), 1e-6F);
		assertEquals(16.0F, RiteWorldMath.woundRadius(10.0F, 1.0F), 1e-6F);
		assertEquals(1.6F, RiteWorldMath.woundRadius(0.0F, 0.5F), 1e-6F, "a tiny footprint still gets a wound");
	}

	@Test
	void rainFollowsSizeStrainAndTheParticleSetting() {
		assertEquals(7, RiteWorldMath.rainPerTick(1.0F, 10.0F, 1.0F, STABLE));
		assertEquals(9, RiteWorldMath.rainPerTick(1.0F, 10.0F, 1.0F, STRAINED));
		assertEquals(30, RiteWorldMath.rainPerTick(1.0F, 100.0F, 1.0F, STABLE));
		assertEquals(4, RiteWorldMath.rainPerTick(1.0F, 10.0F, 0.5F, STABLE));
		assertEquals(0, RiteWorldMath.rainPerTick(1.0F, 10.0F, 0.0F, STABLE));
		assertEquals(0, RiteWorldMath.rainPerTick(0.0F, 10.0F, 1.0F, STABLE));
	}

	@Test
	void braziersGrowLeanAndGutter() {
		assertEquals(1.5F, RiteWorldMath.flameHeight(1.0F, STABLE, 0.0F, 1.0F), 1e-6F);
		assertEquals(1.125F, RiteWorldMath.flameHeight(1.0F, STRAINED, 0.0F, 1.0F), 1e-6F);
		assertEquals(0.225F, RiteWorldMath.flameHeight(1.0F, RUPTURING, 0.0F, 1.0F), 1e-6F, "embers between beats");
		assertEquals(1.5F, RiteWorldMath.flameHeight(1.0F, RUPTURING, 1.0F, 1.0F), 1e-6F, "flaring on the beat");
		assertEquals(20.0F, RiteWorldMath.flameLeanDegrees(1.0F), 1e-6F);
		assertFalse(RiteWorldMath.flameSanguine(0.25F));
		assertTrue(RiteWorldMath.flameSanguine(0.5F));
	}

	@Test
	void soundLayersComeWithTheirLayersAndFallSilentWhenStill() {
		assertEquals(0.0F, RiteWorldMath.droneVolume(0.0F, 1.0F, false));
		assertEquals(0.45F, RiteWorldMath.droneVolume(0.25F, 1.0F, false), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.droneVolume(1.0F, 1.0F, true), "still intervals are silent");
		assertEquals(0.0F, RiteWorldMath.heartbeatVolume(0.25F, 4, 1.0F, false));
		assertEquals(0.9F, RiteWorldMath.heartbeatVolume(0.5F, 4, 1.0F, false), 1e-6F);
		assertEquals(0.6F, RiteWorldMath.stormVolume(0.75F, 4, 1.0F, false), 1e-6F);
		assertEquals(0.75F, RiteWorldMath.strainPitch(RUPTURING), 1e-6F);
	}

	@Test
	void endingsLetGoAtTheirOwnPace() {
		assertEquals(1.0F, RiteWorldMath.groundEnding(Ending.NONE, 50.0F));
		assertEquals(0.5F, RiteWorldMath.groundEnding(Ending.COMPLETED, 30.0F), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.groundEnding(Ending.COLLAPSED, 20.0F));
		assertEquals(0.0F, RiteWorldMath.rainEnding(Ending.COMPLETED, 0.0F), "completion stops the rain at once");
		assertEquals(0.8F, RiteWorldMath.collapseSurge(Ending.COLLAPSED, 10.0F), 1e-6F);
		assertEquals(1.0F, RiteWorldMath.collapseSurge(Ending.COLLAPSED, 80.0F), 1e-6F);
		assertEquals(0.0F, RiteWorldMath.soundEnding(Ending.COLLAPSED, 0.0F), "a collapse cuts the bed");
		assertEquals(0.5F, RiteWorldMath.soundEnding(Ending.COMPLETED, 20.0F), 1e-6F);
		assertEquals(60, RiteWorldMath.endingTicks(Ending.COMPLETED));
		assertEquals(1.0F, RiteWorldMath.woundFlash(Ending.COMPLETED, 0.0F, 0.0F), 1e-6F);
		assertEquals(4, RiteWorldMath.COLLAPSE_STRIKE_TICKS.length);
	}
}
