package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DialogueKnowledge implements INBTSerializable<CompoundTag> {
	private static final String TAG_READ_TOPICS = "ReadTopics";
	private static final int TAG_STRING = 8;
	private final Set<ResourceLocation> readTopics = new LinkedHashSet<>();

	/** The most lore topics one player can have read; far above the authored count, it only bounds a hostile client. */
	public static final int MAX_READ_TOPICS = 2048;

	public boolean markRead(ResourceLocation topicId) {
		if (topicId == null || readTopics.contains(topicId) || readTopics.size() >= MAX_READ_TOPICS) return false;
		return readTopics.add(topicId);
	}

	public boolean hasRead(ResourceLocation topicId) {
		return topicId != null && readTopics.contains(topicId);
	}

	public Set<ResourceLocation> readTopics() {
		return Collections.unmodifiableSet(readTopics);
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		for (ResourceLocation id : readTopics) list.add(StringTag.valueOf(id.toString()));
		tag.put(TAG_READ_TOPICS, list);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		readTopics.clear();
		ListTag list = tag.getList(TAG_READ_TOPICS, TAG_STRING);
		for (int i = 0; i < list.size(); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			if (id != null) readTopics.add(id);
		}
	}
}
