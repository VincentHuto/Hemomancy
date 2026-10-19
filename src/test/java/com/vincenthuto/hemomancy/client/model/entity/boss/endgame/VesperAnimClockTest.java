package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperAnimClockTest {
	@Test
	void burstyPacketsStillAdvanceSmoothly() {
		VesperAnimClock clock = new VesperAnimClock();
		assertEquals(10.0F, clock.sample(1, 10, 100, 0.0F, 1), 1.0E-5F);
		// The next update is late: the synced value is stuck, but the clock keeps time.
		assertEquals(11.5F, clock.sample(1, 10, 101, 0.5F, 1), 1.0E-5F);
		// Two updates land at once: no skip back or forward.
		assertEquals(12.25F, clock.sample(1, 12, 102, 0.25F, 1), 1.0E-5F);
	}

	@Test
	void restartsAndJumpsRebase() {
		VesperAnimClock clock = new VesperAnimClock();
		clock.sample(1, 30, 100, 0.0F, 1);
		// A new move restarts the counter at once even though the drift is small.
		assertEquals(1.0F, clock.sample(2, 1, 101, 0.0F, 1), 1.0E-5F);
		// The server skipped ahead (a lost target jumping to recovery).
		assertEquals(40.0F, clock.sample(2, 40, 102, 0.0F, 1), 1.0E-5F);
	}

	@Test
	void countdownsStopAtZero() {
		VesperAnimClock clock = new VesperAnimClock();
		assertEquals(14.0F, clock.sample(0, 14, 50, 0.0F, -1), 1.0E-5F);
		assertEquals(12.5F, clock.sample(0, 13, 51, 0.5F, -1), 1.0E-5F);
		assertEquals(0.0F, clock.sample(0, 0, 70, 0.9F, -1), 1.0E-5F);
	}
}
