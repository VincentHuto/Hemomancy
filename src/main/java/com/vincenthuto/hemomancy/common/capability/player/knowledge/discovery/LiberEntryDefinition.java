package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.Set;

import com.vincenthuto.hemomancy.common.capability.player.knowledge.DiscoverySource;

import net.minecraft.resources.ResourceLocation;

public record LiberEntryDefinition(ResourceLocation entryId, Set<DiscoverySource> sources) {
}
