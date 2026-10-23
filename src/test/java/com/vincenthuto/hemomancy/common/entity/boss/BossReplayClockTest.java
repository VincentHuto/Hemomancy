package com.vincenthuto.hemomancy.common.entity.boss;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class BossReplayClockTest {
	@Test
	void burstyPacketsStillAdvanceSmoothly() {
		BossReplayClock clock = new BossReplayClock();
		assertEquals(10.0F, clock.sample(1, 10, 100, 0.0F, 1), 1.0E-5F);
		// The next update is late: the synced value is stuck, but the clock keeps time.
		assertEquals(11.5F, clock.sample(1, 10, 101, 0.5F, 1), 1.0E-5F);
		// Two updates land at once: no skip back or forward.
		assertEquals(12.25F, clock.sample(1, 12, 102, 0.25F, 1), 1.0E-5F);
	}

	@Test
	void restartsAndJumpsRebase() {
		BossReplayClock clock = new BossReplayClock();
		clock.sample(1, 30, 100, 0.0F, 1);
		// A new move restarts the counter at once even though the drift is small.
		assertEquals(1.0F, clock.sample(2, 1, 101, 0.0F, 1), 1.0E-5F);
		// The server skipped ahead (a lost target jumping to recovery).
		assertEquals(40.0F, clock.sample(2, 40, 102, 0.0F, 1), 1.0E-5F);
	}

	@Test
	void countdownsStopAtZero() {
		BossReplayClock clock = new BossReplayClock();
		assertEquals(14.0F, clock.sample(0, 14, 50, 0.0F, -1), 1.0E-5F);
		assertEquals(12.5F, clock.sample(0, 13, 51, 0.5F, -1), 1.0E-5F);
		assertEquals(0.0F, clock.sample(0, 0, 70, 0.9F, -1), 1.0E-5F);
	}

	@Test
	void aResetCounterReadsAsResetWhenSampled() {
		BossReplayClock clock = new BossReplayClock();
		clock.sample(1, 20, 100, 0.0F, 1);
		// The server reset the counter to zero under the same key: it reads zero, not zero plus the partial tick.
		assertEquals(0.0F, clock.sample(1, 0, 130, 0.75F, 1), 1.0E-5F);
		// Synced and read apart, the clock reports the replayed value without that rule.
		assertEquals(0.75F, clock.at(130, 0.75F), 1.0E-5F);
	}

	@Test
	void driftWithinToleranceKeepsTheClockAndBeyondItRebases() {
		BossReplayClock clock = new BossReplayClock();
		clock.sync(3, 50, 200, 1);
		// Two ticks of drift are tolerated: the clock keeps its own pace.
		clock.sync(3, 50, 202, 1);
		assertEquals(52.5F, clock.at(202, 0.5F), 1.0E-5F);
		// Three ticks is too far: it jumps to the synced value.
		clock.sync(3, 50, 203, 1);
		assertEquals(50.5F, clock.at(203, 0.5F), 1.0E-5F);
		assertEquals(52.0F, clock.at(205, 0.0F), 1.0E-5F);
	}

	@Test
	void aKeyedClockRunsFromTickZeroUntilItsFirstRebase() {
		// Annetta's clocks: an entity's clock counts from local tick 0 under its idle key, so a small drift on the first
		// sync keeps that count, and only a new key or a large drift re-bases.
		BossReplayClock idle = new BossReplayClock(0);
		assertEquals(7.25F, idle.at(7, 0.25F), 1.0E-5F);
		idle.sync(0, 5, 7, 1);
		assertEquals(7.25F, idle.at(7, 0.25F), 1.0E-5F);
		idle.sync(4, 0, 8, 1);
		assertEquals(0.5F, idle.at(8, 0.5F), 1.0E-5F);

		BossReplayClock fresh = new BossReplayClock();
		fresh.sync(0, 5, 7, 1);
		assertEquals(5.25F, fresh.at(7, 0.25F), 1.0E-5F);
	}

	/** The clocks this one replaced give the same values over jittery, resetting and re-keyed counters. */
	@Test
	void matchesTheVesperAndAnnettaClocksItReplaced() {
		java.util.Random random = new java.util.Random(20261023L);
		for (int run = 0; run < 200; run++) {
			int direction = run % 3 == 0 ? -1 : 1;
			BossReplayClock sampled = new BossReplayClock();
			LegacyVesperClock vesper = new LegacyVesperClock();
			BossReplayClock annettaClock = new BossReplayClock(0);
			LegacyAnnettaClock annetta = new LegacyAnnettaClock();
			int key = 0, synced = random.nextInt(40), localTick = random.nextInt(100);
			for (int step = 0; step < 120; step++) {
				localTick++;
				int roll = random.nextInt(20);
				if (roll == 0) key = random.nextInt(4);
				synced = roll == 1 ? 0 : roll == 2 ? synced + 9 : synced + direction * random.nextInt(3);
				float partial = random.nextFloat();
				assertEquals(vesper.sample(key, synced, localTick, partial, direction),
						sampled.sample(key, synced, localTick, partial, direction), 1.0E-5F);
				if (direction > 0) {
					annetta.tick(key, synced, localTick);
					annettaClock.sync(key, synced, localTick, 1);
					assertEquals(annetta.time(localTick, partial), annettaClock.at(localTick, partial), 1.0E-5F);
				}
			}
		}
	}

	/** {@code VesperAnimClock} as it stood before step 10. */
	private static final class LegacyVesperClock {
		private boolean started;
		private int key, base, baseTick;

		float sample(int key, int synced, int localTick, float partial, int direction) {
			int predicted = base + direction * (localTick - baseTick);
			if (!started || key != this.key || Math.abs(synced - predicted) > 2) {
				started = true;
				this.key = key;
				base = synced;
				baseTick = localTick;
				predicted = synced;
			}
			float value = predicted + direction * partial;
			if (direction < 0) return Math.max(0.0F, value);
			return synced <= 0 && predicted <= 0 ? synced : value;
		}
	}

	/** {@code AnnettaKnowlesEntity}'s action clock as it stood before step 10. */
	private static final class LegacyAnnettaClock {
		private int clientAction;
		private int clientActionStart;

		void tick(int action, int serverTick, int tickCount) {
			if (action != clientAction || Math.abs(tickCount - clientActionStart - serverTick) > 2) {
				clientAction = action;
				clientActionStart = tickCount - serverTick;
			}
		}

		float time(int tickCount, float partial) {
			return tickCount - clientActionStart + partial;
		}
	}

	@Test
	void syncThenReadMatchesSampleForARunningCounter() {
		BossReplayClock split = new BossReplayClock();
		BossReplayClock sampled = new BossReplayClock();
		int[] synced = {4, 5, 5, 7, 8, 8, 8, 12, 13};
		for (int tick = 0; tick < synced.length; tick++) {
			split.sync(2, synced[tick], 40 + tick, 1);
			assertEquals(sampled.sample(2, synced[tick], 40 + tick, 0.3F, 1), split.at(40 + tick, 0.3F), 1.0E-5F);
		}
	}
}
