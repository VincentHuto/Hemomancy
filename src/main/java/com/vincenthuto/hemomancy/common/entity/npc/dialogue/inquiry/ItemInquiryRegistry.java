
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Runtime store for datapack-backed inventory inquiry mappings. */
public final class ItemInquiryRegistry {
    public static final ItemInquiryRegistry INSTANCE = new ItemInquiryRegistry();
    private final Map<String, Map<ResourceLocation, ItemInquiryEntry>> data = new HashMap<>();

    private ItemInquiryRegistry() {}

    public void reload(Map<String, Map<ResourceLocation, ItemInquiryEntry>> freshData) {
        data.clear();
        data.putAll(freshData);
    }

    public Optional<List<String>> resolve(String npcId, ResourceLocation itemId, ItemInquiryContext context) {
        ItemInquiryEntry entry = data.getOrDefault(npcId, Collections.emptyMap()).get(itemId);
        return entry == null ? Optional.empty() : entry.resolve(context);
    }

    public Optional<List<String>> resolve(String npcId, ResourceLocation itemId, int degree, float purity) {
        return resolve(npcId, itemId, ItemInquiryContext.legacy(degree, purity));
    }

    public Map<String, Map<ResourceLocation, ItemInquiryEntry>> allEntries() {
        return Collections.unmodifiableMap(data);
    }
}
