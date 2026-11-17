package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter;
import com.vincenthuto.hutoslib.common.book.knowledge.BookEntryRegistry;
import com.vincenthuto.hutoslib.common.book.knowledge.CommonDiscoverySource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

import java.util.List;
import java.util.Set;

/**
 * Thin bridging handlers that read from HutosLib's {@link BookEntryRegistry}
 * and write the resulting unlocks into Hemomancy's own {@code IBookKnowledge}
 * capability via {@link LiberKnowledgeHelper}.
 *
 * <p>HutosLib's {@code BookDiscoveryEvents} mirrors the same events and writes
 * into HutosLib's own {@code BookKnowledge} attachment (used by HutosLib's
 * generic guide). Because Hemomancy stores knowledge in its own capability
 * ({@code HemoAttachmentTypes.LIBER_KNOWLEDGE}), it must maintain its own
 * listeners — but registration of <em>what unlocks what</em> is now shared
 * through {@code BookEntryRegistry} rather than Hemomancy's own internal maps.
 *
 * <p>Item and advancement → entry mappings are registered during this class's
 * {@code FMLCommonSetupEvent} listener.
 *
 * <p>Rite-based and dialogue-based unlocks are not event-driven and are
 * triggered programmatically from {@link LiberKnowledgeHelper} directly.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class LiberDiscoveryEvents {
	private LiberDiscoveryEvents() {
	}

	/**
	 * Registers all Hemomancy item and advancement → Liber Sanguinium entry
	 * unlock mappings with HutosLib's {@link BookEntryRegistry}. This shared
	 * registry is used by HutosLib's own discovery events and Hemomancy's bridge
	 * handlers below.
	 *
	 * <p>Rite-based and dialogue-based unlocks remain in
	 * {@link LiberEntryDefinitions} and are triggered programmatically from
	 * {@link LiberKnowledgeHelper} directly.
	 */
	public static void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(LiberDiscoveryEvents::initLiberBookUnlocks);
	}

	private static void initLiberBookUnlocks() {
		// ── Item pickup → page unlocks ────────────────────────────────────────
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_stained_stone"),  LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bloody_vial"),           LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_crystal_shard"),   LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_rock"),            LiberEntryDefinitions.HEMOMANCY);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bleeding_bulb"),         LiberEntryDefinitions.ERYTHROMYCELIUM);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("talaromyces_minus"),     LiberEntryDefinitions.ERYTHROMYCELIUM);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("blood_gourd_red"),       LiberEntryDefinitions.ERYTHROMYCELIUM);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("barbed_blade"),          LiberEntryDefinitions.THE_HARBINGERS);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fungal_spine"),          LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("spore_sac"),             LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("chitinous_husk"),        LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fervent_husk"),          LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("foul_paste"),            LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("serpent_scale"),         LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("ferric_spores"),         LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vivacious_spores"),      LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fervent_spores"),        LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("frigid_spores"),         LiberEntryDefinitions.HYPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("umbral_spores"),         LiberEntryDefinitions.HYPHAE);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("sanguine_formation"),         LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hematic_memory"),             LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("engram_stamp"),               LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("unsigned_ancestral_ledger"),  LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("mnemonic_ambergris"), LiberEntryDefinitions.MNEMONIC_AMBERGRIS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("qliphoth_seed"),  LiberEntryDefinitions.QLIPHOTH);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("qliphoth_pome"),  LiberEntryDefinitions.QLIPHOTH);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("silthmere_glaive"),            LiberEntryDefinitions.SAINTS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_hemorath"),  LiberEntryDefinitions.HEMORATH);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_seraphae"),  LiberEntryDefinitions.SERAPHAE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_putriciel"), LiberEntryDefinitions.PUTRICIEL);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_velorum"),   LiberEntryDefinitions.VELORUM);
		for (String saint : new String[] { "hemorath", "seraphae", "putriciel", "velorum" })
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_" + saint), LiberEntryDefinitions.SAINTS);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("consecrated_copper_ingot"),  LiberEntryDefinitions.COPPER_AND_SILVER);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("silver_chalice"),            LiberEntryDefinitions.COPPER_AND_SILVER);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vivianite_cluster"),         LiberEntryDefinitions.COPPER_AND_SILVER);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hemolytic_solution"),  LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vivianite_scalpel"),   LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("absolution_dagger"),   LiberEntryDefinitions.THE_UNSTAINED);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("cleansed_blood_crystal_shard"),  LiberEntryDefinitions.PURIFIED);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hemolytic_solution"),     LiberEntryDefinitions.IMMACULATUS_HEMOLYTIC_SOLUTION);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("consecrated_syringe"),    LiberEntryDefinitions.IMMACULATUS_HEMOLYTIC_SOLUTION);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("cleansing_hemolymph"),    LiberEntryDefinitions.IMMACULATUS_HEMOLYTIC_SOLUTION);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("consecrated_copper_ingot"),  LiberEntryDefinitions.IMMACULATUS_COPPER_AND_SILVER);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("book_of_observances"),       LiberEntryDefinitions.IMMACULATUS_OBSERVANCES);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("stillwater_condenser"),      LiberEntryDefinitions.IMMACULATUS_STILLWATER_CONDENSER);

		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("tears_of_silthmere"),  LiberEntryDefinitions.IMMACULATUS_SHE_WHO_LISTENS);

		// Core loop pages (core-loop Track 11): the item that teaches each one opens it.
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_lignum_mortis"), LiberEntryDefinitions.LIGNUM_MORTIS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_canopy_mortis"), LiberEntryDefinitions.LIGNUM_MORTIS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_worked_lignum"), LiberEntryDefinitions.LIGNUM_MORTIS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_pyretic_forge"), LiberEntryDefinitions.PYRETIC_FORGE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_tendon_line"), LiberEntryDefinitions.TENDON_LINE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_blood_binding"), LiberEntryDefinitions.BLOOD_BINDING);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_lingering_blood_binding"), LiberEntryDefinitions.BLOOD_BINDING);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_chain_blood_binding"), LiberEntryDefinitions.BLOOD_BINDING);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_blood_lattice"), LiberEntryDefinitions.BLOOD_BINDING);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_vascular_dowsing"), LiberEntryDefinitions.GROUND_SCULPTINGS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_hematic_beacon"), LiberEntryDefinitions.GROUND_SCULPTINGS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_blood_eclipse"), LiberEntryDefinitions.GROUND_SCULPTINGS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_sanguine_magnetism"), LiberEntryDefinitions.GROUND_SCULPTINGS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_blood_lash"), LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_searing_lash"), LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_hematic_flare"), LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_blood_eclipse"), LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_glacial_grasp"), LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_hemorrhage"), LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_blood_lash"), LiberEntryDefinitions.STRAND_FORMS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_searing_lash"), LiberEntryDefinitions.STRAND_FORMS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_hematic_flare"), LiberEntryDefinitions.STRAND_FORMS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_glacial_grasp"), LiberEntryDefinitions.STRAND_FORMS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_hemorrhage"), LiberEntryDefinitions.STRAND_FORMS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_lode_draw"), LiberEntryDefinitions.MINING_SCULPTINGS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_hematic_hewing"), LiberEntryDefinitions.MINING_SCULPTINGS);
		for (String memory : java.util.List.of("memory_sanguine_mending", "memory_crimson_flame_conjuration", "memory_furnace_veins",
				"memory_carrion_communion")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(memory), LiberEntryDefinitions.SECOND_SCULPTINGS);
		}
		for (String memory : java.util.List.of("memory_glacial_grasp", "memory_glacial_rampart", "memory_ferric_rampart")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(memory), LiberEntryDefinitions.SECOND_SCULPTINGS_WALLS);
		}
		for (String memory : java.util.List.of("memory_gloam_laceration", "memory_cruor_javelin", "memory_blood_needle_lance",
				"memory_blood_needle_fan", "memory_synaptic_jolt", "memory_prismatic_reproof", "memory_crimson_flame_conjuration",
				"memory_ferric_spikes", "memory_conductive_mark", "memory_insatiable_hunger", "memory_grave_debt")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(memory), LiberEntryDefinitions.SECOND_STRANDS);
		}
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("living_staff"), LiberEntryDefinitions.THE_STAFF_GROWS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("living_pickaxe"), LiberEntryDefinitions.THE_STAFF_GROWS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("iron_brazier"), LiberEntryDefinitions.BRAZIER_TRANSMUTATION);
		// The memories whose selection decides what the brazier makes (round two P9: learning one opens the page too).
		for (String memory : java.util.List.of("memory_pyretic_forge", "memory_glacial_grasp", "memory_vital_effusion")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(memory), LiberEntryDefinitions.BRAZIER_TRANSMUTATION);
		}
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("specimen_vat"), LiberEntryDefinitions.MENAGERIE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("specimen_vat"), LiberEntryDefinitions.THE_BROOD);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("tainted_meat"), LiberEntryDefinitions.MENAGERIE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bloody_vial"), LiberEntryDefinitions.UNREAD_HUMOURS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("venous_basin"), LiberEntryDefinitions.LIVING_VEINS_AND_BASINS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("catalyst_feeder"), LiberEntryDefinitions.LIVING_VEINS_AND_BASINS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("catalyst_feeder"), LiberEntryDefinitions.THE_GROUND_NETWORK);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("gourd_seeds"), LiberEntryDefinitions.FED_GROUND);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("planting_basin"), LiberEntryDefinitions.FED_GROUND);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_vital_effusion"), LiberEntryDefinitions.FED_GROUND);
		for (String item : java.util.List.of("vivacious_spores", "fervent_spores", "ruinous_spores", "neurotic_spores", "frigid_spores",
				"ferric_spores", "incandescent_spores", "umbral_spores", "ravenous_heartwood", "salt_stained_timber", "glowroot", "emberpod",
				"tainted_harvest")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(item), LiberEntryDefinitions.STRAIN_BEDS);
		}
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bloody_flask"), LiberEntryDefinitions.LIVING_BLOOD);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("vascular_status_gauge"), LiberEntryDefinitions.LIVING_BLOOD);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("humoral_barometer"), LiberEntryDefinitions.THE_WORLD_ANSWERS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("ossuary_clock"), LiberEntryDefinitions.THE_WORLD_ANSWERS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("suspended_vivianite"), LiberEntryDefinitions.THE_WORLD_ANSWERS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_living_land"), LiberEntryDefinitions.LIVING_LAND);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("memory_venous_cling"), LiberEntryDefinitions.VENOUS_CLING);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hematic_cleats"), LiberEntryDefinitions.VENOUS_CLING);
		// ... and each core-loop first opens the page about what was just done.
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_SCULPTED, LiberEntryDefinitions.THE_TWO_VERBS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_SET_THROUGH, LiberEntryDefinitions.LIVING_BLOOD);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CARRIED, LiberEntryDefinitions.TEMPER);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_KEPT, LiberEntryDefinitions.MENAGERIE);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIRST_BROOD, LiberEntryDefinitions.THE_BROOD);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIRST_STRAIN_BED, LiberEntryDefinitions.STRAIN_BEDS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CUT_AND_LIT, LiberEntryDefinitions.THE_GROUND_NETWORK);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIRST_RING, LiberEntryDefinitions.THE_STAFF_GROWS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIRST_CRACK, LiberEntryDefinitions.STRANDS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIRST_CRACK, LiberEntryDefinitions.STRAND_FORMS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CLINGING, LiberEntryDefinitions.VENOUS_CLING);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_MEAT_IS_BLOOD, LiberEntryDefinitions.THE_BODY_AND_THE_DAY);
		// Round two P9's firsts, each on the page about what was just done.
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIRST_DRIP, LiberEntryDefinitions.FED_GROUND);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_BASIN_HARVEST, LiberEntryDefinitions.FED_GROUND);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_TAINTED_HARVEST, LiberEntryDefinitions.STRAIN_BEDS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_TAINTED_HARVEST, LiberEntryDefinitions.FED_GROUND);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CHUM_CATCH, LiberEntryDefinitions.CHUM);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_IN_GLASS, LiberEntryDefinitions.MENAGERIE);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_STUDIED, LiberEntryDefinitions.UNREAD_HUMOURS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_LAND_STRIKE, LiberEntryDefinitions.LIVING_LAND);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FULL_NIGHT, LiberEntryDefinitions.THE_BODY_AND_THE_DAY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_MOON_BROUGHT_ON, LiberEntryDefinitions.THE_WORLD_ANSWERS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_MOON_BROUGHT_ON, LiberEntryDefinitions.BLOOD_MOONS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CARRION_DRAWN, LiberEntryDefinitions.SECOND_SCULPTINGS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_TWIN_STRAND, LiberEntryDefinitions.SECOND_STRANDS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_STAIN_RUN, LiberEntryDefinitions.SECOND_STRANDS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_BOUND_AND_BRANDED, LiberEntryDefinitions.BLOOD_BINDING);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FIELD_OF_DARK, LiberEntryDefinitions.GROUND_SCULPTINGS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CARRIED_CROSSING, LiberEntryDefinitions.LIVING_VEINS_AND_BASINS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_STOKED, LiberEntryDefinitions.THE_GROUND_NETWORK);

		initOlderPageUnlocks();

		// DS-9: the Armature and each craft's page open when what they describe is picked up.
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hematic_armature"), LiberEntryDefinitions.ARMATURE_RACK);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_ARTIFICER_ARMATURE_PLACED, LiberEntryDefinitions.ARMATURE_RACK);
		for (LiberEntryDefinitions.CraftPage craft : LiberEntryDefinitions.CRAFT_PAGES) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(craft.item()), craft.page());
		}

		// ── Advancement → page unlocks ────────────────────────────────────────
		// Harbinger degree chain
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE,  LiberEntryDefinitions.HEMOMANCY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE,  LiberEntryDefinitions.THE_HARBINGERS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY,    LiberEntryDefinitions.DEGREES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_3_INITIATE,  LiberEntryDefinitions.ORDER_BELIEFS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT,     LiberEntryDefinitions.HISTORICAL_RECORD);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS, LiberEntryDefinitions.HERMITS);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED, LiberEntryDefinitions.DEGREES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON,    LiberEntryDefinitions.ENTITY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_8_APOTHEOS,  LiberEntryDefinitions.TRUTH);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_DEGREE_8_SILENTIUM, LiberEntryDefinitions.TRUTH);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON, LiberEntryDefinitions.SCAR_PRACTICE);
		// Harbinger milestone advancements
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND,                LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED,     LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED,  LiberEntryDefinitions.BLOOD_MEMORIES);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_VOICES_IN_THE_VEIN,            LiberEntryDefinitions.ENTITY);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_ETERNAL_COVENANT_SEALED,       LiberEntryDefinitions.TRUTH);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_SANGUINE_DOMAIN,               LiberEntryDefinitions.BLOOD_MEMORIES);
		// Unstained purity stages
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_BLESSED_BY_ALTAR,  LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_TAINTED,           LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLEANSING,         LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_ABSOLVED,          LiberEntryDefinitions.THE_UNSTAINED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED,  LiberEntryDefinitions.PURIFIED);
		// Unstained clarity stages
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_DISCERNING,        LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_VIGILANT,          LiberEntryDefinitions.IMMACULATUS_CLARITY_PRICE);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_RESOLUTE_STAGE,    LiberEntryDefinitions.PURIFIED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_ENLIGHTENED_SEEKER, LiberEntryDefinitions.PURIFIED);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_PURIFIED,          LiberEntryDefinitions.PURIFIED);
	}

	/**
	 * The older pages' earlier openers (CQ-1, core-loop round two P9). Each also has a Degree floor in
	 * {@link LiberKnowledgeHelper}, since an item opens a page only when picked up off the ground.
	 */
	private static void initOlderPageUnlocks() {
		// Manipulations: a memory in hand or the first Sculpting opens the overview; a school's enzyme, and mastering it,
		// opens that school's page in both the Manipulations and the Temperament chapters (its spores the latter too).
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hematic_memory"), LiberEntryDefinitions.MANIPULATIONS_OVERVIEW);
		for (String crude : List.of("blood_rush", "blood_shot", "deadly_gaze", "glacial_grasp", "hematic_flare", "hemorrhage",
				"sanguine_ignition", "sanguine_mending", "void_shroud")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("crude_memory_" + crude), LiberEntryDefinitions.MANIPULATIONS_OVERVIEW);
		}
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_SCULPTED, LiberEntryDefinitions.MANIPULATIONS_OVERVIEW);
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			String enzyme = BuiltInRegistries.ITEM.getKey(EnumBloodTemper.getRepEnzyme(school)).getPath();
			String sign = enzyme.substring(0, enzyme.length() - "_enzyme".length());
			ResourceLocation mastery = Hemomancy.rloc("hemomancy/enzyme_mastery_" + sign);
			for (ResourceLocation page : List.of(LiberEntryDefinitions.manipulationsPage(school), LiberEntryDefinitions.temperamentPage(school))) {
				BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(enzyme), page);
				BookEntryRegistry.registerAdvancementUnlock(mastery, page);
			}
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(sign + "_spores"), LiberEntryDefinitions.temperamentPage(school));
		}
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("bloody_vial"), LiberEntryDefinitions.TEMPERAMENT);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_CARRIED, LiberEntryDefinitions.TEMPERAMENT);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hematic_memory"), LiberEntryDefinitions.MEMORY_WEAVING);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("somatic_loom"), LiberEntryDefinitions.MEMORY_WEAVING);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE, LiberEntryDefinitions.MEMORY_WEAVING);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE, LiberEntryDefinitions.MEMORY_WEAVING);
		for (String saint : List.of("hemorath", "seraphae", "putriciel", "velorum")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hallowed_residuum_" + saint), LiberEntryDefinitions.SAINT_MEMORIES);
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("recollection_" + saint), LiberEntryDefinitions.SAINT_MEMORIES);
		}
		for (String tincture : List.of("sanguine_fists", "laboring_arms", "coursing_legs", "hushed_gait", "predatory_eyes",
				"second_pulse", "enduring_viscera", "carrion_metabolism")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("tincture_" + tincture), LiberEntryDefinitions.THELEMIC_MEMORIES);
		}
		// The Resonant Forge, the bloodwork stations and the Condenser: the station itself, and what it works.
		for (String item : List.of("resonant_forge", "wax_cylinder", "corpse_wax_cylinder")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(item), LiberEntryDefinitions.FORGE_RECORDING);
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(item), LiberEntryDefinitions.FORGE_MAINTENANCE);
		}
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("scarlet_vanity"), LiberEntryDefinitions.BLOODWORK_STATIONS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("fungal_implantation_pylon"), LiberEntryDefinitions.BLOODWORK_STATIONS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("sanguine_condenser"), LiberEntryDefinitions.SANGUINE_CONDENSER);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("sanguine_formation"), LiberEntryDefinitions.SANGUINE_CONDENSER);
		BookEntryRegistry.registerAdvancementUnlock(HarbingerAdvancementGranter.ADV_SET_THROUGH, LiberEntryDefinitions.SANGUINE_CONDENSER);
		// Morphlings: the jar and the polyp, every strain (read from its table, never named here), the cradle.
		for (String item : List.of("morphling_jar", "morphling_polyp", "morphling_incubator")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(item), LiberEntryDefinitions.MORPHLING_CULTIVATION);
		}
		for (String strain : MorphlingStrains.itemPaths()) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(strain), LiberEntryDefinitions.MORPHLING_CULTIVATION);
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(strain), LiberEntryDefinitions.MORPHLING_STRAINS);
		}
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("morphling_cradle"), LiberEntryDefinitions.MORPHLING_CRADLE_AND_NECTAR);
		for (String item : List.of("chitinous_husk", "enthralling_filament", "cuttlefish_chromatophores", "barbed_splinter",
				"venous_pinion", "ganglion_cluster")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(item), LiberEntryDefinitions.SHARED_MOB_MATERIALS);
		}
		// Annetta: the scout's notes that name her, and what her fight leaves (her route's start also opens it).
		for (String item : List.of("scout_field_notes", "annettas_sanguis_lancea", "annettas_absolution_dagger")) {
			BookEntryRegistry.registerItemUnlock(Hemomancy.rloc(item), LiberEntryDefinitions.ANNETTA_KNOWLES);
		}

		// Liber Immaculatus: each page's sacred tool, and the purity and clarity stages it describes.
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_PLEDGED_UNSTAINED, LiberEntryDefinitions.IMMACULATUS_STAGES_OF_PURITY);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_PLEDGED_UNSTAINED, LiberEntryDefinitions.IMMACULATUS_LIVING_THE_LIFE);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_BLESSED_BY_ALTAR, LiberEntryDefinitions.IMMACULATUS_LIVING_THE_LIFE);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_TAINTED, LiberEntryDefinitions.IMMACULATUS_WHAT_PURITY_GRANTS);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_TAINTED, LiberEntryDefinitions.IMMACULATUS_VIRID_SALIS);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLEANSING, LiberEntryDefinitions.IMMACULATUS_LETHEAN_GIFTS);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLEANSING, LiberEntryDefinitions.IMMACULATUS_WHITE_HUMOR);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_PURIFIED, LiberEntryDefinitions.IMMACULATUS_STAGES_OF_CLARITY);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED, LiberEntryDefinitions.IMMACULATUS_STAGES_OF_CLARITY);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_CLARITY_AWAKENED, LiberEntryDefinitions.IMMACULATUS_STILL_ARTS);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_DISCERNING, LiberEntryDefinitions.IMMACULATUS_STILL_ARTS);
		BookEntryRegistry.registerAdvancementUnlock(UnstainedAdvancementGranter.ADV_RESOLUTE_STAGE, LiberEntryDefinitions.IMMACULATUS_PALE_INTERCESSION);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("ghost_pipe"), LiberEntryDefinitions.IMMACULATUS_GHOST_PIPE);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("virid_salis_trail"), LiberEntryDefinitions.IMMACULATUS_VIRID_SALIS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("pale_humor_flask"), LiberEntryDefinitions.IMMACULATUS_WHITE_HUMOR);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("lethean_dew"), LiberEntryDefinitions.IMMACULATUS_LETHEAN_GIFTS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("lethean_poppy"), LiberEntryDefinitions.IMMACULATUS_LETHEAN_GIFTS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("pale_silver_bell"), LiberEntryDefinitions.IMMACULATUS_LETHEAN_GIFTS);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("pallid_icon"), LiberEntryDefinitions.IMMACULATUS_PALE_INTERCESSION);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("hemolytic_plating"), LiberEntryDefinitions.IMMACULATUS_STAGES_OF_PURITY);
		BookEntryRegistry.registerItemUnlock(Hemomancy.rloc("pale_silver_ingot"), LiberEntryDefinitions.IMMACULATUS_STAGES_OF_CLARITY);
	}

	@SubscribeEvent
	public static void onItemPickup(ItemEntityPickupEvent.Post event) {
		if (!(event.getPlayer() instanceof ServerPlayer player)) return;
		unlockForItem(player, event.getOriginalStack());
	}

	/** Unlocks what picking {@code stack} up would, for items handed straight into an inventory (a Saint's reward). */
	public static void unlockForItem(ServerPlayer player, ItemStack stack) {
		if (stack.isEmpty()) return;
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		Set<ResourceLocation> entries = BookEntryRegistry.entriesForItem(itemId);
		if (!entries.isEmpty()) {
			LiberKnowledgeHelper.unlockEntries(player, entries, CommonDiscoverySource.ITEM_PICKUP);
		}
	}

	@SubscribeEvent
	public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		Set<ResourceLocation> entries = BookEntryRegistry.entriesForAdvancement(event.getAdvancement().id());
		if (!entries.isEmpty()) {
			LiberKnowledgeHelper.unlockEntries(player, entries, CommonDiscoverySource.ADVANCEMENT);
		}
	}
}

