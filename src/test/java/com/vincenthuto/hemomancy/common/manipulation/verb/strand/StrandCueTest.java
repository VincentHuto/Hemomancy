package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.world.phys.Vec3;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Track 12: the strand cue's count round-trips, and the hand sits on the side of the arm that holds it. */
class StrandCueTest {
	@Test
	void everyFormSchoolArmAndStateRoundTrips() {
		for (StrandForm form : StrandForm.values())
			for (EnumBloodTemper primary : EnumBloodTemper.values())
				for (EnumBloodTemper secondary : new EnumBloodTemper[] {null, EnumBloodTemper.LUX, EnumBloodTemper.TENEBRIS})
					for (boolean left : new boolean[] {false, true})
						for (double param : new double[] {0.0D, 0.37D, 1.0D}) {
							int count = StrandCue.encode(form, primary, secondary, left, param);
							assertTrue(count > 0, "a cue count must be positive");
							StrandCue.Decoded decoded = StrandCue.decode(count);
							assertEquals(form, decoded.form());
							assertEquals(primary, decoded.primary());
							assertEquals(secondary, decoded.secondary());
							assertEquals(left, decoded.leftArm());
							assertEquals(param, decoded.param(), 0.005D);
						}
	}

	@Test
	void everyAccentRoundTrips() {
		for (StrandCue.Accent accent : StrandCue.Accent.values())
			for (EnumBloodTemper school : EnumBloodTemper.values()) {
				StrandCue.DecodedAccent decoded = StrandCue.decodeAccent(
						StrandCue.encodeAccent(accent, school, EnumBloodTemper.MORTEM));
				assertEquals(accent, decoded.accent());
				assertEquals(school, decoded.primary());
				assertEquals(EnumBloodTemper.MORTEM, decoded.secondary());
			}
	}

	@Test
	void theHandSitsOnItsArmsSide() {
		Vec3 eye = new Vec3(0, 1.6, 0), south = new Vec3(0, 0, 1);
		Vec3 right = StrandCue.hand(eye, south, 0.0F, false), left = StrandCue.hand(eye, south, 0.0F, true);
		assertTrue(right.x < 0 && left.x > 0, "facing south, the right hand is to the west");
		assertTrue(right.y < eye.y && right.z > eye.z, "the hand is in front of and below the eye");
	}

	@Test
	void eachFormSetsItsPace() {
		assertEquals(0.8D, StrandForm.LASH.movement, 1.0E-9D);
		assertEquals(0.5D, StrandForm.JET.movement, 1.0E-9D);
		assertEquals(1.0D, StrandForm.SPRAY.movement, 1.0E-9D);
		assertEquals(0.6D, StrandForm.PLANT.movement, 1.0E-9D);
		assertEquals(0.6D, StrandForm.BRAND.movement, 1.0E-9D);
	}

	/** Round two P5: the caster's skill and the twin ride the cue; a cue with neither reads as it always did. */
	@Test
	void theSkillAndTheTwinRoundTripAndLeaveOldCuesAlone() {
		for (StrandForm form : StrandForm.values())
			for (int skill = 0; skill <= 3; skill++)
				for (boolean twin : new boolean[] {false, true}) {
					int count = StrandCue.encode(form, EnumBloodTemper.FERRIC, EnumBloodTemper.DUCTILIS, true, 0.42D, skill, twin);
					assertTrue(count > 0, "a cue count must be positive");
					StrandCue.Decoded decoded = StrandCue.decode(count);
					assertEquals(form, decoded.form());
					assertEquals(EnumBloodTemper.FERRIC, decoded.primary());
					assertEquals(EnumBloodTemper.DUCTILIS, decoded.secondary());
					assertTrue(decoded.leftArm());
					assertEquals(0.42D, decoded.param(), 0.005D);
					assertEquals(skill, decoded.skill());
					assertEquals(twin, decoded.twin());
				}
		int old = StrandCue.encode(StrandForm.LASH, EnumBloodTemper.LUX, null, false, 0.5D);
		assertEquals(old, StrandCue.encode(StrandForm.LASH, EnumBloodTemper.LUX, null, false, 0.5D, 0, false),
				"skill 0 and no twin changed the count");
		assertEquals(0, StrandCue.decode(old).skill());
		assertFalse(StrandCue.decode(old).twin());
		assertEquals(3, StrandCue.decode(StrandCue.encode(StrandForm.BRAND, EnumBloodTemper.LUX, null, false, 0.0D, 9, false)).skill(),
				"a skill past three is clamped, not spilled into the twin bit");
	}
	/** A lash's phase rides on bits of its own: every phase survives the trip beside a full skill and a twin. */
	@Test
	void aLashPhaseRoundTripsAndKeepsTheCountPositive() {
		for (LashRules.Phase phase : LashRules.Phase.values()) {
			int count = StrandCue.encode(StrandForm.LASH, EnumBloodTemper.TENEBRIS, EnumBloodTemper.MORTEM, true, 0.73D, 3, true, phase);
			assertTrue(count > 0, "the cue went negative for " + phase);
			StrandCue.Decoded decoded = StrandCue.decode(count);
			assertEquals(phase, decoded.phase());
			assertEquals(StrandForm.LASH, decoded.form());
			assertEquals(0.73D, decoded.param(), 0.005D);
			assertEquals(3, decoded.skill());
			assertTrue(decoded.twin());
		}
		assertEquals(LashRules.Phase.CHARGING, StrandCue.decode(
				StrandCue.encode(StrandForm.JET, EnumBloodTemper.LUX, null, false, 0.5D)).phase(), "a form with no phase reads as the first");
	}
	/** There are more than eight accents now: the ninth and later ride a fourth bit, and the first eight encode as they did. */
	@Test
	void accentsPastTheEighthRoundTripAndTheFirstEightAreUnchanged() {
		assertTrue(StrandCue.Accent.values().length > 8);
		assertEquals(StrandCue.Accent.JAVELIN, StrandCue.decodeAccent(
				StrandCue.encodeAccent(StrandCue.Accent.JAVELIN, EnumBloodTemper.CONGEATIO, EnumBloodTemper.FERRIC)).accent());
		int crack = StrandCue.encodeAccent(StrandCue.Accent.CRACK, EnumBloodTemper.FLAMMEUS, null);
		assertEquals(1 << 12 | EnumBloodTemper.FLAMMEUS.ordinal() << 3 | StrandCue.Accent.CRACK.ordinal(), crack);
		assertTrue(StrandCue.encodeAccent(StrandCue.Accent.JAVELIN, EnumBloodTemper.CONGEATIO, null) > 0);
	}
}
