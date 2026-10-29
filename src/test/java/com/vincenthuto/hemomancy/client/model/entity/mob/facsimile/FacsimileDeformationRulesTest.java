package com.vincenthuto.hemomancy.client.model.entity.mob.facsimile;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacsimileDeformationRulesTest {
	@Test
	void theSameBodyIsAlwaysWrongTheSameWay() {
		long seed = FacsimileDeformationRules.seedOf(UUID.fromString("5f0e1c2a-0000-4000-8000-00000000abcd"));
		assertArrayEquals(FacsimileDeformationRules.scaleFor(seed, 3), FacsimileDeformationRules.scaleFor(seed, 3));
	}

	@Test
	void scalesStayRecognisableAndOnlySomePartsAreWrong() {
		long seed = 0x1234_5678_9ABCL;
		int deformed = 0;
		for (int part = 0; part < 200; part++) {
			float[] scale = FacsimileDeformationRules.scaleFor(seed, part);
			for (float axis : scale) assertTrue(axis >= 0.8f && axis <= 1.3f, "axis " + axis);
			if (scale[0] != 1f || scale[1] != 1f || scale[2] != 1f) deformed++;
		}
		assertTrue(deformed >= 50 && deformed <= 110, "deformed parts: " + deformed);
	}

	@Test
	void twoBodiesAreWrongDifferently() {
		boolean differs = false;
		for (int part = 0; part < 10 && !differs; part++) {
			differs = !Arrays.equals(FacsimileDeformationRules.scaleFor(11L, part), FacsimileDeformationRules.scaleFor(97L, part));
		}
		assertTrue(differs);
	}
}
