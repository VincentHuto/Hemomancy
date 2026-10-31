package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

/** Brined Court Track 10: a presence's first sight is recorded once and survives a save. */
class CourtPresenceStateTest {
	@Test
	void aPresenceIsSeenOnceAndTheSightIsSaved() {
		CourtProgress progress = new CourtProgress();
		assertFalse(progress.seenPresence("marimo"));
		assertTrue(progress.markPresenceSeen("marimo"));
		assertFalse(progress.markPresenceSeen("marimo"), "a second sight is not new");
		CourtProgress loaded = new CourtProgress();
		loaded.deserializeNBT(null, progress.serializeNBT(null));
		assertTrue(loaded.seenPresence("marimo"));
		assertFalse(loaded.seenPresence("hydra"));
	}

	@Test
	void aSaveFromBeforeTheTrackHasSeenNothing() {
		CourtProgress loaded = new CourtProgress();
		loaded.deserializeNBT(null, new CompoundTag());
		assertFalse(loaded.seenPresence("marimo"));
		assertFalse(loaded.seenPresence("hydra"));
	}
}
