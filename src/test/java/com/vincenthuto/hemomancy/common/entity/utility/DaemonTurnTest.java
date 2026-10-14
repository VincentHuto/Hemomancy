package com.vincenthuto.hemomancy.common.entity.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class DaemonTurnTest {
	@Test
	void turningTakesTheShortWayRound() {
		assertEquals(180.0F, DaemonTurn.toward(170.0F, -170.0F, 0.5F), 1e-4F, "+10 across the seam, not -170");
		assertEquals(-180.0F, DaemonTurn.toward(-170.0F, 170.0F, 0.5F), 1e-4F);
	}

	@Test
	void theTurnEasesInAndSettlesOnTheTarget() {
		float body = 0.0F;
		float previousStep = Float.MAX_VALUE;
		for (int tick = 0; tick < 80; tick++) {
			float next = DaemonTurn.toward(body, 90.0F, DaemonTurn.BODY_RATE);
			float step = next - body;
			assertTrue(step <= previousStep + 1e-4F, "it slows as it arrives, never snapping");
			previousStep = step;
			body = next;
		}
		assertEquals(90.0F, body, 0.5F);
	}

	@Test
	void theHeadTurnsAheadOfTheBody() {
		float head = 0.0F;
		float body = 0.0F;
		for (int tick = 0; tick < 6; tick++) {
			head = DaemonTurn.toward(head, 120.0F, DaemonTurn.HEAD_RATE);
			body = DaemonTurn.toward(body, 120.0F, DaemonTurn.BODY_RATE);
		}
		assertTrue(head > body + 20.0F, "the head leads: head " + head + ", body " + body);
	}
}
