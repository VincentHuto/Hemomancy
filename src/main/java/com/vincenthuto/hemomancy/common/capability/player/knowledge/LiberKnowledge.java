package com.vincenthuto.hemomancy.common.capability.player.knowledge;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.vincenthuto.hutoslib.common.book.knowledge.BookKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;

import net.minecraft.resources.ResourceLocation;

/**
 * Hemomancy's book-knowledge implementation. Extends {@link BookKnowledge} for
 * all NBT persistence and generic logic; overrides the two protected hooks so
 * discovery sources round-trip correctly through NBT.
 */
public class LiberKnowledge extends BookKnowledge implements ILiberKnowledge {

	// ── BookKnowledge overrides ───────────────────────────────────────────────

	/**
	 * Re-hydrates a persisted source name as a {@link DiscoverySource}.
	 * Falls back to empty (skipped) for unknown names, which covers forward-
	 * compat (new server sending a source the client doesn't know yet).
	 */
	@Override
	protected Optional<IDiscoverySource> lookupSource(String name) {
		try {
			return Optional.of(DiscoverySource.valueOf(name));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	/**
	 * Returns a mutable {@link LinkedHashSet} for source storage, preserving
	 * insertion order (useful for deterministic NBT serialization).
	 */
	@Override
	protected Set<IDiscoverySource> newSourceSet() {
		return new LinkedHashSet<>();
	}

	// ── Hemomancy-specific overrides ─────────────────────────────────────────

	/**
	 * Records {@code memoId} and simultaneously unlocks the associated
	 * {@code entryId} with a {@link DiscoverySource#MEMO} source tag.
	 */
	@Override
	public boolean unlockMemo(ResourceLocation memoId, ResourceLocation entryId) {
		boolean changed = memoId != null && recordMemo(memoId);
		return unlockEntry(entryId, DiscoverySource.MEMO) || changed;
	}
}
