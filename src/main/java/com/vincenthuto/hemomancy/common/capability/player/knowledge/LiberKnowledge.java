package com.vincenthuto.hemomancy.common.capability.player.knowledge;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class LiberKnowledge implements ILiberKnowledge, INBTSerializable<CompoundTag> {
	private static final String TAG_ENTRIES = "UnlockedLiberEntries";
	private static final String TAG_MEMOS = "KnownMemos";
	private static final String TAG_SOURCES = "EntrySources";
	private static final int TAG_STRING = 8;

	private final Set<ResourceLocation> unlockedEntries = new LinkedHashSet<>();
	private final Set<ResourceLocation> knownMemos = new LinkedHashSet<>();
	private final Map<ResourceLocation, Set<DiscoverySource>> entrySources = new LinkedHashMap<>();

	@Override
	public boolean unlockEntry(ResourceLocation entryId, DiscoverySource source) {
		if (entryId == null) {
			return false;
		}
		boolean changed = unlockedEntries.add(entryId);
		entrySources.computeIfAbsent(entryId, id -> EnumSet.noneOf(DiscoverySource.class)).add(source);
		return changed;
	}

	@Override
	public boolean unlockMemo(ResourceLocation memoId, ResourceLocation entryId) {
		boolean changed = knownMemos.add(memoId);
		return unlockEntry(entryId, DiscoverySource.MEMO) || changed;
	}

	@Override
	public boolean recordMemo(ResourceLocation memoId) {
		return memoId != null && knownMemos.add(memoId);
	}

	@Override
	public boolean knowsMemo(ResourceLocation memoId) {
		return knownMemos.contains(memoId);
	}

	@Override
	public boolean hasEntry(ResourceLocation entryId) {
		return unlockedEntries.contains(entryId);
	}

	@Override
	public Set<ResourceLocation> getUnlockedEntries() {
		return Collections.unmodifiableSet(unlockedEntries);
	}

	@Override
	public Set<ResourceLocation> getKnownMemos() {
		return Collections.unmodifiableSet(knownMemos);
	}

	@Override
	public Map<ResourceLocation, Set<DiscoverySource>> getEntrySources() {
		return Collections.unmodifiableMap(entrySources);
	}

	@Override
	public void setFrom(ILiberKnowledge other) {
		unlockedEntries.clear();
		knownMemos.clear();
		entrySources.clear();
		unlockedEntries.addAll(other.getUnlockedEntries());
		knownMemos.addAll(other.getKnownMemos());
		other.getEntrySources().forEach((entry, sources) ->
				entrySources.put(entry, sources.isEmpty()
						? EnumSet.noneOf(DiscoverySource.class)
						: EnumSet.copyOf(sources)));
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.put(TAG_ENTRIES, writeResourceLocationList(unlockedEntries));
		tag.put(TAG_MEMOS, writeResourceLocationList(knownMemos));

		CompoundTag sourcesTag = new CompoundTag();
		entrySources.forEach((entry, sources) -> {
			ListTag sourceList = new ListTag();
			for (DiscoverySource source : sources) {
				sourceList.add(StringTag.valueOf(source.name()));
			}
			sourcesTag.put(entry.toString(), sourceList);
		});
		tag.put(TAG_SOURCES, sourcesTag);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		unlockedEntries.clear();
		knownMemos.clear();
		entrySources.clear();

		readResourceLocationList(tag.getList(TAG_ENTRIES, TAG_STRING), unlockedEntries);
		readResourceLocationList(tag.getList(TAG_MEMOS, TAG_STRING), knownMemos);

		CompoundTag sourcesTag = tag.getCompound(TAG_SOURCES);
		for (String entryKey : sourcesTag.getAllKeys()) {
			ResourceLocation entryId = ResourceLocation.tryParse(entryKey);
			if (entryId == null) {
				continue;
			}
			Set<DiscoverySource> sources = EnumSet.noneOf(DiscoverySource.class);
			ListTag sourceList = sourcesTag.getList(entryKey, TAG_STRING);
			for (int i = 0; i < sourceList.size(); i++) {
				try {
					sources.add(DiscoverySource.valueOf(sourceList.getString(i)));
				} catch (IllegalArgumentException ignored) {
				}
			}
			entrySources.put(entryId, sources);
		}
	}

	private static ListTag writeResourceLocationList(Set<ResourceLocation> values) {
		ListTag list = new ListTag();
		for (ResourceLocation value : values) {
			list.add(StringTag.valueOf(value.toString()));
		}
		return list;
	}

	private static void readResourceLocationList(ListTag list, Set<ResourceLocation> target) {
		for (int i = 0; i < list.size(); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null) {
				target.add(id);
			}
		}
	}
}
