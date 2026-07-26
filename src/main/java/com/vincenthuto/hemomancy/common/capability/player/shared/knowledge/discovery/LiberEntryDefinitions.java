package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class LiberEntryDefinitions {
	public static final String DIALOGUE_EVENT_PREFIX = "liber_unlock:";

	private static final Map<ResourceLocation, LiberEntryDefinition> ENTRIES = new LinkedHashMap<>();
	private static final Map<String, java.util.Set<ResourceLocation>> RITE_UNLOCKS = new LinkedHashMap<>();
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
	public static final ResourceLocation ABOCIPHER_LITERACY = entry("the_hematic_order/pages/abocipher_literacy");
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
	public static final ResourceLocation IMMACULATUS_OBSERVANCES = immaculatusEntry("sacred_tools/pages/book_of_observances");
	public static final ResourceLocation IMMACULATUS_STILLWATER_CONDENSER = immaculatusEntry("sacred_tools/pages/stillwater_condenser");
	public static final ResourceLocation IMMACULATUS_SHE_WHO_LISTENS = immaculatusEntry("our_lady/pages/she_who_listens");
	public static final ResourceLocation IMMACULATUS_CLARITY_PRICE = immaculatusEntry("our_lady/pages/clarity_and_its_price");
	public static final ResourceLocation ANNETTA_KNOWLES_GEODE = entry("cosmic_forces/pages/annetta_geode_memo");
	public static final ResourceLocation IMMACULATUS_ANNETTA_GEODE = immaculatusEntry("the_path/pages/annetta_geode_memo");

	static {
		register(FIRST_RITE_NOTES, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.RITE_FRAGMENT);
		register(HEMOMANCY, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.DIALOGUE,
				HemomancyDiscoverySource.RITE_FRAGMENT);
		register(ERYTHROMYCELIUM, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.DIALOGUE);
		register(THE_HARBINGERS, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.RITE_FRAGMENT,
				CommonDiscoverySource.ADVANCEMENT, CommonDiscoverySource.ITEM_PICKUP);
		register(THE_UNSTAINED, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.RITE_FRAGMENT,
				CommonDiscoverySource.ADVANCEMENT, CommonDiscoverySource.ITEM_PICKUP);
		register(DEGREES, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.RITE_FRAGMENT,
				CommonDiscoverySource.ADVANCEMENT);
		register(HERMITS, HemomancyDiscoverySource.DIALOGUE, HemomancyDiscoverySource.RITE);
		register(ORDER_BELIEFS, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.RITE);
		register(HISTORICAL_RECORD, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.RITE_FRAGMENT);
		register(ABOCIPHER_LITERACY, HemomancyDiscoverySource.DIALOGUE);
		register(BLOOD_MEMORIES, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE,
				HemomancyDiscoverySource.BLOOD_ECHO, HemomancyDiscoverySource.RITE_FRAGMENT);
		register(HYPHAE, HemomancyDiscoverySource.MEMO, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.DIALOGUE,
				HemomancyDiscoverySource.BLOOD_ECHO);
		register(ENTITY, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.DIALOGUE, CommonDiscoverySource.ITEM_PICKUP);
		register(TRUTH, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.DIALOGUE);
		register(BLOOD_MOONS, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ADVANCEMENT);
		register(QLIPHOTH, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.DEGREE, CommonDiscoverySource.ITEM_PICKUP);
		register(SAINTS, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.BLOOD_ECHO);
		register(HEMORATH, CommonDiscoverySource.ITEM_PICKUP);
		register(UNSTAINED_REJECTION, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ADVANCEMENT);
		register(COPPER_AND_SILVER, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ITEM_PICKUP);
		register(PURIFIED, CommonDiscoverySource.ADVANCEMENT, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_FIRST_STEPS, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.DIALOGUE,
				HemomancyDiscoverySource.RITE_FRAGMENT);
		register(IMMACULATUS_INFECTION, HemomancyDiscoverySource.MEMO, CommonDiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_HEMOLYTIC_SOLUTION, HemomancyDiscoverySource.MEMO, CommonDiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_COPPER_AND_SILVER, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_OBSERVANCES, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_STILLWATER_CONDENSER, CommonDiscoverySource.ITEM_PICKUP);
		register(IMMACULATUS_SHE_WHO_LISTENS, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.DIALOGUE,
				HemomancyDiscoverySource.RITE_FRAGMENT);
		register(IMMACULATUS_CLARITY_PRICE, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.RITE,
				HemomancyDiscoverySource.RITE_FRAGMENT);
		register(ANNETTA_KNOWLES_GEODE, HemomancyDiscoverySource.MEMO);
		register(IMMACULATUS_ANNETTA_GEODE, HemomancyDiscoverySource.MEMO);

		registerRite("cardinal_rite/sanguine_initiation", HEMOMANCY);
		registerRite("cardinal_rite/sanguine_initiation", THE_HARBINGERS);
		registerRite("cardinal_rite/sanguine_initiation", FIRST_RITE_NOTES);
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
		registerRite("cardinal_rite/lethean_baptism", IMMACULATUS_OBSERVANCES);
		registerRite("cardinal_rite/silver_veil", COPPER_AND_SILVER);
		registerRite("cardinal_rite/silver_veil", IMMACULATUS_COPPER_AND_SILVER);
		registerRite("cardinal_rite/clarity_ascension", PURIFIED);
		registerRite("cardinal_rite/clarity_ascension", IMMACULATUS_CLARITY_PRICE);
		// Harbinger utility / greater rites
		registerRite("cardinal_rite/vascular_mending",         HEMOMANCY);
		registerRite("cardinal_rite/hungering_earth",           HEMOMANCY);
		registerRite("cardinal_rite/hematic_fortification",     THE_HARBINGERS);
		registerRite("cardinal_rite/sanguine_fervor",           THE_HARBINGERS);
		registerRite("cardinal_rite/sanguine_attunement",       THE_HARBINGERS);
		registerRite("cardinal_rite/exsanguination",            BLOOD_MEMORIES);
		registerRite("cardinal_rite/crimson_beacon",            BLOOD_MEMORIES);
		registerRite("cardinal_rite/sanguine_brotherhood",      HISTORICAL_RECORD);
		registerRite("cardinal_rite/sanguine_dominion",         BLOOD_MEMORIES);
		registerRite("cardinal_rite/hematic_unbinding",         BLOOD_MEMORIES);
		registerRite("cardinal_rite/scarlet_summons",           BLOOD_MEMORIES);
		registerRite("cardinal_rite/founding_fane",          BLOOD_MEMORIES);
		registerRite("cardinal_rite/chamber_of_will",        BLOOD_MEMORIES);
		registerRite("cardinal_rite/horn_of_culmination_rite",  BLOOD_MEMORIES);
		registerRite("cardinal_rite/pruning_of_qliphoth",       QLIPHOTH);
		registerRite("cardinal_rite/ancestral_communion",       ENTITY);
		// Unstained rites
		registerRite("cardinal_rite/still_waters",              IMMACULATUS_SHE_WHO_LISTENS);
		registerRite("cardinal_rite/silthmeres_remembrance",    IMMACULATUS_SHE_WHO_LISTENS);
		registerRite("cardinal_rite/pale_vigil",                IMMACULATUS_CLARITY_PRICE);
		registerRite("cardinal_rite/pale_consecration",         IMMACULATUS_COPPER_AND_SILVER);
		registerRite("cardinal_rite/silver_dawn",               COPPER_AND_SILVER);
		registerRite("cardinal_rite/lethean_tide",              THE_UNSTAINED);
		registerRite("cardinal_rite/lethean_tide",              BLOOD_MOONS);
		registerRite("cardinal_rite/pallid_shadow",             UNSTAINED_REJECTION);
		registerRite("cardinal_rite/lethean_font",              PURIFIED);
		registerRite("cardinal_rite/lethean_judgment",          PURIFIED);
		registerRite("cardinal_rite/lethe_covenant",            PURIFIED);

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
		return Hemomancy.rloc("fanesanguinium/" + path);
	}

	private static ResourceLocation immaculatusEntry(String path) {
		return Hemomancy.rloc("liberimmaculatus/" + path);
	}

	private static void register(ResourceLocation entryId, IDiscoverySource... sources) {
		ENTRIES.put(entryId, new LiberEntryDefinition(entryId, SetUtil.of(sources)));
	}

	private static void registerRite(String ritePath, ResourceLocation entryId) {
		add(RITE_UNLOCKS, ritePath, entryId);
	}

	private static void registerDialogue(String eventId, ResourceLocation entryId) {
		add(DIALOGUE_UNLOCKS, eventId, entryId);
	}

	private static <K> void add(Map<K, java.util.Set<ResourceLocation>> map, K key, ResourceLocation entryId) {
		map.computeIfAbsent(key, ignored -> new java.util.LinkedHashSet<>()).add(entryId);
	}
}
