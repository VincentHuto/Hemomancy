package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.Set;

import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;

import net.minecraft.resources.ResourceLocation;

public record LiberEntryDefinition(ResourceLocation entryId, Set<IDiscoverySource> sources) {
}
