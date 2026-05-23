package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinition;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hutoslib.common.book.knowledge.BookKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.IBookKnowledge;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Hemomancy's book-knowledge implementation. Extends {@link BookKnowledge} for
 * all NBT persistence and generic logic; overrides the two protected hooks so
 * discovery sources round-trip correctly through NBT.
 */
public class LiberKnowledge extends BookKnowledge {
	private static final String TAG_PENDING_MEMOS = "PendingMemos";
	private static final int TAG_STRING = 8;

	private final Set<ResourceLocation> pendingMemos = new LinkedHashSet<>();

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

	@Override
	public boolean recordMemo(ResourceLocation memoId) {
		boolean changed = super.recordMemo(memoId);
		if (memoId != null) {
			pendingMemos.remove(memoId);
		}
		return changed;
	}

	public Set<ResourceLocation> getPendingMemos() {
		return Collections.unmodifiableSet(pendingMemos);
	}

	public boolean recordPendingMemo(ResourceLocation memoId) {
		return memoId != null && !knowsMemo(memoId) && pendingMemos.add(memoId);
	}

	public boolean removePendingMemos(Collection<ResourceLocation> memoIds) {
		boolean changed = false;
		for (ResourceLocation memoId : memoIds) {
			changed |= pendingMemos.remove(memoId);
		}
		return changed;
	}

	public int countPendingMemosByPath(MemoDefinition.MemoPath path) {
		int count = 0;
		for (ResourceLocation memoId : pendingMemos) {
			if (pathForMemo(memoId) == path) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void setFrom(IBookKnowledge other) {
		super.setFrom(other);
		pendingMemos.clear();
		if (other instanceof LiberKnowledge liber) {
			pendingMemos.addAll(liber.getPendingMemos());
		}
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = super.serializeNBT(provider);
		tag.put(TAG_PENDING_MEMOS, writeResourceLocationSet(pendingMemos));
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		super.deserializeNBT(provider, tag);
		pendingMemos.clear();
		readResourceLocationSet(tag.getList(TAG_PENDING_MEMOS, TAG_STRING), pendingMemos);
		pendingMemos.removeIf(this::knowsMemo);
	}

	private static MemoDefinition.MemoPath pathForMemo(ResourceLocation memoId) {
		return MemoDefinitions.get(memoId)
				.map(MemoDefinition::path)
				.orElse(MemoDefinition.MemoPath.SHARED);
	}

	private static ListTag writeResourceLocationSet(Set<ResourceLocation> values) {
		ListTag list = new ListTag();
		for (ResourceLocation value : values) {
			list.add(StringTag.valueOf(value.toString()));
		}
		return list;
	}

	private static void readResourceLocationSet(ListTag list, Set<ResourceLocation> target) {
		for (int i = 0; i < list.size(); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null) {
				target.add(id);
			}
		}
	}
}
