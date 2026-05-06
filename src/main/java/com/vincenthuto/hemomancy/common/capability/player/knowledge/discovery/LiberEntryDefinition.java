package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record LiberEntryDefinition(ResourceLocation entryId, Set<IDiscoverySource> sources) {
}
