package com.vincenthuto.hemomancy.common.capability.player.knowledge;

import java.util.Map;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

public interface ILiberKnowledge {
	boolean unlockEntry(ResourceLocation entryId, DiscoverySource source);

	boolean unlockMemo(ResourceLocation memoId, ResourceLocation entryId);

	boolean recordMemo(ResourceLocation memoId);

	boolean knowsMemo(ResourceLocation memoId);

	boolean hasEntry(ResourceLocation entryId);

	Set<ResourceLocation> getUnlockedEntries();

	Set<ResourceLocation> getKnownMemos();

	Map<ResourceLocation, Set<DiscoverySource>> getEntrySources();

	void setFrom(ILiberKnowledge other);
}
