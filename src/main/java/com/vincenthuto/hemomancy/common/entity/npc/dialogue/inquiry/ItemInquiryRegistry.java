package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Runtime store for all item-inquiry mappings loaded by {@link ItemInquiryLoader}.
 * <p>
 * The registry is keyed by NPC identifier (e.g. {@code "vicar"}, {@code "alchemist"},
 * {@code "zealot"}, {@code "guardian"}) and then by the item's {@link ResourceLocation}.
 * <p>
 * Entries are populated on server start/reload by {@link ItemInquiryLoader} and
 * cleared before each reload cycle.
 */
public final class ItemInquiryRegistry {

	public static final ItemInquiryRegistry INSTANCE = new ItemInquiryRegistry();

	/** npcId → ( itemId → entry ) */
	private final Map<String, Map<ResourceLocation, ItemInquiryEntry>> data = new HashMap<>();

	private ItemInquiryRegistry() {}

	// ── Called by ItemInquiryLoader ───────────────────────────────────────────

	/** Replaces the registry contents with newly loaded data. */
	public void reload(Map<String, Map<ResourceLocation, ItemInquiryEntry>> freshData) {
		data.clear();
		data.putAll(freshData);
	}

	// ── Public lookup API ─────────────────────────────────────────────────────

	/**
	 * Resolves dialogue lines for the given NPC + item combination.
	 *
	 * @param npcId  NPC type identifier (e.g. {@code "vicar"})
	 * @param itemId registry location of the held item
	 * @param degree player's initiatory degree
	 * @param purity player's Unstained purity (0–100)
	 * @return resolved translation keys, or empty if no entry matches
	 */
	public Optional<List<String>> resolve(String npcId, ResourceLocation itemId, int degree, float purity) {
		Map<ResourceLocation, ItemInquiryEntry> npcMap = data.getOrDefault(npcId, Collections.emptyMap());
		ItemInquiryEntry entry = npcMap.get(itemId);
		if (entry == null) return Optional.empty();
		return entry.resolve(degree, purity);
	}

	/** Returns an unmodifiable view of all entries for debugging/inspection. */
	public Map<String, Map<ResourceLocation, ItemInquiryEntry>> allEntries() {
		return Collections.unmodifiableMap(data);
	}
}
