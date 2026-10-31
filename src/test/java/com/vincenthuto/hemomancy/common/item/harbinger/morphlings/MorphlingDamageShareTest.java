package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Breakwater and Colony arithmetic: shares, floors and blood (track 02, "Damage redirection"). */
final class MorphlingDamageShareTest {
	private static final float EPSILON = 1.0E-5F;

	@Test
	void breakwaterMovesAQuarterAtTwoMillilitresPerPoint() {
		assertEquals(2.5F, MorphlingDamageShareRules.breakwater(10.0F, 0.25F, 20.0F, 20.0F, 5000.0D, 2.0D), EPSILON);
	}

	@Test
	void breakwaterStopsAtThirtyPercentHostHealth() {
		assertEquals(1.0F, MorphlingDamageShareRules.breakwater(10.0F, 0.25F, 7.0F, 20.0F, 5000.0D, 2.0D), EPSILON);
		assertEquals(0.0F, MorphlingDamageShareRules.breakwater(10.0F, 0.25F, 6.0F, 20.0F, 5000.0D, 2.0D), EPSILON);
	}

	@Test
	void breakwaterIsBoundedByTheBloodToPayForIt() {
		assertEquals(1.5F, MorphlingDamageShareRules.breakwater(10.0F, 0.25F, 20.0F, 20.0F, 3.0D, 2.0D), EPSILON);
		assertEquals(0.0F, MorphlingDamageShareRules.breakwater(10.0F, 0.25F, 20.0F, 20.0F, 0.0D, 2.0D), EPSILON);
	}

	@Test
	void colonySpreadsAFifthEvenly() {
		float[] parts = MorphlingDamageShareRules.colony(10.0F, 0.2F, new float[] { 20.0F, 20.0F }, new float[] { 20.0F, 20.0F });
		assertArrayEquals(new float[] { 1.0F, 1.0F }, parts, EPSILON);
		assertEquals(2.0F, MorphlingDamageShareRules.sum(parts), EPSILON);
	}

	@Test
	void colonyNeverTakesAPartnerBelowHalf() {
		float[] parts = MorphlingDamageShareRules.colony(10.0F, 0.2F, new float[] { 10.5F, 9.0F },
				new float[] { 20.0F, 20.0F });
		assertArrayEquals(new float[] { 0.5F, 0.0F }, parts, EPSILON);
	}

	@Test
	void colonyWithNoPartnersSharesNothing() {
		assertEquals(0, MorphlingDamageShareRules.colony(10.0F, 0.2F, new float[0], new float[0]).length);
	}
}
