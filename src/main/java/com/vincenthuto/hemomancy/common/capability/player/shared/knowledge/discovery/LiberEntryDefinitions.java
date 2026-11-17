package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
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
	public static final ResourceLocation SCAR_PRACTICE = entry("the_hematic_order/pages/scar_practice");
	public static final ResourceLocation SCRIPTORIUM = entry("temperament/pages/enzymatic_scriptorium");
	public static final ResourceLocation BLOOD_MEMORIES = entry("the_infection/pages/blood_memories");
	public static final ResourceLocation MNEMONIC_AMBERGRIS = entry("the_infection/pages/mnemonic_ambergris");
	public static final ResourceLocation HYPHAE = entry("the_infection/pages/hyphae");
	public static final ResourceLocation ENTITY = entry("the_infection/pages/the_entity");
	public static final ResourceLocation TRUTH = entry("the_infection/pages/the_truth");
	public static final ResourceLocation BLOOD_MOONS = entry("cosmic_forces/pages/blood_moons");
	public static final ResourceLocation QLIPHOTH = entry("cosmic_forces/pages/the_qliphoth");
	public static final ResourceLocation SAINTS = entry("cosmic_forces/pages/the_saints");
	public static final ResourceLocation HEMORATH = entry("cosmic_forces/pages/hemorath");
	public static final ResourceLocation SERAPHAE = entry("cosmic_forces/pages/seraphae");
	public static final ResourceLocation PUTRICIEL = entry("cosmic_forces/pages/putriciel");
	public static final ResourceLocation VELORUM = entry("cosmic_forces/pages/velorum");
	public static final ResourceLocation PHLEGETHOSOMA = entry("cosmic_forces/pages/phlegethosoma");
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
	/** DS-4: the three vessel rites' page. */
	public static final ResourceLocation BLOOD_GOURDS = entry("the_hematic_order/pages/blood_gourds");

	// ── DS-9 pages (2026-11-16): the Armature chapter, a page for each craft, four bosses and the Brined Court ──
	public static final ResourceLocation ARMATURE_RACK = entry("armature/pages/the_rack");
	public static final ResourceLocation VESPER = entry("cosmic_forces/pages/vesper");
	public static final ResourceLocation MYCOPHANT = entry("cosmic_forces/pages/mycophant");
	public static final ResourceLocation NAEGLEROPHAEON = entry("cosmic_forces/pages/naeglerophaeon");
	public static final ResourceLocation COURT_HOUSES = entry("brined_court/pages/the_houses");
	public static final ResourceLocation BLOOM_WARDEN = entry("brined_court/pages/bloom_warden");
	public static final ResourceLocation KINDLED_CHAMPION = entry("brined_court/pages/kindled_champion");

	/**
	 * A craft's page: the rite that raises it ({@code null} for a floorless craft), the Degree that opens it and the item,
	 * named by the page, whose pickup opens it.
	 */
	public record CraftPage(ResourceLocation page, String rite, int degree) {
		public String item() {
			return page.getPath().substring(page.getPath().lastIndexOf('/') + 1);
		}
	}

	/** The crafts with no page of their own before DS-9, each opening at its craft's Degree (a floorless D0 craft at D1). */
	public static final java.util.List<CraftPage> CRAFT_PAGES = java.util.List.of(
			new CraftPage(entry("the_hematic_order/pages/scarlet_vanity"), "cardinal_rite/working/scarlet_vanity", 3),
			new CraftPage(entry("the_hematic_order/pages/mycelial_lantern"), "cardinal_rite/working/mycelial_lantern", 3),
			new CraftPage(entry("the_hematic_order/pages/mnemonic_reliquary"), "cardinal_rite/working/mnemonic_reliquary", 3),
			new CraftPage(entry("the_hematic_order/pages/somnolent_seat"), "cardinal_rite/working/somnolent_seat", 3),
			new CraftPage(entry("the_hematic_order/pages/fungal_implantation_pylon"), "cardinal_rite/working/fungal_implantation_pylon", 4),
			new CraftPage(entry("the_hematic_order/pages/mason_effigy"), "cardinal_rite/working/mason_effigy", 4),
			new CraftPage(entry("the_hematic_order/pages/scar_station"), "cardinal_rite/working/runic_chisel_station", 4),
			new CraftPage(entry("the_hematic_order/pages/dendritic_distributor"), "cardinal_rite/working/dendritic_distributor", 5),
			new CraftPage(entry("the_hematic_order/pages/morphling_incubator"), "cardinal_rite/working/morphling_incubator", 5),
			new CraftPage(entry("the_hematic_order/pages/vacant_effigy"), "cardinal_rite/working/vacant_effigy", 5),
			new CraftPage(entry("the_hematic_order/pages/sanguine_monolith"), "cardinal_rite/working/sanguine_monolith", 5),
			new CraftPage(entry("the_hematic_order/pages/covenant_throne"), "cardinal_rite/working/covenant_throne", 6),
			new CraftPage(entry("the_hematic_order/pages/sanguine_vigil"), "cardinal_rite/working/vascular_effigy", 6),
			new CraftPage(entry("the_hematic_order/pages/sporitic_thurible"), "cardinal_rite/working/sporitic_thurible", 8),
			new CraftPage(entry("the_hematic_order/pages/dried_gourd"), null, 1),
			new CraftPage(entry("the_hematic_order/pages/hematic_stake"), null, 5),
			new CraftPage(entry("the_hematic_order/pages/mycelial_synapse"), null, 8));

	// ── Core loop pages (2026-11-09 to 11-10): unlocked by Degree, by the item that teaches them, or by the core-loop
	// first that marks the act (core-loop Track 11; before it they were written but never registered, so hidden) ──
	public static final ResourceLocation THE_TWO_VERBS = entry("manipulations/pages/the_two_verbs");
	public static final ResourceLocation LIGNUM_MORTIS = entry("manipulations/pages/lignum_mortis");
	public static final ResourceLocation PYRETIC_FORGE = entry("manipulations/pages/pyretic_forge");
	public static final ResourceLocation TENDON_LINE = entry("manipulations/pages/tendon_line");
	public static final ResourceLocation BLOOD_BINDING = entry("manipulations/pages/blood_binding");
	public static final ResourceLocation BRAZIER_TRANSMUTATION = entry("manipulations/pages/brazier_transmutation");
	public static final ResourceLocation THE_GROUND_NETWORK = entry("manipulations/pages/the_ground_network");
	public static final ResourceLocation LIVING_VEINS_AND_BASINS = entry("manipulations/pages/living_veins_and_basins");
	public static final ResourceLocation GROUND_SCULPTINGS = entry("manipulations/pages/ground_sculptings");
	public static final ResourceLocation STRANDS = entry("manipulations/pages/strands");
	public static final ResourceLocation STRAND_FORMS = entry("manipulations/pages/strand_forms");
	public static final ResourceLocation THE_DRAW_AND_THE_MARK = entry("manipulations/pages/the_draw_and_the_mark");
	public static final ResourceLocation THE_STAFF_GROWS = entry("manipulations/pages/the_staff_grows");
	public static final ResourceLocation MINING_SCULPTINGS = entry("manipulations/pages/mining_sculptings");
	public static final ResourceLocation LIVING_LAND = entry("manipulations/pages/living_land");
	public static final ResourceLocation VENOUS_CLING = entry("manipulations/pages/venous_cling");
	/** Core-loop round two P4: the second wave of Sculptings, on two pages. */
	public static final ResourceLocation SECOND_SCULPTINGS = entry("manipulations/pages/second_sculptings");
	public static final ResourceLocation SECOND_SCULPTINGS_WALLS = entry("manipulations/pages/second_sculptings_walls");
	/** Core-loop round two P5: the second wave of strands, the stain run, the strand skill line and the twin. */
	public static final ResourceLocation SECOND_STRANDS = entry("manipulations/pages/second_strands");
	public static final ResourceLocation TEMPER = entry("temperament/pages/temper");
	public static final ResourceLocation LIVING_BLOOD = entry("the_hematic_order/pages/living_blood");
	public static final ResourceLocation FED_GROUND = entry("the_hematic_order/pages/fed_ground");
	public static final ResourceLocation CHUM = entry("the_hematic_order/pages/chum");
	public static final ResourceLocation MENAGERIE = entry("the_hematic_order/pages/menagerie");
	public static final ResourceLocation UNREAD_HUMOURS = entry("the_hematic_order/pages/unread_humours");
	/** Core-loop round two P6: the vessel's screen, the syringe and the vial, the wider Bestiary and the brood. */
	public static final ResourceLocation THE_BROOD = entry("the_hematic_order/pages/the_brood");
	public static final ResourceLocation STRAIN_BEDS = entry("the_hematic_order/pages/strain_beds");
	public static final ResourceLocation THE_BODY_AND_THE_DAY = entry("the_hematic_order/pages/the_body_and_the_day");
	public static final ResourceLocation THE_WORLD_ANSWERS = entry("the_hematic_order/pages/the_world_answers");
	/** Every page the core loop wrote, in the order a Harbinger meets them. */
	public static final java.util.List<ResourceLocation> CORE_LOOP_PAGES = java.util.List.of(THE_TWO_VERBS, LIVING_BLOOD,
			THE_BODY_AND_THE_DAY, THE_DRAW_AND_THE_MARK, THE_GROUND_NETWORK, FED_GROUND, CHUM, TEMPER, UNREAD_HUMOURS,
			THE_WORLD_ANSWERS, LIGNUM_MORTIS, PYRETIC_FORGE, TENDON_LINE, BLOOD_BINDING, GROUND_SCULPTINGS, STRANDS, STRAND_FORMS,
			MINING_SCULPTINGS, THE_STAFF_GROWS, BRAZIER_TRANSMUTATION, MENAGERIE, LIVING_VEINS_AND_BASINS, LIVING_LAND,
			VENOUS_CLING, SECOND_SCULPTINGS, SECOND_SCULPTINGS_WALLS, SECOND_STRANDS, THE_BROOD, STRAIN_BEDS);

	// ── Older pages (CQ-1, core-loop round two P9): written before the core loop and never registered, so hidden. Each
	// has a Degree floor in LiberKnowledgeHelper (a purity or clarity stage for Liber Immaculatus) and earlier openers ──
	public static final ResourceLocation MANIPULATIONS_OVERVIEW = entry("manipulations/pages/overview");
	public static final ResourceLocation ANIMUS_MANIPULATIONS = entry("manipulations/pages/vivacious");
	public static final ResourceLocation FLAMMEUS_MANIPULATIONS = entry("manipulations/pages/fervent");
	public static final ResourceLocation DUCTILIS_MANIPULATIONS = entry("manipulations/pages/neurotic");
	public static final ResourceLocation FERRIC_MANIPULATIONS = entry("manipulations/pages/ferric");
	public static final ResourceLocation LUX_MANIPULATIONS = entry("manipulations/pages/incandescent");
	public static final ResourceLocation MORTEM_MANIPULATIONS = entry("manipulations/pages/ruinous");
	public static final ResourceLocation CONGEATIO_MANIPULATIONS = entry("manipulations/pages/frigid");
	public static final ResourceLocation TENEBRIS_MANIPULATIONS = entry("manipulations/pages/umbral");
	public static final ResourceLocation MEMORY_WEAVING = entry("manipulations/pages/memory_weaving");
	public static final ResourceLocation SAINT_MEMORIES = entry("manipulations/pages/saint_memories");
	public static final ResourceLocation THELEMIC_MEMORIES = entry("manipulations/pages/thelemic_memories");
	public static final ResourceLocation TEMPERAMENT = entry("temperament/pages/temperament");
	public static final ResourceLocation ANIMUS_TEMPERAMENT = entry("temperament/pages/vivacious");
	public static final ResourceLocation FLAMMEUS_TEMPERAMENT = entry("temperament/pages/fervent");
	public static final ResourceLocation DUCTILIS_TEMPERAMENT = entry("temperament/pages/neurotic");
	public static final ResourceLocation FERRIC_TEMPERAMENT = entry("temperament/pages/ferric");
	public static final ResourceLocation LUX_TEMPERAMENT = entry("temperament/pages/incandescent");
	public static final ResourceLocation MORTEM_TEMPERAMENT = entry("temperament/pages/ruinous");
	public static final ResourceLocation CONGEATIO_TEMPERAMENT = entry("temperament/pages/frigid");
	public static final ResourceLocation TENEBRIS_TEMPERAMENT = entry("temperament/pages/umbral");
	public static final ResourceLocation FORGE_RECORDING = entry("resonant_forge/pages/recording");
	public static final ResourceLocation FORGE_MAINTENANCE = entry("resonant_forge/pages/maintenance");
	public static final ResourceLocation BLOODWORK_STATIONS = entry("the_hematic_order/pages/bloodwork_stations");
	public static final ResourceLocation SANGUINE_CONDENSER = entry("the_hematic_order/pages/sanguine_condenser");
	public static final ResourceLocation MORPHLING_CULTIVATION = entry("the_hematic_order/pages/morphling_cultivation");
	public static final ResourceLocation MORPHLING_STRAINS = entry("the_hematic_order/pages/morphling_strains");
	public static final ResourceLocation MORPHLING_CRADLE_AND_NECTAR = entry("the_hematic_order/pages/morphling_cradle_and_nectar");
	public static final ResourceLocation SHARED_MOB_MATERIALS = entry("the_infection/pages/shared_mob_materials");
	public static final ResourceLocation ANNETTA_KNOWLES = entry("cosmic_forces/pages/annetta_knowles");
	public static final ResourceLocation IMMACULATUS_WHAT_PURITY_GRANTS = immaculatusEntry("intro/pages/what_purity_grants");
	public static final ResourceLocation IMMACULATUS_LETHEAN_GIFTS = immaculatusEntry("our_lady/pages/lethean_gifts");
	public static final ResourceLocation IMMACULATUS_PALE_INTERCESSION = immaculatusEntry("our_lady/pages/pale_intercession");
	public static final ResourceLocation IMMACULATUS_GHOST_PIPE = immaculatusEntry("sacred_tools/pages/ghost_pipe");
	public static final ResourceLocation IMMACULATUS_VIRID_SALIS = immaculatusEntry("sacred_tools/pages/virid_salis_brushing");
	public static final ResourceLocation IMMACULATUS_WHITE_HUMOR = immaculatusEntry("sacred_tools/pages/white_humor_purification");
	public static final ResourceLocation IMMACULATUS_LIVING_THE_LIFE = immaculatusEntry("the_path/pages/living_the_life");
	public static final ResourceLocation IMMACULATUS_STAGES_OF_CLARITY = immaculatusEntry("the_path/pages/stages_of_clarity");
	public static final ResourceLocation IMMACULATUS_STAGES_OF_PURITY = immaculatusEntry("the_path/pages/stages_of_purity");
	public static final ResourceLocation IMMACULATUS_STILL_ARTS = immaculatusEntry("the_path/pages/still_arts");
	/** The older Liber Sanguinium pages, in chapter order. */
	public static final java.util.List<ResourceLocation> OLDER_PAGES = java.util.List.of(ANNETTA_KNOWLES,
			MANIPULATIONS_OVERVIEW, ANIMUS_MANIPULATIONS, FLAMMEUS_MANIPULATIONS, DUCTILIS_MANIPULATIONS, FERRIC_MANIPULATIONS,
			LUX_MANIPULATIONS, MORTEM_MANIPULATIONS, CONGEATIO_MANIPULATIONS, TENEBRIS_MANIPULATIONS, MEMORY_WEAVING,
			SAINT_MEMORIES, THELEMIC_MEMORIES, FORGE_RECORDING, FORGE_MAINTENANCE, TEMPERAMENT, ANIMUS_TEMPERAMENT,
			FLAMMEUS_TEMPERAMENT, DUCTILIS_TEMPERAMENT, FERRIC_TEMPERAMENT, LUX_TEMPERAMENT, MORTEM_TEMPERAMENT,
			CONGEATIO_TEMPERAMENT, TENEBRIS_TEMPERAMENT, BLOODWORK_STATIONS, SANGUINE_CONDENSER, MORPHLING_CULTIVATION,
			MORPHLING_STRAINS, MORPHLING_CRADLE_AND_NECTAR, SHARED_MOB_MATERIALS);
	/** The older Liber Immaculatus pages. */
	public static final java.util.List<ResourceLocation> OLDER_IMMACULATUS_PAGES = java.util.List.of(
			IMMACULATUS_WHAT_PURITY_GRANTS, IMMACULATUS_LETHEAN_GIFTS, IMMACULATUS_PALE_INTERCESSION, IMMACULATUS_GHOST_PIPE,
			IMMACULATUS_VIRID_SALIS, IMMACULATUS_WHITE_HUMOR, IMMACULATUS_LIVING_THE_LIFE, IMMACULATUS_STAGES_OF_CLARITY,
			IMMACULATUS_STAGES_OF_PURITY, IMMACULATUS_STILL_ARTS);

	/** The Manipulations chapter's page for {@code school}'s memories. */
	public static ResourceLocation manipulationsPage(EnumBloodTemper school) {
		return switch (school) {
			case ANIMUS -> ANIMUS_MANIPULATIONS;
			case FLAMMEUS -> FLAMMEUS_MANIPULATIONS;
			case DUCTILIS -> DUCTILIS_MANIPULATIONS;
			case FERRIC -> FERRIC_MANIPULATIONS;
			case LUX -> LUX_MANIPULATIONS;
			case MORTEM -> MORTEM_MANIPULATIONS;
			case CONGEATIO -> CONGEATIO_MANIPULATIONS;
			case TENEBRIS -> TENEBRIS_MANIPULATIONS;
		};
	}

	/** The Temperament chapter's page for {@code school}. */
	public static ResourceLocation temperamentPage(EnumBloodTemper school) {
		return switch (school) {
			case ANIMUS -> ANIMUS_TEMPERAMENT;
			case FLAMMEUS -> FLAMMEUS_TEMPERAMENT;
			case DUCTILIS -> DUCTILIS_TEMPERAMENT;
			case FERRIC -> FERRIC_TEMPERAMENT;
			case LUX -> LUX_TEMPERAMENT;
			case MORTEM -> MORTEM_TEMPERAMENT;
			case CONGEATIO -> CONGEATIO_TEMPERAMENT;
			case TENEBRIS -> TENEBRIS_TEMPERAMENT;
		};
	}

	static {
        register(entry("the_hematic_order/pages/troupe_school"), HemomancyDiscoverySource.DIALOGUE);
        for (var lesson : com.vincenthuto.hemomancy.common.mission.circus.CircusCurriculum.lessons())
            register(entry("the_hematic_order/pages/troupe_" + lesson.summon()), HemomancyDiscoverySource.DIALOGUE,
                    HemomancyDiscoverySource.RITE);

        for (var fact : com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch.Evidence.values())
            register(entry("the_infection/pages/antecedent_" + fact.key()), HemomancyDiscoverySource.MEMO);
        register(entry("the_infection/pages/clairaudiograph"), HemomancyDiscoverySource.DIALOGUE);
        for (String lesson : java.util.List.of("specimen", "microscopy", "ordered_collection", "microscope", "injection",
                "cabinet", "field_referral", "field_case", "echo_referral", "clairaudiograph")) {
            register(com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge.entry(lesson),
                    HemomancyDiscoverySource.DIALOGUE);
        }
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
		register(SCAR_PRACTICE, CommonDiscoverySource.ADVANCEMENT);
		register(SCRIPTORIUM, HemomancyDiscoverySource.DIALOGUE);
		registerUpgradeLesson("cardinal_rite/first_condensation", "alembic/pages/first_condensation");
		registerUpgradeLesson("cardinal_rite/first_condensation", "alembic/pages/refined_and_compound");
		registerUpgradeLesson("cardinal_rite/sanguine_athanor", "alembic/pages/sanguine_athanor");
		registerUpgradeLesson("cardinal_rite/sanguine_athanor", "alembic/pages/bound_potion");
		registerUpgradeLesson("cardinal_rite/steady_separation", "centrifuge/pages/steady_separation");
		registerUpgradeLesson("cardinal_rite/second_fraction", "centrifuge/pages/second_fraction");
		registerUpgradeLesson("cardinal_rite/true_groove", "resonant_forge/pages/upgrades");
		registerRite("cardinal_rite/enduring_pattern", entry("resonant_forge/pages/upgrades"));
		registerUpgradeLesson("cardinal_rite/eightfold_script", "temperament/pages/eightfold_script");
		registerUpgradeLesson("cardinal_rite/palimpsest", "temperament/pages/palimpsest");
		registerUpgradeLesson("cardinal_rite/ganglionic_weave", "manipulations/pages/ganglionic_weave");
		registerUpgradeLesson("cardinal_rite/cortical_weave", "manipulations/pages/cortical_weave");
		registerUpgradeLesson("cardinal_rite/first_clotting", "the_hematic_order/pages/first_clotting");
		registerUpgradeLesson("cardinal_rite/druse_crystallization", "the_hematic_order/pages/druse_crystallization");
		register(BLOOD_MEMORIES, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE,
				HemomancyDiscoverySource.BLOOD_ECHO, HemomancyDiscoverySource.RITE_FRAGMENT);
		register(MNEMONIC_AMBERGRIS, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE);
		register(BLOOD_GOURDS, HemomancyDiscoverySource.RITE);
		register(ARMATURE_RACK, HemomancyDiscoverySource.DEGREE, CommonDiscoverySource.ITEM_PICKUP, CommonDiscoverySource.ADVANCEMENT);
		registerUpgradeLesson("cardinal_rite/armature_consecration", "armature/pages/consecration");
		registerUpgradeLesson("cardinal_rite/monolithic_armature", "armature/pages/monolithic_armature");
		for (CraftPage craft : CRAFT_PAGES) {
			register(craft.page(), HemomancyDiscoverySource.DEGREE, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE);
			if (craft.rite() != null) registerRite(craft.rite(), craft.page());
		}
		register(VESPER, HemomancyDiscoverySource.ENCOUNTER);
		register(MYCOPHANT, HemomancyDiscoverySource.ENCOUNTER);
		register(NAEGLEROPHAEON, HemomancyDiscoverySource.ENCOUNTER);
		register(COURT_HOUSES, HemomancyDiscoverySource.DIALOGUE, CommonDiscoverySource.STRUCTURE_DISCOVER);
		register(BLOOM_WARDEN, HemomancyDiscoverySource.ENCOUNTER);
		register(KINDLED_CHAMPION, HemomancyDiscoverySource.ENCOUNTER);
		register(HYPHAE, HemomancyDiscoverySource.MEMO, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.DIALOGUE,
				HemomancyDiscoverySource.BLOOD_ECHO);
		register(ENTITY, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.DIALOGUE, CommonDiscoverySource.ITEM_PICKUP);
		register(TRUTH, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.DIALOGUE, HemomancyDiscoverySource.RITE);
		register(BLOOD_MOONS, HemomancyDiscoverySource.RITE, CommonDiscoverySource.ADVANCEMENT);
		register(QLIPHOTH, HemomancyDiscoverySource.MEMO, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.DEGREE, CommonDiscoverySource.ITEM_PICKUP);
		register(SAINTS, CommonDiscoverySource.ITEM_PICKUP, HemomancyDiscoverySource.RITE, HemomancyDiscoverySource.BLOOD_ECHO);
		register(HEMORATH, CommonDiscoverySource.ITEM_PICKUP);
		register(SERAPHAE, CommonDiscoverySource.ITEM_PICKUP);
		register(PUTRICIEL, CommonDiscoverySource.ITEM_PICKUP);
		register(VELORUM, CommonDiscoverySource.ITEM_PICKUP);
		register(PHLEGETHOSOMA, HemomancyDiscoverySource.DEGREE, HemomancyDiscoverySource.ENCOUNTER);
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

		// The outpost speakers' core-loop lessons open them too (round two P10, CoreLoopLessons).
		for (ResourceLocation page : CORE_LOOP_PAGES) {
			register(page, HemomancyDiscoverySource.DEGREE, CommonDiscoverySource.ITEM_PICKUP, CommonDiscoverySource.ADVANCEMENT,
					HemomancyDiscoverySource.DIALOGUE);
		}
		// The older pages (CQ-1): a Degree floor, the items that teach them, the advancements that mark them, and a
		// memory learned (its school's pages) or an encounter begun (Annetta).
		for (ResourceLocation page : OLDER_PAGES) {
			register(page, HemomancyDiscoverySource.DEGREE, CommonDiscoverySource.ITEM_PICKUP, CommonDiscoverySource.ADVANCEMENT,
					HemomancyDiscoverySource.PRACTICE, HemomancyDiscoverySource.DIALOGUE);
		}
		for (ResourceLocation page : OLDER_IMMACULATUS_PAGES) {
			register(page, HemomancyDiscoverySource.PRACTICE, CommonDiscoverySource.ITEM_PICKUP, CommonDiscoverySource.ADVANCEMENT);
		}

		registerRite("cardinal_rite/initiate_rite", ORDER_BELIEFS);
		registerRite("cardinal_rite/sanctified_rite", HISTORICAL_RECORD);
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
		registerRite("cardinal_rite/rooted_vein",               HEMOMANCY);
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
		registerRite("cardinal_rite/ancestral_communion",       ENTITY);
		// DS-4: one line each for the nineteen Harbinger rites that had no page.
		registerRite("cardinal_rite/puppeteer_trial_veinwing_vulture", entry("the_hematic_order/pages/troupe_veinwing_vulture"));
		registerRite("cardinal_rite/puppeteer_trial_marrow_spitter", entry("the_hematic_order/pages/troupe_marrow_spitter"));
		registerRite("cardinal_rite/puppeteer_trial_sanguine_hound", entry("the_hematic_order/pages/troupe_sanguine_hound"));
		registerRite("cardinal_rite/puppeteer_trial_cinder_bellows", entry("the_hematic_order/pages/troupe_cinder_bellows"));
		registerRite("cardinal_rite/puppeteer_trial_scarlet_mummer", entry("the_hematic_order/pages/troupe_scarlet_mummer"));
		registerRite("cardinal_rite/puppeteer_trial_gorebound_hulk", entry("the_hematic_order/pages/troupe_gorebound_hulk"));
		registerRite("cardinal_rite/puppeteer_trial_mnemonist_puppet", entry("the_hematic_order/pages/troupe_mnemonist_puppet"));
		registerRite("cardinal_rite/pallid_vessel_rite",        BLOOD_GOURDS);
		registerRite("cardinal_rite/crimson_vessel_rite",       BLOOD_GOURDS);
		registerRite("cardinal_rite/ashen_vessel_rite",         BLOOD_GOURDS);
		registerRite("cardinal_rite/bloodline_recall",          HISTORICAL_RECORD);
		registerRite("cardinal_rite/covenant_vigil",            HISTORICAL_RECORD);
		registerRite("cardinal_rite/eternal_covenant",          HISTORICAL_RECORD);
		registerRite("cardinal_rite/hematic_succession",        MNEMONIC_AMBERGRIS);
		registerRite("cardinal_rite/mnemonic_restoration",      MNEMONIC_AMBERGRIS);
		registerRite("cardinal_rite/possession_rite",           ENTITY);
		registerRite("cardinal_rite/visitation_rite",           TRUTH);
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

	/** Only events registered here unlock entries; a client cannot name an entry in the event id. */
	public static java.util.Set<ResourceLocation> forDialogueEvent(String eventId) {
		if (eventId == null || eventId.isEmpty()) {
			return java.util.Set.of();
		}
		return DIALOGUE_UNLOCKS.getOrDefault(eventId, java.util.Set.of());
	}

	private static ResourceLocation entry(String path) {
		return Hemomancy.rloc("libersanguinium/" + path);
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

	private static void registerUpgradeLesson(String ritePath, String pagePath) {
		ResourceLocation entryId = entry(pagePath);
		register(entryId, HemomancyDiscoverySource.PRACTICE, HemomancyDiscoverySource.RITE);
		registerRite(ritePath, entryId);
	}

	private static void registerDialogue(String eventId, ResourceLocation entryId) {
		add(DIALOGUE_UNLOCKS, eventId, entryId);
	}

	private static <K> void add(Map<K, java.util.Set<ResourceLocation>> map, K key, ResourceLocation entryId) {
		map.computeIfAbsent(key, ignored -> new java.util.LinkedHashSet<>()).add(entryId);
	}
}
