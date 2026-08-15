package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TraceLayerInvalidationTest {

	@Test
	void stableFramesDoNotRebuildUntilTheObservedRevisionChanges() {
		TraceLayerInvalidation invalidation = new TraceLayerInvalidation();

		assertTrue(invalidation.consume(12L));
		assertFalse(invalidation.consume(12L));
		assertTrue(invalidation.consume(13L));
		assertFalse(invalidation.consume(13L));
	}

	@Test
	void explicitChangesDirtyAnOtherwiseStableRevision() {
		TraceLayerInvalidation invalidation = new TraceLayerInvalidation();
		invalidation.consume(4L);

		invalidation.markDirty();

		assertTrue(invalidation.consume(4L));
		assertFalse(invalidation.consume(4L));
	}
}
