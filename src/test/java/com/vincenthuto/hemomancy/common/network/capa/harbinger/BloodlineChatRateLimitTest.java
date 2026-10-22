package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-7: bloodline chat allows a short burst, then drops messages until the window passes. */
class BloodlineChatRateLimitTest {
	@Test
	void allowsTheBurstThenDropsUntilTheWindowPasses() {
		long[] recent = BloodlineChatRateLimit.emptyWindow();
		for (int i = 0; i < BloodlineChatRateLimit.MESSAGES; i++) {
			assertTrue(BloodlineChatRateLimit.allow(recent, 1000L + i), "message " + i + " of the burst");
		}
		assertFalse(BloodlineChatRateLimit.allow(recent, 1003L), "one past the burst is dropped");
		assertFalse(BloodlineChatRateLimit.allow(recent, 1000L + BloodlineChatRateLimit.WINDOW_TICKS - 1),
				"still within the window of the first message");
		assertTrue(BloodlineChatRateLimit.allow(recent, 1000L + BloodlineChatRateLimit.WINDOW_TICKS),
				"the first message has left the window");
		assertFalse(BloodlineChatRateLimit.allow(recent, 1000L + BloodlineChatRateLimit.WINDOW_TICKS),
				"the next slot frees one tick later");
		assertTrue(BloodlineChatRateLimit.allow(recent, 1001L + BloodlineChatRateLimit.WINDOW_TICKS));
	}

	@Test
	void sendersAreLimitedSeparately() {
		BloodlineChatRateLimit.clearSessionState();
		var first = java.util.UUID.randomUUID();
		var second = java.util.UUID.randomUUID();
		for (int i = 0; i < BloodlineChatRateLimit.MESSAGES; i++) {
			assertTrue(BloodlineChatRateLimit.tryAcquire(first, 50L));
		}
		assertFalse(BloodlineChatRateLimit.tryAcquire(first, 50L));
		assertTrue(BloodlineChatRateLimit.tryAcquire(second, 50L), "another sender has their own window");
		BloodlineChatRateLimit.clearSessionState();
	}
}
