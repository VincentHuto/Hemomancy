package com.vincenthuto.hemomancy.client.screen.overlay.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossBarFrameMathTest {
	@Test
	void chipHoldsThenDrains() {
		BossBarFrameClientState.Entry entry = new BossBarFrameClientState.Entry(Hemomancy.rloc("mycophant"), 0);
		assertEquals(1.0F, entry.chip(1.0F, 0), 1.0E-4F);
		assertEquals(1.0F, entry.chip(0.6F, 0), 1.0E-4F);
		assertEquals(1.0F, entry.chip(0.6F, 15), 1.0E-4F);
		assertEquals(1.0F, entry.chip(0.6F, 16), 1.0E-4F);
		assertTrue(entry.chip(0.6F, 17) < 1.0F, "drains once the hold ends");
		assertEquals(0.6F, entry.chip(0.6F, 36), 1.0E-4F);
		assertEquals(0.6F, entry.chip(0.6F, 50), 1.0E-4F, "never below the bar");
	}

	@Test
	void anotherHitRestartsTheHold() {
		BossBarFrameClientState.Entry entry = new BossBarFrameClientState.Entry(Hemomancy.rloc("mycophant"), 0);
		entry.chip(1.0F, 0);
		entry.chip(0.8F, 0);
		entry.chip(0.6F, 10);
		assertEquals(1.0F, entry.chip(0.6F, 26), 1.0E-4F, "the hold runs from the latest hit");
	}

	@Test
	void chipFollowsHealsImmediately() {
		BossBarFrameClientState.Entry entry = new BossBarFrameClientState.Entry(Hemomancy.rloc("mycophant"), 0);
		entry.chip(0.5F, 0);
		assertEquals(0.7F, entry.chip(0.7F, 1), 1.0E-4F);
	}

	@Test
	void revealRunsTwentyTicks() {
		BossBarFrameClientState.Entry entry = new BossBarFrameClientState.Entry(Hemomancy.rloc("mycophant"), 100);
		entry.seen(100);
		assertEquals(0.0F, entry.reveal(100), 1.0E-4F);
		float last = 0.0F;
		for (int t = 101; t <= 120; t++) {
			float reveal = entry.reveal(t);
			assertTrue(reveal >= last, "monotonic");
			last = reveal;
			entry.seen(t);
		}
		assertEquals(1.0F, entry.reveal(120), 1.0E-4F);
	}

	@Test
	void revealRestartsWhenAHiddenBarReturns() {
		BossBarFrameClientState.Entry entry = new BossBarFrameClientState.Entry(Hemomancy.rloc("mycophant"), 0);
		for (int t = 0; t <= 30; t++) entry.seen(t);
		assertEquals(1.0F, entry.reveal(30), 1.0E-4F);
		entry.seen(200);
		assertEquals(0.0F, entry.reveal(200), 1.0E-4F, "a bar hidden and shown again unfurls again");
	}

	@Test
	void bindingSurvivesWhileTheBarIsHidden() {
		BossBarFrameClientState.clear();
		UUID bar = UUID.randomUUID();
		BossBarFrameClientState.bind(bar, Hemomancy.rloc("mycophant"));
		BossBarFrameClientState.prune(BossBarFrameClientState.FORGET_TICKS - 1);
		assertNotNull(BossBarFrameClientState.entry(bar), "hidden bars keep their frame (setVisible re-adds without a new binding)");
		BossBarFrameClientState.prune(BossBarFrameClientState.FORGET_TICKS + 1);
		assertNull(BossBarFrameClientState.entry(bar));
	}
}
