package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.knowledge.DiscoverySource;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter;

import net.minecraft.resources.ResourceLocation;

public final class LiberEntryDefinitions {
	public static final String DIALOGUE_EVENT_PREFIX = "liber_unlock:";

	private static final Map<ResourceLocation, LiberEntryDefinition> ENTRIES = new LinkedHashMap<>();
	private static final Map<String, java.util.Set<ResourceLocation>> RITE_UNLOCKS = new LinkedHashMap<>();
	private static final Map<ResourceLocation, java.util.Set<ResourceLocation>> ADVANCEMENT_UNLOCKS = new LinkedHashMap<>();
	private static final Map<ResourceLocation, java.util.Set<ResourceLocation>> ITEM_UNLOCKS = new LinkedHashMap<>();
	private static final Map<String, java.util.Set<ResourceLocation>> DIALOGUE_UNLOCKS = new LinkedHashMap<>();

	public static final ResourceLocation FIRST_RITE_NOTES = entry("the_hematic_order/pages/first_rite_notes");
	public static final ResourceLocation HEMOMANCY = entry("intro/pages/hemomancy");
	public static final ResourceLocation ERYTHROMYCELIUM = entry("intro/pages/erythromycelium");
	public static final ResourceLocation THE_HARBINGERS = entry("intro/pages/the_harbingers");
	public static final ResourceLocation THE_UNSTAINED = entry("intro/pages/the_unstained");
	public static final ResourceLocation DEGREES = entry("the_hematic_order/pages/degrees");
	public static final ResourceLocation HERMITS = entry("the_hematic_order/pages/the_hermit_tradition");
	public static final ResourceLocation ORDER_BELIEFS = entry("the_hematic_order/pages/what_they_believe");
	public static final ResourceLocation HISTORICAL_RECORD = entry("the_hematic_order/pages/historical_record");
	public static final ResourceLocation BLOOD_MEMORIES = entry("the_infection/pages/blood_memories");
	public static final ResourceLocation HYPHAE = entry("the_infection/pages/hyphae");
	public static final ResourceLocation ENTITY = entry("the_infection/pages/the_entity");
	public static final ResourceLocation TRUTH = entry("the_infection/pages/the_truth");
	public static final ResourceLocation BLOOD_MOONS = entry("cosmic_forces/pages/blood_moons");
	public static final ResourceLocation QLIPHOTH = entry("cosmic_forces/pages/the_qliphoth");
	public static final ResourceLocation SAINTS = entry("cosmic_forces/pages/the_saints");
	public static final ResourceLocation HEMORATH = entry("cosmic_forces/pages/hemorath");
	public static final ResourceLocation UNSTAINED_REJECTION = entry("the_unstained_path/pages/the_rejection");
	public static final ResourceLocation COPPER_AND_SILVER = entry("the_unstained_path/pages/copper_and_silver");
	public static final ResourceLocation PURIFIED = entry("the_unstained_path/pages/the_purified");
	public static final ResourceLocation IMMACULATUS_FIRST_STEPS = immaculatusEntry("intro/pages/first_steps");
	public static final ResourceLocation IMMACULATUS_INFECTION = immaculatusEntry("intro/pages/the_infection");
	public static final ResourceLocation IMMACULATUS_HEMOLYTIC_SOLUTION = immaculatusEntry("sacred_tools/pages/hemolytic_solution");
	public static final ResourceLocation IMMACULATUS_COPPER_AND_SILVER = immaculatusEntry("sacred_tools/pages/copper_and_silver");
	public static final ResourceLocation IMMACULATUS_SHE_WHO_LISTENS = immaculatusEntry("our_lady/pages/she_who_listens");
	public static final ResourceLocation IMMACULATUS_CLARITY_PRICE = immaculatusEntry("our_lady/pages/clarity_and_its_price");

	static {
		register(FIRST_RITE_NOTES, DiscoverySource.MEMO);
		register(HEMOMANCY, DiscoverySource.ITEM_PICKUP, DiscoverySource.RITE, DiscoverySource.DIALOGUE);
		register(ERYTHROMYCELIUM, DiscoverySource.ITEM_PICKUP, DiscoverySource.DIALOGUE);
		register(THE_HARBINGERS, DiscoverySource.DEGREE, DiscoverySource.RITE, DiscoverySource.ADVANCEMENT);
		register(THE_UNSTAINED, DiscoverySource.RITE, DiscoverySource.ADVANCEMENT, DiscoverySource.ITEM_PICKUP);
		register(DEGREES, DiscoverySource.DEGREE, DiscoverySource.RITE, DiscoverySource.ADVANCEMENT);
		register(HERMITS, DiscoverySource.DIALOGUE, DiscoverySource.RITE);
		register(ORDER_BELIEFS, DiscoverySource.DEGREE, DiscoverySource.RITE);
		register(HISTORICAL_RECORD, DiscoverySource.DEGREE, DiscoverySource.RITE);
		register(BLOOD_MEMORIES, DiscoverySource.ITEM_PICKUP, DiscoverySource.RITE);
		register(HYPHAE, DiscoverySource.ITEM_PICKUP, DiscoverySource.DIALOGUE);
		register(ENTITY, DiscoverySource.DEGREE, DiscoverySource.RITE, DiscoverySource.DIALOGUE);
		register(TRUTH, DiscoverySource.DEGREE, DiscoverySource.DIALOGUE);
		register(BLOOD_MOONS, DiscoverySource.RITE, DiscoverySource.ADVANCEMENT);
		register(QLIPHOTH, DiscoverySource.RITE, DiscoverySource.DEGREE, DiscoverySource.ITEM_PICKUP);
		register(SAINTS, DiscoverySource.ITEM_PICKUP, DiscoverySource.RITE);
		register(HEMORATH, DiscoverySource.ITEM_PICKUP);
		register(UNSTAINED_REJECTION, DiscoverySource.RITE, DiscoverySource.ADVANCEMENT);
		register(COPPER_AND_SILVER, DiscoverySource.RITE, DiscoverySource.ITEM_PICKUP);
		register(PURIFIED, DiscoverySource.ADVANCEMENT, DiscoverySource.RITE);
		register(IMMACULATUS_FIRST_STEPS, DiscoverySource.MEMO, DiscoverySource.RITE, DiscoverySource.DIALOGUE);
		register(IMMACULATUS_INFECTION, DiscoverySource.MEMO, DiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_HEMOLYTIC_SOLUTION, DiscoverySource.MEMO, DiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_COPPER_AND_SILVER, DiscoverySource.MEMO, DiscoverySource.RITE, DiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_SHE_WHO_LISTENS, DiscoverySource.MEMO, DiscoverySource.DIALOGUE);
		register(IMMACULATUS_CLARITY_PRICE, DiscoverySource.MEMO, DiscoverySource.RITE);

		registerRite("cardinal_rite/sanguine_initiation", HEMOMANCY);
		registerRite("cardinal_rite/sanguine_initiation", THE_HARBINGERS);
		registerRite("cardinal_rite/votary_rite", DEGREES);
		registerRite("cardinal_rite/initiate_rite", ORDER_BELIEFS);
		registerRite("cardinal_rite/adept_rite", HISTORICAL_RECORD);
		registerRite("cardinal_rite/illuminatus_rite", HERMITS);
		registerRite("cardinal_rite/archon_rite", ENTITY);
		registerRite("cardinal_rite/apotheos_rite", TRUTH);
		registerRite("cardinal_rite/bloodline_founding", BLOOD_MEMORIES);
		registerRite("cardinal_rite/sanguine_eclipse", BLOOD_MOONS);
		registerRite("cardinal_rite/bloom_of_qliphoth", QLIPHOTH);
		registerRite("cardinal_rite/lethean_baptism", THE_UNSTAINED);
		registerRite("cardinal_rite/lethean_baptism", IMMACULATUS_FIRST_STEPS);
		registerRite("cardinal_rite/silver_veil", COPPER_AND_SILVER);
		registerRite("cardinal_rite/silver_veil", IMMACULATUS_COPPER_AND_SILVER);
		registerRite("cardinal_rite/clarity_ascension", PURIFIED);
		registerRite("cardinal_rite/clarity_ascension", IMMACULATUS_CLARITY_PRICE);

		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE, HEMOMANCY);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE, THE_HARBINGERS);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY, DEGREES);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_3_INITIATE, ORDER_BELIEFS);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT, HISTORICAL_RECORD);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS, HERMITS);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON, ENTITY);
		registerAdvancement(HarbingerAdvancementGranter.ADV_DEGREE_8_APOTHEOS, TRUTH);
		registerAdvancement(HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND, BLOOD_MEMORIES);
		registerAdvancement(HarbingerAdvancementGranter.ADV_VOICES_IN_THE_VEIN, ENTITY);
		registerAdvancement(UnstainedAdvancementGranter.ADV_BLESSED_BY_ALTAR, THE_UNSTAINED);
		registerAdvancement(UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED, PURIFIED);
		registerAdvancement(UnstainedAdvancementGranter.ADV_PURIFIED, PURIFIED);

		registerItem("blood_stained_stone", HEMOMANCY);
		registerItem("bleeding_bulb", ERYTHROMYCELIUM);
		registerItem("dicentra_sap", ERYTHROMYCELIUM);
		registerItem("fungal_spine", HYPHAE);
		registerItem("spore_sac", HYPHAE);
		registerItem("sanguine_formation", BLOOD_MEMORIES);
		registerItem("qliphoth_seed", QLIPHOTH);
		registerItem("qliphoth_pome", QLIPHOTH);
		registerItem("hallowed_residuum_hemorath", HEMORATH);
		registerItem("consecrated_copper_ingot", COPPER_AND_SILVER);
		registerItem("hemolytic_solution", THE_UNSTAINED);
		registerItem("hemolytic_solution", IMMACULATUS_HEMOLYTIC_SOLUTION);
		registerItem("consecrated_copper_ingot", IMMACULATUS_COPPER_AND_SILVER);

		registerDialogue("hermit_first_rite", HERMITS);
	}

	private LiberEntryDefinitions() {
	}

	public static Optional<LiberEntryDefinition> get(ResourceLocation entryId) {
		return Optional.ofNullable(ENTRIES.get(entryId));
	}

	public static Collection<LiberEntryDefinition> all() {
		return ENTRIES.values();
	}

	public static java.util.Set<ResourceLocation> forRite(String ritePath) {
		return RITE_UNLOCKS.getOrDefault(ritePath, java.util.Set.of());
	}

	public static java.util.Set<ResourceLocation> forAdvancement(ResourceLocation advancementId) {
		return ADVANCEMENT_UNLOCKS.getOrDefault(advancementId, java.util.Set.of());
	}

	public static java.util.Set<ResourceLocation> forItem(ResourceLocation itemId) {
		return ITEM_UNLOCKS.getOrDefault(itemId, java.util.Set.of());
	}

	public static java.util.Set<ResourceLocation> forDialogueEvent(String eventId) {
		if (eventId == null || eventId.isEmpty()) {
			return java.util.Set.of();
		}
		if (eventId.startsWith(DIALOGUE_EVENT_PREFIX)) {
			ResourceLocation entryId = ResourceLocation.tryParse(eventId.substring(DIALOGUE_EVENT_PREFIX.length()));
			return entryId == null ? java.util.Set.of() : java.util.Set.of(entryId);
		}
		return DIALOGUE_UNLOCKS.getOrDefault(eventId, java.util.Set.of());
	}

	private static ResourceLocation entry(String path) {
		return Hemomancy.rloc("sanctumsanguinium/" + path);
	}

	private static ResourceLocation immaculatusEntry(String path) {
		return Hemomancy.rloc("liberimmaculatus/" + path);
	}

	private static void register(ResourceLocation entryId, DiscoverySource... sources) {
		ENTRIES.put(entryId, new LiberEntryDefinition(entryId, SetUtil.of(sources)));
	}

	private static void registerRite(String ritePath, ResourceLocation entryId) {
		add(RITE_UNLOCKS, ritePath, entryId);
	}

	private static void registerAdvancement(ResourceLocation advancementId, ResourceLocation entryId) {
		add(ADVANCEMENT_UNLOCKS, advancementId, entryId);
	}

	private static void registerItem(String itemPath, ResourceLocation entryId) {
		add(ITEM_UNLOCKS, Hemomancy.rloc(itemPath), entryId);
	}

	private static void registerDialogue(String eventId, ResourceLocation entryId) {
		add(DIALOGUE_UNLOCKS, eventId, entryId);
	}

	private static <K> void add(Map<K, java.util.Set<ResourceLocation>> map, K key, ResourceLocation entryId) {
		map.computeIfAbsent(key, ignored -> new java.util.LinkedHashSet<>()).add(entryId);
	}
}
