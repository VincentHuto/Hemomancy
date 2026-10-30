package com.vincenthuto.hemomancy.common.init;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * The groups inside {@link CreativeTabSection#HEMATIC}, in display order; each starts on a fresh row of the tab. A
 * group lists its entries in the order they show: progression order, and each station followed by its upgrades.
 * Blood memories need not be listed: any not named here join {@link #MEMORIES} after the named ones, sorted by
 * manipulation rank. Anything else unlisted is not Hematic and falls to {@link CreativeTabSection#MISC}.
 */
public enum CreativeTabGroup {
	GUIDES(List.of(
			"liber_sanguinum", "harbinger_assignment_ledger", "book_of_observances", "scout_field_notes",
			"mnemonic_blueprint", "mnemonic_folio", "engram_stamp", "stained_church_map", "masons_respite_map",
			"covenant_waybill", "heart_pattern", "veins_pattern")),
	BLOOD_TOOLS(List.of(
			"blood_absorption", "blood_projection", "living_syringe", "bloody_vial", "cleansing_hemolymph", "vial_rack",
			"bloody_flask", "bloody_jug", "blood_gourd_white", "blood_gourd_red", "blood_gourd_black","curved_horn",
			"hematic_microscope", "scrying_dish", "hearty_compass", "sanguine_conduit", "charm_of_vascularium",
			"terrestrial_speculum", "sporitic_thurible", "mind_spike", "listening_scar", "wax_cylinder",
			"ambergris_cylinder")),
	STATIONS(List.of(
			"gourd", "vial_centrifuge", "centrifugal_governor_kit", "fractionating_rotor_kit",
			"phlebotomists_field_case", "phlebotomists_cabinet", "ghastly_alembic", "hematic_condenser_kit",
			"sanguine_athanor_kit", "somatic_loom", "ganglionic_harness", "cortical_crown", "mnemonic_reliquary",
			"clairaudiograph", "antecedent_vessel", "scar_station", "mason_effigy", "iron_brazier", "vacant_effigy",
			"sanguine_condenser", "clotting_lattice", "druse_heart", "scarlet_vanity", "morphling_incubator",
			"specimen_jar", "enzymatic_scriptorium", "rubricators_quill", "palimpsest_burin", "resonant_forge",
			"precision_governor_kit", "master_cam_kit", "hematic_armature", "vicars_consecration_kit",
			"monolithic_cornerstone", "consecrated_bloodwell", "cardinal_focus", "covenant_throne",
			"sanguine_monolith", "scrying_podium", "mycelial_crucible", "mycelial_lantern", "fungal_podium",
			"fungal_implantation_pylon", "dendritic_distributor", "mortal_display", "earthen_vein", "hematic_stake",
			"warp_chair", "harbinger_escritoire", "dictation_table", "humoral_barometer", "mnemonic_candle",
			"hematic_suture_node", "blood_echo_inscription", "rite_fragment_inscription")),
	MEMORIES(List.of(
			"hematic_memory", "faded_memory", "bound_mnemonic_remnant", "crude_memory_blood_shot",
			"crude_memory_blood_rush", "crude_memory_deadly_gaze", "crude_memory_sanguine_mending",
			"crude_memory_hemorrhage", "crude_memory_glacial_grasp", "crude_memory_sanguine_ignition",
			"crude_memory_void_shroud", "crude_memory_hematic_flare")),
	SCARS(List.of(
			"scar_blank", "runic_motif_paper", "scar_pattern", "rhizovitta_communis", "talaromyces_minus",
			"noctifly_agaric", "antiphonomyces_resonans", "putrivora_resolvens", "oculiflora_reticularis",
			"cryostroma_perdurans", "saprovitta_vestigium", "hyphal_substrate", "immature_fungal_scar",
			"vivacious_spores", "fervent_spores", "neurotic_spores", "incandescent_spores", "ruinous_spores",
			"frigid_spores", "ferric_spores", "umbral_spores", "scar_heart", "scar_marrow", "scar_phoenix", "scar_pyre",
			"scar_sol", "scar_corona", "scar_feral", "scar_flux", "scar_chimera", "scar_halo", "scar_veil",
			"scar_transcendence", "scar_blight", "scar_wither", "scar_oblivion", "scar_rime", "scar_glacier",
			"scar_descendence", "scar_thorn", "scar_anvil", "scar_crucible", "scar_blood_honed", "scar_shade",
			"scar_moon", "scar_eye")),
	ALCHEMY(List.of(
			"enzyme_primer", "vivacious_enzyme", "ruinous_enzyme", "neurotic_enzyme", "ferric_enzyme",
			"fervent_enzyme", "frigid_enzyme", "incandescent_enzyme", "umbral_enzyme", "recycled_enzyme",
			"cured_clay_jug", "tincture_sanguine_fists", "tincture_laboring_arms", "tincture_coursing_legs",
			"tincture_hushed_gait", "tincture_predatory_eyes", "tincture_second_pulse", "tincture_enduring_viscera",
			"tincture_carrion_metabolism", "tincture_sanguine_fists_jug", "tincture_laboring_arms_jug",
			"tincture_coursing_legs_jug", "tincture_hushed_gait_jug", "tincture_predatory_eyes_jug",
			"tincture_second_pulse_jug", "tincture_enduring_viscera_jug", "tincture_carrion_metabolism_jug",
			"sanguine_salve", "morphic_tonic", "foul_paste", "ferric_binder", "crimson_lacquer", "sclerotic_oleum",
			"aculeate_vitriol", "chromatic_sublimate")),
	MORPHLINGS(List.of(
			"morphling_jar", "morphling_polyp", "morphling_deadmans_purse", "morphling_gravecap",
			"morphling_witchs_ear", "morphling_lumenlace", "morphling_bootlace", "morphling_irontooth",
			"morphling_emberfang", "morphling_winter_shroud")),
	ARSENAL(List.of(
			"living_staff", "worn_vow_fitting", "barbed_fitting", "chitinite_fitting", "prismatic_fitting",
			"crimson_vestment_fitting", "monolithic_frame_fitting", "assumed_limb_fitting", "living_weapon_graft",
			"living_blade", "living_axe", "living_spear", "living_baghnakh", "living_crossbow", "living_torch",
			"living_flail", "hematic_iron_knapper", "vivianite_scalpel", "hematic_iron_sword", "barbed_blade",
			"barbed_shield", "chitinite_mace", "chitinite_shield", "chitinite_arm_banner", "sanguis_lancea",
			"annettas_sanguis_lancea")),
	ARMOR(List.of(
			"hematic_iron_helm", "hematic_iron_chestplate", "hematic_iron_leggings", "hematic_iron_boots",
			"barbed_helm", "barbed_chestplate", "barbed_leggings", "barbed_boots", "chitinite_helm",
			"chitinite_chestplate", "chitinite_leggings", "chitinite_boots", "prismatic_helm", "prismatic_chestplate",
			"prismatic_leggings", "prismatic_boots", "hemolymphopoda_headpiece", "lantern_tick_helmet",
			"venous_strider_sabatons", "marrow_crown", "blood_lust_helm", "blood_lust_chest", "blood_lust_legs",
			"blood_lust_boots", "tengu_mask", "blood_lust_helm_tengu", "grinning_mask", "blood_lust_helm_grinning",
			"lodestone_faceplate", "blood_lust_helm_lodestone", "blood_lust_helm_velorum", "edacious_blood_lust_helm",
			"edacious_blood_lust_chest", "edacious_blood_lust_legs", "edacious_blood_lust_boots",
			"sheolic_blood_lust_helm", "sheolic_blood_lust_chest", "sheolic_blood_lust_legs",
			"sheolic_blood_lust_boots", "phantasmal_blood_lust_helm", "phantasmal_blood_lust_chest",
			"phantasmal_blood_lust_legs", "phantasmal_blood_lust_boots", "covenant_mantle",
			"vestment_of_the_final_molt")),
	DROPS(List.of(
			"swollen_leech", "dried_leech", "serpent_scale", "chitinous_husk", "fervent_husk", "toxicognath",
			"fargone_proboscis", "telson",  "venous_pinion", "spore_sac", "queens_physogastrism",
			"calcified_blood_spine", "ganglion_cluster", "naeglerophaeon_ganglion")),
	MATERIALS(List.of(
			"sanguine_formation", "blood_crystal_shard", "sanguine_quintessence", "hematic_iron_scrap",
			"hematic_iron_powder", "vivianite_cluster", "adipocere", "blood_rock", "blood_stained_stone", "bleeding_bulb",
			"dried_gourd", "constrictor_cord", "scale_grip")),
	FOOD(List.of(
			"gourd_seeds", "gourd_slice", "gourd_stew", "raw_venison", "cooked_venison")),
	BUILDING(List.of(
			"venous_stone", "venous_stone_stairs", "venous_stone_slab", "venous_stone_wall", "polished_venous_stone",
			"polished_venous_stone_stairs", "polished_venous_stone_slab", "polished_venous_stone_wall",
			"chiseled_polished_venous_stone", "polished_venous_stone_bricks", "polished_venous_stone_brick_stairs",
			"polished_venous_stone_brick_slab", "polished_venous_stone_brick_wall",
			"cracked_polished_venous_stone_bricks", "gilded_venous_stone", "infested_venous_stone",
			"hematic_iron_block", "chiseled_hematic_iron_block", "hematic_iron_pillar", "hematic_iron_plating",
			"hematic_iron_plating_stairs", "hematic_iron_plating_slab", "hematic_iron_plating_wall",
			"hematic_iron_bars", "hematic_iron_door", "hematic_iron_trapdoor", "hematic_iron_chain",
			"hematic_lantern", "blood_wood_log", "blood_wood_planks", "blood_wood_leaves", "infested_wood",
			"sanguine_glass", "sanguine_pane", "vivianite_glass", "vivianite_pane", "chitinite_block",
			"sclerite_block", "organic_prism", "frozen_cruor")),
	NATURAL(List.of(
			"hematic_iron_ore", "bog_iron", "blood_crystal", "suspended_blood_crystal", "suspended_vivianite",
			"bog_body", "bleeding_heart", "stinkhorn_fungus", "devils_tooth", "sarcodes", "rafflesia",
			"crimson_flames", "smouldering_ash_trail", "befouling_ash_trail")),
	/** Spawn eggs; they always close the section, in this order. */
	CREATURES(List.of(
			"spawn_egg_leech", "spawn_egg_chitinite", "spawn_egg_fervent_chitinite", "spawn_egg_hemolymphopoda",
			"spawn_egg_hematic_burrower", "spawn_egg_crimson_doe", "spawn_egg_venous_strider",
			"spawn_egg_scarlet_serpent", "spawn_egg_desiccant", "spawn_egg_venom_rib_centipede",
			"spawn_egg_luminal_cicada", "spawn_egg_vampire_bat", "spawn_egg_lantern_tick", "spawn_egg_tooth_pecks",
			"spawn_egg_morphling_polyp", "spawn_egg_bog_revenant", "spawn_egg_thirster", "spawn_egg_fargone",
			"spawn_egg_chthonian", "spawn_egg_chthonian_queen", "spawn_egg_mortarbound", "spawn_egg_naeglerophaeon",
			"spawn_egg_will", "spawn_egg_harbinger_artificer"));

	private record Place(CreativeTabGroup group, int index) {
	}

	private static final Map<ResourceLocation, Place> PLACES = new HashMap<>();

	static {
		for (CreativeTabGroup group : values()) {
			for (int i = 0; i < group.paths.size(); i++) {
				Place previous = PLACES.put(Hemomancy.rloc(group.paths.get(i)), new Place(group, i));
				if (previous != null) {
					throw new IllegalStateException(group.paths.get(i) + " is listed in both " + previous.group
							+ " and " + group);
				}
			}
		}
	}

	private final List<String> paths;

	CreativeTabGroup(List<String> paths) {
		this.paths = paths;
	}

	public List<String> paths() {
		return paths;
	}

	/** The item's Hematic group, or null if it is not a Hematic entry. */
	public static CreativeTabGroup of(Item item) {
		Place place = PLACES.get(BuiltInRegistries.ITEM.getKey(item));
		if (place != null) {
			return place.group;
		}
		return item instanceof BloodMemoryItem ? MEMORIES : null;
	}

	/**
	 * Where an item sorts inside its group: listed entries by their place in the list, then unlisted blood memories
	 * by manipulation rank. Ties keep registry order.
	 */
	static int orderWithinGroup(Item item) {
		Place place = PLACES.get(BuiltInRegistries.ITEM.getKey(item));
		if (place != null) {
			return place.index;
		}
		if (item instanceof BloodMemoryItem memory) {
			return MEMORIES.paths.size() + memory.getManip().getRank().ordinal();
		}
		return 0;
	}
}
