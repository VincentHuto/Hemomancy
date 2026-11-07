package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HemorathPresenceRulesTest {
	@Test
	void fullWithinFourAndNothingAtNine() {
		assertEquals(1.0F, HemorathPresenceRules.falloff(0.0D, false));
		assertEquals(1.0F, HemorathPresenceRules.falloff(4.0D, false));
		assertEquals(0.5F, HemorathPresenceRules.falloff(6.5D, false), 1.0E-4F);
		assertEquals(0.0F, HemorathPresenceRules.falloff(9.0D, false));
		assertEquals(9.0D, HemorathPresenceRules.radius(false));
	}

	@Test
	void aPulseEveryFiveTicksTakesTwoAndAHalfATick() {
		assertEquals(5, HemorathPresenceRules.CADENCE);
		assertEquals(12.5F, HemorathPresenceRules.drain(1.0F, false));
		assertEquals(2.5F, HemorathPresenceRules.drain(1.0F, false) / HemorathPresenceRules.CADENCE);
		assertEquals(125.0F, HemorathPresenceRules.intake(100.0F, true), 1.0E-4F);
		assertEquals(100.0F, HemorathPresenceRules.intake(100.0F, false));
	}

	@Test
	void anEmptyPlayerBleedsHealthInsideFourAndAHalfOnceASecond() {
		assertTrue(HemorathPresenceRules.hurtsEmpty(HemorathPresenceRules.falloff(4.5D, false), false));
		assertFalse(HemorathPresenceRules.hurtsEmpty(HemorathPresenceRules.falloff(5.0D, false), false));
		int due = 0;
		for (int tick = 0; tick < 100; tick += HemorathPresenceRules.CADENCE) if (HemorathPresenceRules.emptyTickDue(tick)) due++;
		assertEquals(5, due);
		assertEquals(25.0F, HemorathPresenceRules.METER_PER_HEALTH);
	}

	@Test
	void theLastThirstIsTwoATickFlatToEighteen() {
		assertEquals(18.0D, HemorathPresenceRules.radius(true));
		assertEquals(1.0F, HemorathPresenceRules.falloff(17.0D, true));
		assertEquals(0.0F, HemorathPresenceRules.falloff(18.5D, true));
		assertEquals(2.0F, HemorathPresenceRules.drain(0.3F, true) / HemorathPresenceRules.CADENCE);
	}

	@Test
	void nothingDuringTheRiseTheCardOrTheEnding() {
		assertTrue(HemorathPresenceRules.active(false, false, false));
		assertFalse(HemorathPresenceRules.active(true, false, false));
		assertFalse(HemorathPresenceRules.active(false, true, false));
		assertFalse(HemorathPresenceRules.active(false, false, true));
		assertEquals(40, HemorathPresenceRules.heartbeat(0.0F));
		assertEquals(20, HemorathPresenceRules.heartbeat(1.0F));
	}
}
