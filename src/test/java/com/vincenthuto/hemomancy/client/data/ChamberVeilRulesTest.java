package com.vincenthuto.hemomancy.client.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ChamberVeilRulesTest {
	@Test
	void theHoleStartsWhereTheCrossingLeftItAndSwallowsTheViewWithinASecond() {
		assertEquals(ChamberVeilRules.ARRIVAL_RADIUS, ChamberVeilRules.holeRadius(0L), 1e-6);
		assertTrue(ChamberVeilRules.holeRadius(300L) > ChamberVeilRules.ARRIVAL_RADIUS);
		assertTrue(ChamberVeilRules.holeRadius(300L) < ChamberVeilRules.FULL_RADIUS);
		assertEquals(ChamberVeilRules.FULL_RADIUS, ChamberVeilRules.holeRadius(ChamberVeilRules.SWALLOW_MILLIS), 1e-6);
		assertEquals(ChamberVeilRules.FULL_RADIUS, ChamberVeilRules.holeRadius(60_000L), 1e-6, "and stays swallowed however long the chunk takes");
	}

	@Test
	void anArmedVeilIsSpentOnceAndExpiresUnused() {
		long armed = 1000L;
		assertTrue(ChamberVeilRules.stillArmed(armed, armed + 500L));
		assertTrue(ChamberVeilRules.stillArmed(armed, armed + ChamberVeilRules.ARM_MILLIS));
		assertTrue(!ChamberVeilRules.stillArmed(armed, armed + ChamberVeilRules.ARM_MILLIS + 1L), "a crossing that stepped back out leaves no veil for the next visit");
	}

	@Test
	void aVeilArmedByAWalkThatAlreadySwallowedTheViewStaysFull() {
		assertEquals(ChamberVeilRules.FULL_RADIUS, ChamberVeilRules.holeRadius(0L, ChamberVeilRules.FULL_RADIUS), 1e-6);
		assertEquals(ChamberVeilRules.FULL_RADIUS, ChamberVeilRules.holeRadius(60_000L, ChamberVeilRules.FULL_RADIUS), 1e-6);
		assertTrue(ChamberVeilRules.holeRadius(100L, 1.0F) > 1.0F && ChamberVeilRules.holeRadius(100L, 1.0F) < ChamberVeilRules.FULL_RADIUS,
				"a narrower start still swallows");
	}
}
