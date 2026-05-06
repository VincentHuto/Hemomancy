package com.vincenthuto.hemomancy.common.capability.player.knowledge;

import com.vincenthuto.hutoslib.common.book.knowledge.BookKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Hemomancy's book-knowledge implementation. Extends {@link BookKnowledge} for
 * all NBT persistence and generic logic; overrides the two protected hooks so
 * discovery sources round-trip correctly through NBT.
 */
public class LiberKnowledge extends BookKnowledge {

	// ── BookKnowledge overrides ───────────────────────────────────────────────

	/**
	 * Re-hydrates a persisted source name as an {@link IDiscoverySource}.
	 * Tries {@link HemomancyDiscoverySource} first (Hemomancy-specific), then
	 * {@link CommonDiscoverySource} (shared HutosLib sources). Falls back to
	 * empty (skipped) for unknown names, covering forward-compat scenarios.
	 */
	@Override
	protected Optional<IDiscoverySource> lookupSource(String name) {
		try {
			return Optional.of(HemomancyDiscoverySource.valueOf(name));
		} catch (IllegalArgumentException ignored) {
		}
		try {
			return Optional.of(CommonDiscoverySource.valueOf(name));
		} catch (IllegalArgumentException ignored) {
		}
		return Optional.empty();
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
	 * {@code entryId} with a {@link HemomancyDiscoverySource#MEMO} source tag.
	 */
	@Override
	public boolean unlockMemo(ResourceLocation memoId, ResourceLocation entryId) {
		boolean changed = memoId != null && recordMemo(memoId);
		return unlockEntry(entryId, HemomancyDiscoverySource.MEMO) || changed;
	}
}
