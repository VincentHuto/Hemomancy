package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Kinbud's Cultivated Mending queue and the Kindled cap (track 02, "Queued regrowth"). */
final class MorphlingRegrowthQueueTest {
	private static final float EPSILON = 1.0E-4F;

	@Test
	void aShareOfANonLethalHitRegrowsEvenlyOverTenSeconds() {
		MorphlingRegrowthQueue queue = new MorphlingRegrowthQueue();
		assertEquals(2.0F, queue.queueHit(10.0F, 0.2D, false, 0L, 200), EPSILON);
		assertEquals(0.0F, queue.regrow(0L), EPSILON, "nothing regrows early");
		assertEquals(1.0F, queue.regrow(100L), EPSILON);
		assertEquals(1.0F, queue.regrow(200L), EPSILON);
		assertEquals(0.0F, queue.regrow(300L), EPSILON);
		assertTrue(queue.isEmpty());
	}

	@Test
	void lethalHitsQueueNothing() {
		MorphlingRegrowthQueue queue = new MorphlingRegrowthQueue();
		assertEquals(0.0F, queue.queueHit(30.0F, 0.35D, true, 0L, 200), EPSILON);
		assertEquals(0.0F, queue.pending(), EPSILON);
	}

	@Test
	void kindledQueuesWholeHitsUpToItsCap() {
		MorphlingRegrowthQueue queue = new MorphlingRegrowthQueue();
		queue.kindle(400L, KinbudRules.kindledCap(true));
		assertEquals(12.0F, queue.queueHit(12.0F, 0.2D, false, 10L, 200), EPSILON);
		assertEquals(8.0F, queue.queueHit(12.0F, 0.2D, false, 20L, 200), EPSILON, "capped at 20 total at home");
		assertEquals(0.0F, queue.queueHit(5.0F, 0.2D, false, 30L, 200), EPSILON);
		assertEquals(2.0F, queue.queueHit(10.0F, 0.2D, false, 400L, 200), EPSILON, "the window has closed");
		assertEquals(10.0F, KinbudRules.kindledCap(false), EPSILON);
	}

	@Test
	void overlappingWoundsRegrowIndependently() {
		MorphlingRegrowthQueue queue = new MorphlingRegrowthQueue();
		queue.queue(2.0F, 0L, 200);
		queue.queue(4.0F, 100L, 200);
		assertEquals(1.0F, queue.regrow(100L), EPSILON);
		assertEquals(1.0F + 2.0F, queue.regrow(200L), EPSILON);
		assertEquals(2.0F, queue.regrow(300L), EPSILON);
		assertEquals(0.0F, queue.pending(), EPSILON);
	}

	@Test
	void mendingShareFollowsStageHomeAndCap() {
		assertEquals(0.10D, KinbudRules.mendingShare(0, false), 1.0E-9D);
		assertEquals(0.35D, KinbudRules.mendingShare(5, false), 1.0E-9D);
		assertEquals(0.30D, KinbudRules.mendingShare(2, true), 1.0E-9D);
		assertEquals(0.50D, KinbudRules.mendingShare(5, true), 1.0E-9D, "never past half");
	}
}
