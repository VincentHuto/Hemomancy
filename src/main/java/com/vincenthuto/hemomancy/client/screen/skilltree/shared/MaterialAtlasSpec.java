package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MaterialAtlasSpec {
	private static final int HARBINGER_HUB_X = 686;
	private static final int HARBINGER_HUB_Y = 617;
	private static final int HARBINGER_HUB_LABEL_X = 590;
	private static final int HARBINGER_HUB_LABEL_Y = 625;
	private static final int UNSTAINED_HUB_X = 495;
	private static final int UNSTAINED_HUB_Y = 522;
	private static final int UNSTAINED_HUB_LABEL_X = 495;
	private static final int UNSTAINED_HUB_LABEL_Y = 522;

	private static final EnumMap<MaterialAtlasPath, List<MaterialAtlasBucket>> BUCKETS =
			new EnumMap<>(MaterialAtlasPath.class);
	private static final EnumMap<MaterialAtlasPath, LinkedHashMap<String, MaterialAtlasEntry>> ENTRIES =
			new EnumMap<>(MaterialAtlasPath.class);

	static {
		for (MaterialAtlasPath path : MaterialAtlasPath.values()) {
			BUCKETS.put(path, new ArrayList<>());
			ENTRIES.put(path, new LinkedHashMap<>());
		}
		registerBuckets();
		registerHarbingerEntries();
		registerUnstainedEntries();
		validateParentIds();
	}

	private MaterialAtlasSpec() {
	}

	public static Collection<MaterialAtlasBucket> buckets(MaterialAtlasPath path) {
		return BUCKETS.getOrDefault(path, List.of());
	}

	public static Collection<MaterialAtlasEntry> entries(MaterialAtlasPath path) {
		return List.copyOf(ENTRIES.getOrDefault(path, new LinkedHashMap<>()).values());
	}

	public static MaterialAtlasEntry entryFor(MaterialAtlasPath path, MaterialEntry material) {
		MaterialAtlasEntry entry = ENTRIES.getOrDefault(path, new LinkedHashMap<>()).get(material.name());
		if (entry != null) {
			return entry;
		}
		MaterialAtlasBucket fallback = BUCKETS.get(path).isEmpty()
				? new MaterialAtlasBucket(path, "unknown", "Unknown",
						path.accentColor(), hubX(path), hubY(path), hubX(path), hubY(path))
				: BUCKETS.get(path).getFirst();
		return new MaterialAtlasEntry(path, material.name(), fallback, MaterialGate.always(), Integer.MAX_VALUE, List.of(),
				null, null);
	}

	public static int hubX(MaterialAtlasPath path) {
		return path == MaterialAtlasPath.UNSTAINED ? UNSTAINED_HUB_X : HARBINGER_HUB_X;
	}

	public static int hubY(MaterialAtlasPath path) {
		return path == MaterialAtlasPath.UNSTAINED ? UNSTAINED_HUB_Y : HARBINGER_HUB_Y;
	}

	public static int hubLabelX(MaterialAtlasPath path) {
		return path == MaterialAtlasPath.UNSTAINED ? UNSTAINED_HUB_LABEL_X : HARBINGER_HUB_LABEL_X;
	}

	public static int hubLabelY(MaterialAtlasPath path) {
		return path == MaterialAtlasPath.UNSTAINED ? UNSTAINED_HUB_LABEL_Y : HARBINGER_HUB_LABEL_Y;
	}

	private static void registerBuckets() {
		bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", 0xFFD04436, 615, 500, 477, 625);
		bucket(MaterialAtlasPath.HARBINGER, "vascular_craft", "Vascular Craft", 0xFFB64044, 660, 220, 623, 85);
		bucket(MaterialAtlasPath.HARBINGER, "alchemy_enzymes", "Alchemy & Enzymes", 0xFFD99B2D, 360, 350, 885, 1123);
		bucket(MaterialAtlasPath.HARBINGER, "fungal_ecology", "Botany and Mycology", 0xFF714D41, 850, 610, 860, 837);
		bucket(MaterialAtlasPath.HARBINGER, "morphlings", "Morphlings", 0xFFB45BA5, 760, 875, 1192, 1141);
		bucket(MaterialAtlasPath.HARBINGER, "scars_patterns", "Scars & Patterns", 0xFF6FC2D8, 660, 1260, 432, 64);
		bucket(MaterialAtlasPath.HARBINGER, "living_implements", "Living Implements", 0xFFC58B35, 280, 850, 195, 900);
		bucket(MaterialAtlasPath.HARBINGER, "architecture", "Architecture", 0xFF9A6A45, 470, 615, 458, 790);
		bucket(MaterialAtlasPath.HARBINGER, "new_category", "Biomaterials", 0xFF19711A, 1050, 390, 916, 320);
		bucket(MaterialAtlasPath.HARBINGER, "gourds_vessels", "Gourds & Vessels", 0xFFC78539, 240, 590, 760, 457);
		bucket(MaterialAtlasPath.HARBINGER, "spores_cultures", "Spores & Cultures", 0xFF8EA441, 1065, 805, 1310, 843);
		bucket(MaterialAtlasPath.HARBINGER, "myco_realm_blocks", "Myco-Realm Blocks", 0xFF7B8F4D, 1045, 590, 1137, 594);
		bucket(MaterialAtlasPath.HARBINGER, "masks_vestments", "Masks & Vestments", 0xFFC15872, 560, 930, 482, 1019);
		bucket(MaterialAtlasPath.HARBINGER, "idols_fixtures", "Idols & Fixtures", 0xFF9E7A58, 815, 365, 642, 855);
		bucket(MaterialAtlasPath.HARBINGER, "qliphoth_reagents", "Qliphoth Reagents", 0xFF7F4FA2, 925, 150, 820, 25);
		bucket(MaterialAtlasPath.UNSTAINED, "still_waters_core", "Still Waters Core", 0xFF80B0A0, 520, 230, 520, 170);
		bucket(MaterialAtlasPath.UNSTAINED, "cleansing_facilities", "Cleansing Facilities", 0xFF8FB8D8, 790, 360, 870, 330);
		bucket(MaterialAtlasPath.UNSTAINED, "lethean_flora", "Lethean Flora", 0xFFA6C58A, 760, 720, 840, 760);
		bucket(MaterialAtlasPath.UNSTAINED, "pale_architecture", "Pale Architecture", 0xFFD5D0B6, 520, 850, 520, 940);
		bucket(MaterialAtlasPath.UNSTAINED, "anti_blood_warding", "Anti-Blood Warding", 0xFFB7C6CE, 250, 720, 145, 760);
		bucket(MaterialAtlasPath.UNSTAINED, "vestments_instruments", "Vestments & Instruments", 0xFFB09AC8, 250, 360, 120, 330);
	}

	private static void registerHarbingerEntries() {
		MaterialAtlasPath h = MaterialAtlasPath.HARBINGER;
		entryAt("sanguine_formation", h, "bloodcraft_core", a(), 498, 520, "mortal_display");
		entryAt("blood_crystal_shard", h, "bloodcraft_core", a(), 533, 470, "sanguine_formation");
		entryAt("blood_rock", h, "bloodcraft_core", a(), 402, 480);
		entryAt("blood_stained_stone", h, "bloodcraft_core", a(), 463, 430, "mortal_display");
		entryAt("hematic_iron_scrap", h, "bloodcraft_core", a(), 340, 776, "hematic_iron_ore");
		entryAt("hematic_iron_powder", h, "alchemy_enzymes", d(2), 805, 922, "vial_centrifuge");
		entryAt("vivianite_cluster", h, "bloodcraft_core", d(2), 655, 358, "blood_crystal_shard", "bog_body");
		entryAt("hematic_iron_block", h, "bloodcraft_core", d(2), 477, 824, "hematic_iron_scrap");
		entryAt("hematic_iron_pillar", h, "architecture", d(2), 520, 824, "hematic_iron_block");
		entryAt("sanguine_conduit", h, "bloodcraft_core", d(2), 498, 470, "sanguine_formation");
		entryAt("blood_crystal_block", h, "bloodcraft_core", d(3), 533, 430, "blood_crystal_shard");
		entryAt("suspended_vivianite", h, "bloodcraft_core", d(3), 655, 308, "vivianite_cluster");
		entryAt("sanguine_quintessence", h, "bloodcraft_core", d(5), 463, 385);
		entryAt("iron_brazier", h, "vascular_craft", d(1), 540, 230);
		entryAt("blood_basin", h, "vascular_craft", d(2), 750, 195, "blood_pylon");
		entryAt("earthen_vein", h, "vascular_craft", d(2), 845, 725);
		entryAt("somatic_loom", h, "vascular_craft", d(2), 540, 160, "iron_brazier");
		entryAt("mnemonic_reliquary", h, "vascular_craft", d(2), 540, 105);
		entryAt("blood_pylon", h, "vascular_craft", d(3), 700, 195, "blood_trial_altar");
		entryAt("dictation_table", h, "idols_fixtures", d(3), 659, 883);
		entryAt("mortal_display", h, "vascular_craft", d(3), 463, 470);
		entryAt("scarlet_vanity", h, "vascular_craft", d(3), 580, 230, "sanguine_conduit");
		entryAt("scrying_podium", h, "vascular_craft", d(3), 580, 179, "scarlet_vanity");
		entryAt("dendritic_distributor", h, "vascular_craft", d(4), 540, 135, "mnemonic_reliquary", "somatic_loom");
		entryAt("visceral_mirror", h, "vascular_craft", d(4), 580, 135, "scrying_podium");
		entryAt("consecrated_bloodwell", h, "vascular_craft", d(5), 649, 160);
		entryAt("covenant_throne", h, "vascular_craft", d(5), 744, 125, "sanguine_vigil");
		entryAt("sanguine_vigil", h, "vascular_craft", d(5), 700, 160, "consecrated_bloodwell");
		entryAt("sanguine_monolith", h, "vascular_craft", d(5), 700, 125, "consecrated_bloodwell");
		entryAt("blood_trial_altar", h, "vascular_craft", d(5), 805, 105);
		entryAt("bloody_vial", h, "alchemy_enzymes", a(), 717, 963);
		entryAt("bloody_flask", h, "alchemy_enzymes", a(), 349, 480, "blood_rock", "ghastly_alembic");
		entryAt("bleeding_bulb", h, "fungal_ecology", a(), 915, 444, "bleeding_heart");
		entryAt("blood_chum", h, "new_category", a(), 1000, 444, "bleeding_bulb", "dried_leech");
		entryAt("foul_paste", h, "alchemy_enzymes", a(), 1082, 776, "stinkhorn_fungus", "infected_fungus");
		entryAt("bloody_jug", h, "alchemy_enzymes", d(2), 349, 525, "bloody_flask");
		entryAt("vial_rack", h, "alchemy_enzymes", d(2), 760, 963, "bloody_vial");
		entryAt("blood_gourd_white", h, "alchemy_enzymes", d(2), 697, 484);
		entryAt("ghastly_alembic", h, "alchemy_enzymes", d(2), 349, 435);
		entryAt("vial_centrifuge", h, "alchemy_enzymes", d(2), 805, 963, "vial_rack");
		entryAt("ferric_enzyme", h, "alchemy_enzymes", d(2), 900, 961, "recycled_enzyme");
		entryAt("fervent_enzyme", h, "alchemy_enzymes", d(2), 979, 922, "recycled_enzyme");
		entryAt("frigid_enzyme", h, "alchemy_enzymes", d(2), 900, 998, "recycled_enzyme");
		entryAt("incandescent_enzyme", h, "alchemy_enzymes", d(2), 979, 998, "recycled_enzyme");
		entryAt("neurotic_enzyme", h, "alchemy_enzymes", d(2), 979, 961, "recycled_enzyme");
		entryAt("recycled_enzyme", h, "alchemy_enzymes", d(2), 939, 968, "vial_centrifuge");
		entryAt("ruinous_enzyme", h, "alchemy_enzymes", d(2), 940, 1006, "recycled_enzyme");
		entryAt("umbral_enzyme", h, "alchemy_enzymes", d(2), 900, 922, "recycled_enzyme");
		entryAt("vivacious_enzyme", h, "alchemy_enzymes", d(2), 940, 903, "recycled_enzyme");
		entryAt("aculeate_vitriol", h, "new_category", d(3), 1170, 310, "toxicognath", "telson", "calcified_blood_spine");
		entryAt("chromatic_sublimate", h, "new_category", d(3), 1170, 345, "cuttlefish_chromatophores", "puppeteering_thread", "serpent_scale");
		entryAt("fervent_husk", h, "new_category", d(3), 1220, 275, "sclerotic_oleum");
		entryAt("queens_physogastrism", h, "new_category", d(3), 1055, 275);
		entryAt("sclerotic_oleum", h, "new_category", d(3), 1170, 275, "queens_physogastrism", "chitinous_husk", "chalybeate_sclerite");
		entryAt("telson", h, "new_category", d(3), 1105, 310);
		entryAt("toxicognath", h, "new_category", d(3), 1055, 310);
		entryAt("bleeding_heart", h, "fungal_ecology", a(), 915, 484);
		entryAt("devils_tooth", h, "fungal_ecology", a(), 1157, 725, "erythrocytic_mycelium");
		entryAt("infected_fungus", h, "fungal_ecology", a(), 1052, 725, "erythrocytic_mycelium");
		entryAt("rafflesia", h, "fungal_ecology", a(), 1122, 725, "erythrocytic_mycelium");
		entryAt("sarcodes", h, "fungal_ecology", a(), 1202, 725);
		entryAt("stinkhorn_fungus", h, "fungal_ecology", a(), 1082, 725, "erythrocytic_mycelium");
		entryAt("erythrocytic_dirt", h, "fungal_ecology", a(), 840, 670);
		entryAt("bog_body", h, "new_category", d(2), 595, 358);
		entryAt("blood_wood_log", h, "fungal_ecology", d(3), 840, 614, "erythrocytic_dirt");
		entryAt("blood_wood_planks", h, "fungal_ecology", d(3), 840, 569, "blood_wood_log");
		entryAt("chitinous_husk", h, "new_category", d(3), 1010, 275);
		entryAt("conscious_mass", h, "fungal_ecology", d(3), 1027, 911, "neurotic_enzyme", "vivacious_enzyme");
		entryAt("curved_horn", h, "fungal_ecology", d(3), 744, 484, "blood_gourd_white");
		entryAt("cuttlefish_chromatophores", h, "new_category", d(3), 1105, 345);
		entryAt("erythrocytic_mycelium", h, "fungal_ecology", d(3), 1067, 635, "erythrocytic_dirt");
		entryAt("fargone_proboscis", h, "new_category", d(3), 1220, 310, "aculeate_vitriol");
		entryAt("serpent_scale", h, "new_category", d(3), 1010, 345);
		entryAt("spore_sac", h, "fungal_ecology", d(3), 1157, 649, "erythrocytic_mycelium");
		entryAt("venous_pinion", h, "new_category", d(3), 1105, 240);
		entryAt("hyphae_block", h, "fungal_ecology", d(4), 1002, 635, "erythrocytic_mycelium");
		entryAt("infected_cap", h, "fungal_ecology", d(4), 1010, 761, "infected_fungus");
		entryAt("infected_stem", h, "fungal_ecology", d(4), 1027, 594, "erythrocytic_mycelium");
		entryAt("infested_venous_stone", h, "fungal_ecology", d(4), 775, 670, "erythrocytic_dirt", "venous_stone");
		entryAt("fungal_spine", h, "fungal_ecology", d(4), 1117, 77);
		entryAt("fungal_podium", h, "fungal_ecology", d(4), 1052, 824, "infected_fungus");
		entryAt("mycelial_crucible", h, "fungal_ecology", d(4), 1052, 871, "fungal_podium", "morphling_incubator");
		entryAt("fungal_implantation_pylon", h, "fungal_ecology", d(5), 1132, 883, "fungal_podium", "conscious_mass");
		entryAt("fruiting_infected_cap", h, "fungal_ecology", d(5), 1000, 725, "infected_cap");
		entryAt("morphling_jar", h, "morphlings", d(3), 1082, 1046, "morphling_polyp");
		entryAt("morphling_cradle", h, "morphlings", d(3), 1207, 961, "morphling_incubator");
		entryAt("morphling_incubator", h, "morphlings", d(3), 1082, 961, "morphling_polyp", "recycled_enzyme");
		entryAt("morphling_polyp", h, "morphlings", d(3), 1137, 1021);
		entryAt("morphling_bat", h, "morphlings", d(3), 1237, 1016, "morphling_polyp");
		entryAt("morphling_centipede", h, "morphlings", d(3), 1297, 1131, "morphling_polyp");
		entryAt("morphling_chitinite", h, "morphlings", d(3), 1312, 1096, "morphling_polyp");
		entryAt("morphling_cuttlefish", h, "morphlings", d(3), 1272, 1036, "morphling_polyp");
		entryAt("morphling_fungal", h, "morphlings", d(3), 1082, 1081, "morphling_polyp");
		entryAt("morphling_leeches", h, "morphlings", d(3), 1132, 1141, "morphling_polyp");
		entryAt("morphling_mole", h, "morphlings", d(3), 1312, 1071, "morphling_polyp");
		entryAt("morphling_pests", h, "morphlings", d(3), 1202, 1181, "morphling_polyp");
		entryAt("morphling_serpent", h, "morphlings", d(3), 1107, 1106, "morphling_polyp");
		entryAt("morphling_spider", h, "morphlings", d(3), 1272, 1161, "morphling_polyp");
		entryAt("morphling_tick", h, "morphlings", d(3), 1162, 1171, "morphling_polyp");
		entryAt("morphling_urchin", h, "morphlings", d(3), 1237, 1181, "morphling_polyp");
		entryAt("scar_station", h, "scars_patterns", d(4), 284, 160, "mason_effigy");
		entryAt("scar_blank", h, "scars_patterns", d(4), 317, 160, "scar_station");
		entryAt("scar_blight", h, "scars_patterns", d(4), 294, 220, "scar_station", "scar_blank");
		entryAt("scar_feral", h, "scars_patterns", d(4), 349, 160, "scar_station", "scar_blank");
		entryAt("scar_halo", h, "scars_patterns", d(4), 334, 195, "scar_station", "scar_blank");
		entryAt("scar_heart", h, "scars_patterns", d(4), 294, 90, "scar_station", "scar_blank");
		entryAt("scar_pyre", h, "scars_patterns", d(4), 334, 125, "scar_station", "scar_blank");
		entryAt("scar_rime", h, "scars_patterns", d(4), 249, 195, "scar_station", "scar_blank");
		entryAt("scar_shade", h, "scars_patterns", d(4), 249, 125, "scar_station", "scar_blank");
		entryAt("scar_thorn", h, "scars_patterns", d(4), 229, 160, "scar_station", "scar_blank");
		entryAt("scar_anvil", h, "scars_patterns", d(5), 194, 160, "scar_thorn");
		entryAt("scar_flux", h, "scars_patterns", d(5), 394, 160, "scar_feral");
		entryAt("scar_glacier", h, "scars_patterns", d(5), 214, 220, "scar_rime");
		entryAt("scar_marrow", h, "scars_patterns", d(5), 294, 60, "scar_heart");
		entryAt("scar_moon", h, "scars_patterns", d(5), 214, 100, "scar_shade");
		entryAt("scar_sol", h, "scars_patterns", d(5), 369, 100, "scar_pyre");
		entryAt("scar_veil", h, "scars_patterns", d(5), 369, 220, "scar_halo");
		entryAt("scar_wither", h, "scars_patterns", d(5), 294, 250, "scar_blight");
		entryAt("scar_chimera", h, "scars_patterns", d(6), 424, 160, "scar_flux");
		entryAt("scar_corona", h, "scars_patterns", d(6), 394, 75, "scar_sol");
		entryAt("scar_crucible", h, "scars_patterns", d(6), 164, 160, "scar_anvil");
		entryAt("scar_descendence", h, "scars_patterns", d(6), 184, 250, "scar_glacier");
		entryAt("scar_eye", h, "scars_patterns", d(6), 184, 75, "scar_moon");
		entryAt("scar_oblivion", h, "scars_patterns", d(6), 294, 295, "scar_wither");
		entryAt("scar_phoenix", h, "scars_patterns", d(6), 294, 30, "scar_marrow");
		entryAt("scar_transcendence", h, "scars_patterns", d(6), 404, 250, "scar_veil");
		entryAt("living_syringe", h, "alchemy_enzymes", d(2), 805, 1006, "vial_centrifuge");
		entryAt("puppeteering_thread", h, "new_category", d(2), 1055, 345);
		entryAt("hematic_iron_knapper", h, "living_implements", d(2), 284, 760, "hematic_iron_scrap");
		entryAt("hematic_iron_sword", h, "living_implements", d(2), 284, 796, "hematic_iron_scrap");
		entryAt("barbed_blade", h, "masks_vestments", d(3), 180, 1059, "barbed_chestplate");
		entryAt("living_staff", h, "living_implements", d(3), 340, 700, "hematic_iron_scrap");
		entryAt("chitinite_mace", h, "masks_vestments", d(4), 274, 1128, "chitinite_chestplate");
		entryAt("vivianite_scalpel", h, "living_implements", d(4), 700, 358, "vivianite_cluster");
		entryAt("blood_lust_helm", h, "living_implements", d(5), 420, 1062, "vicars_consecration_kit");
		entryAt("sanguis_lancea", h, "living_implements", d(5), 236, 776);
		entryAt("venous_stone", h, "architecture", a(), 595, 675);
		entryAt("hematic_iron_ore", h, "architecture", a(), 437, 685, "venous_stone");
		entryAt("polished_venous_stone", h, "architecture", d(2), 510, 675, "polished_venous_stone_bricks");
		entryAt("sanguine_glass", h, "architecture", d(2), 533, 520, "blood_crystal_shard");
		entryAt("vivianite_glass", h, "architecture", d(2), 695, 308, "vivianite_cluster");
		entryAt("chiseled_hematic_iron_block", h, "architecture", d(3), 477, 776, "hematic_iron_block");
		entryAt("gilded_venous_stone", h, "architecture", d(3), 510, 635, "venous_stone");
		entryAt("polished_venous_stone_bricks", h, "architecture", d(3), 550, 725, "venous_stone");
		entryAt("mnemonic_ambergris", h, "new_category", d(1), 1220, 345, "chromatic_sublimate");
		entryAt("calcified_blood_spine", h, "new_category", d(1), 1010, 310);
		entryAt("chalybeate_sclerite", h, "new_category", d(1), 1105, 275);
		entryAt("active_befouling_ash", h, "alchemy_enzymes", d(3), 402, 391, "ghastly_alembic");
		entryAt("active_smouldering_ash", h, "alchemy_enzymes", d(3), 284, 391, "ghastly_alembic");
		entryAt("crimson_lacquer", h, "alchemy_enzymes", d(3), 294, 435, "ghastly_alembic");
		entryAt("engram_block", h, "alchemy_enzymes", d(3), 349, 358, "ghastly_alembic");
		entryAt("enzyme_primer", h, "alchemy_enzymes", d(3), 939, 938, "recycled_enzyme");
		entryAt("ferric_binder", h, "alchemy_enzymes", d(3), 860, 922, "ferric_enzyme", "hematic_iron_powder");
		entryAt("qliphoth_bloom", h, "qliphoth_reagents", d(3), 760, 65);
		entryAt("humoral_barometer", h, "idols_fixtures", d(3), 625, 838);
		entryAt("ossuary_clock", h, "idols_fixtures", d(3), 591, 838);
		entryAt("venous_stone_slab", h, "architecture", d(3), 630, 725, "venous_stone");
		entryAt("venous_stone_wall", h, "architecture", d(3), 592, 725, "venous_stone");
		entryAt("liber_sanguinum", h, "bloodcraft_core", a(), 459, 520);
		entryAt("blood_gourd_black", h, "gourds_vessels", d(2), 697, 439, "blood_gourd_white");
		entryAt("blood_gourd_red", h, "gourds_vessels", d(2), 649, 439, "blood_gourd_white");
		entryAt("dried_gourd", h, "gourds_vessels", d(2), 697, 534, "blood_gourd_white", "gourd");
		entryAt("gourd", h, "gourds_vessels", d(2), 697, 574, "gourd_seeds");
		entryAt("gourd_seeds", h, "gourds_vessels", d(2), 697, 614);
		entryAt("gourd_slice", h, "gourds_vessels", d(2), 649, 574, "gourd");
		entryAt("gourd_stew", h, "gourds_vessels", d(2), 649, 534, "gourd_slice");
		entryAt("gourdvine_tap", h, "gourds_vessels", d(2), 649, 484, "blood_gourd_white");
		entryAt("hemorath_rib", h, "gourds_vessels", d(2), 744, 444, "blood_gourd_white");
		entryAt("sanguine_salve", h, "gourds_vessels", d(3), 349, 585, "bloody_jug");
		entryAt("scrying_dish", h, "gourds_vessels", d(3), 402, 525, "bloody_jug");
		entryAt("vascular_poultice", h, "gourds_vessels", d(3), 294, 585, "sanguine_salve");
		entryAt("vitality_chalice", h, "gourds_vessels", d(3), 294, 525, "bloody_jug");
		entryAt("anastomotic_brazier", h, "idols_fixtures", d(4), 437, 295, "iron_brazier", "mason_effigy");
		entryAt("hematic_armature", h, "idols_fixtures", d(4), 265, 1004, "hematic_iron_chestplate");
		entryAt("hematic_iron_chain", h, "architecture", d(4), 540, 883, "hematic_iron_block");
		entryAt("hematic_stake", h, "qliphoth_reagents", d(4), 805, 160);
		entryAt("humane_idol", h, "idols_fixtures", d(4), 591, 883);
		entryAt("mason_effigy", h, "idols_fixtures", d(4), 349, 295);
		entryAt("mnemonic_candle", h, "idols_fixtures", d(4), 659, 838);
		entryAt("mycelial_lantern", h, "spores_cultures", d(4), 1205, 649, "spore_sac");
		entryAt("puppeteers_spindle", h, "vascular_craft", d(4), 1052, 391, "puppeteering_thread");
		entryAt("semi_sentient_construct", h, "idols_fixtures", d(4), 697, 838);
		entryAt("serpentine_idol", h, "idols_fixtures", d(4), 625, 883);
		entryAt("specimen_jar", h, "new_category", d(4), 740, 310, "vivianite_glass");
		entryAt("witness_organ", h, "idols_fixtures", d(4), 697, 883);
		entryAt("annettas_sanguis_lancea", h, "living_implements", d(3), 192, 776, "sanguis_lancea");
		entryAt("sporitic_thurible", h, "living_implements", d(3), 1328, 649, "neurotic_spores", "incandescent_spores", "frigid_spores", "umbral_spores", "vivacious_spores", "fervent_spores", "ferric_spores", "ruinous_spores");
		entryAt("barbed_chestplate", h, "masks_vestments", d(4), 223, 1074, "hematic_armature");
		entryAt("barbed_shield", h, "masks_vestments", d(4), 180, 1094, "barbed_chestplate");
		entryAt("chitinite_chestplate", h, "masks_vestments", d(4), 257, 1074, "hematic_armature");
		entryAt("chitinite_shield", h, "masks_vestments", d(4), 236, 1128, "chitinite_chestplate");
		entryAt("prismatic_chestplate", h, "masks_vestments", d(4), 284, 1074, "hematic_armature");
		entryAt("silent_archon_chestplate", h, "masks_vestments", d(7), 620, 998, "monolithic_cornerstone");
		entryAt("blood_lust_helm_grinning", h, "masks_vestments", d(5), 401, 1139, "grinning_mask");
		entryAt("blood_lust_helm_lodestone", h, "masks_vestments", d(5), 437, 1139, "lodestone_faceplate");
		entryAt("blood_lust_helm_tengu", h, "masks_vestments", d(5), 360, 1139, "tengu_mask");
		entryAt("blood_lust_helm_velorum", h, "masks_vestments", d(5), 477, 1139, "velorum_mask");
		entryAt("edacious_blood_lust_chest", h, "masks_vestments", d(5), 520, 1104, "monolithic_cornerstone");
		entryAt("grinning_mask", h, "masks_vestments", d(5), 401, 1104, "blood_lust_helm");
		entryAt("lodestone_faceplate", h, "masks_vestments", d(5), 437, 1104, "blood_lust_helm");
		entryAt("phantasmal_blood_lust_chest", h, "masks_vestments", d(5), 595, 1106, "monolithic_cornerstone");
		entryAt("sheolic_blood_lust_chest", h, "masks_vestments", d(5), 560, 1104, "monolithic_cornerstone");
		entryAt("tengu_mask", h, "masks_vestments", d(5), 360, 1104, "blood_lust_helm");
		entryAt("velorum_mask", h, "masks_vestments", d(5), 477, 1104, "blood_lust_helm");
		entryAt("blood_wood_leaves", h, "myco_realm_blocks", d(4), 775, 569, "blood_wood_log");
		entryAt("calcified_erythrocoral", h, "fungal_ecology", d(4), 939, 685, "erythrocoral_block");
		entryAt("calcified_hyphae", h, "myco_realm_blocks", d(4), 1117, 594, "hyphae");
		entryAt("erythrocoral_block", h, "fungal_ecology", d(4), 940, 635);
		entryAt("erythrocoral_fan", h, "fungal_ecology", d(4), 939, 574, "erythrocoral_block");
		entryAt("erythrocoral_tendril", h, "fungal_ecology", d(4), 900, 574, "erythrocoral_block");
		entryAt("hematic_iron_bars", h, "architecture", d(4), 457, 883, "hematic_iron_block");
		entryAt("hematic_iron_door", h, "architecture", d(4), 421, 883, "hematic_iron_block");
		entryAt("hematic_iron_trapdoor", h, "architecture", d(4), 497, 883, "hematic_iron_block");
		entryAt("hemorrhagic_crust", h, "living_implements", d(4), 592, 635, "venous_stone");
		entryAt("hyphae", h, "myco_realm_blocks", d(4), 1067, 594, "erythrocytic_mycelium");
		entryAt("infested_wood", h, "myco_realm_blocks", d(4), 775, 614, "blood_wood_log");
		entryAt("mycelium_erythrocytic_dirt", h, "myco_realm_blocks", d(4), 1157, 685, "erythrocytic_mycelium");
		entryAt("sporite_crystal", h, "myco_realm_blocks", d(4), 1169, 594, "calcified_hyphae");
		entryAt("constrictor_cord", h, "new_category", d(3), 1010, 391, "serpent_scale", "puppeteering_thread");
		entryAt("dried_leech", h, "new_category", d(3), 1066, 444, "swollen_leech");
		entryAt("erythrocoral_fragment", h, "new_category", d(3), 1000, 501, "erythrocoral_block");
		entryAt("scale_grip", h, "new_category", d(3), 955, 310, "chitinous_husk", "serpent_scale");
		entryAt("swollen_leech", h, "new_category", d(3), 1105, 391);
		entryAt("tendon_line", h, "new_category", d(3), 1055, 240);
		entryAt("hallowed_residuum_hemorath", h, "qliphoth_reagents", d(5), 860, 105, "blood_trial_altar");
		entryAt("hallowed_residuum_putriciel", h, "qliphoth_reagents", d(5), 900, 105, "blood_trial_altar");
		entryAt("hallowed_residuum_seraphae", h, "qliphoth_reagents", d(5), 955, 105, "blood_trial_altar");
		entryAt("hallowed_residuum_velorum", h, "qliphoth_reagents", d(5), 1010, 105, "blood_trial_altar");
		entryAt("memory_of_vesper", h, "qliphoth_reagents", d(5), 1066, 77, "blood_trial_altar");
		entryAt("monolith_fragment", h, "qliphoth_reagents", d(5), 900, 65, "blood_trial_altar");
		entryAt("monolith_imbued_cloth", h, "qliphoth_reagents", d(5), 955, 65, "blood_trial_altar");
		entryAt("monolithic_cornerstone", h, "qliphoth_reagents", d(5), 556, 1004, "vicars_consecration_kit");
		entryAt("qliphoth_pome", h, "qliphoth_reagents", d(5), 860, 65, "blood_trial_altar");
		entryAt("qliphoth_seed", h, "qliphoth_reagents", d(5), 805, 65, "blood_trial_altar");
		entryAt("vicars_consecration_kit", h, "masks_vestments", d(5), 420, 1004, "hematic_armature");
		entryAt("rhizovitta_communis", h, "spores_cultures", d(4), 1220, 858, "fungal_implantation_pylon");
		entryAt("antiphonomyces_resonans", h, "spores_cultures", d(4), 1220, 903, "fungal_implantation_pylon");
		entryAt("ferric_spores", h, "spores_cultures", d(3), 1318, 614, "mycelial_lantern");
		entryAt("fervent_spores", h, "spores_cultures", d(3), 1284, 614, "mycelial_lantern");
		entryAt("frigid_spores", h, "spores_cultures", d(3), 1348, 614, "mycelial_lantern");
		entryAt("incandescent_spores", h, "spores_cultures", d(3), 1383, 614, "mycelial_lantern");
		entryAt("oculiflora_reticularis", h, "spores_cultures", d(4), 1261, 858, "fungal_implantation_pylon");
		entryAt("neurotic_spores", h, "spores_cultures", d(3), 1383, 685, "mycelial_lantern");
		entryAt("noctifly_agaric", h, "spores_cultures", d(4), 1297, 858, "fungal_implantation_pylon");
		entryAt("ruinous_spores", h, "spores_cultures", d(3), 1284, 685, "mycelial_lantern");
		entryAt("putrivora_resolvens", h, "spores_cultures", d(4), 1341, 858, "fungal_implantation_pylon");
		entryAt("saprovitta_vestigium", h, "spores_cultures", d(4), 1341, 903, "fungal_implantation_pylon");
		entryAt("talaromyces_minus", h, "spores_cultures", d(4), 1261, 903, "fungal_implantation_pylon");
		entryAt("cryostroma_perdurans", h, "spores_cultures", d(4), 1297, 903, "fungal_implantation_pylon");
		entryAt("umbral_spores", h, "spores_cultures", d(3), 1318, 685, "mycelial_lantern");
		entryAt("vivacious_spores", h, "spores_cultures", d(3), 1348, 685, "mycelial_lantern");
		entryAt("echo_of_heart", h, "vascular_craft", d(4), 620, 65, "visceral_mirror");
		entryAt("echo_of_kidneys", h, "vascular_craft", d(4), 560, 65, "visceral_mirror");
		entryAt("echo_of_liver", h, "vascular_craft", d(4), 595, 65, "visceral_mirror");
		entryAt("echo_of_lungs", h, "vascular_craft", d(4), 655, 65, "visceral_mirror");
		entryAt("echo_of_spleen", h, "vascular_craft", d(4), 685, 65, "visceral_mirror");
		entryAt("hematic_iron_chestplate", h, "masks_vestments", d(1), 340, 835, "hematic_iron_scrap");
	}

	private static void registerUnstainedEntries() {
		MaterialAtlasPath u = MaterialAtlasPath.UNSTAINED;
		entry("hemolytic_solution", u, "still_waters_core", a());
		entry("hemolytic_vial", u, "still_waters_core", a());
		entry("pallid_icon", u, "still_waters_core", a());
		entry("tome_of_the_unstained", u, "still_waters_core", a());
		entry("consecrated_copper_ingot", u, "still_waters_core", p(10.0F));
		entry("lethean_poppy_wreath", u, "still_waters_core", p(10.0F));
		entry("pale_humor_flask", u, "still_waters_core", p(15.0F));
		entry("pale_silver_ingot", u, "still_waters_core", p(15.0F));
		entry("pale_distillate", u, "still_waters_core", p(25.0F));
		entry("cleansed_blood_crystal_shard", u, "still_waters_core", p(25.0F));
		entry("tears_of_silthmere", u, "still_waters_core", p(50.0F));
		entry("draught_of_still_waters", u, "still_waters_core", p(50.0F));
		entry("altar_of_cleansing", u, "cleansing_facilities", a());
		entry("unstained_podium", u, "cleansing_facilities", a());
		entry("suspended_cleansed_blood_crystal", u, "cleansing_facilities", p(25.0F));
		entry("lethean_poppy", u, "lethean_flora", a());
		entry("ghost_pipe", u, "lethean_flora", a());
		entry("puffball_fungus", u, "lethean_flora", a());
		entry("cleansed_stone", u, "pale_architecture", a());
		entry("pallid_lantern", u, "pale_architecture", a());
		entry("cleansed_sanguine_glass", u, "pale_architecture", a());
		entry("pale_silver_block", u, "pale_architecture", p(25.0F));
		entry("pale_silver_bells", u, "pale_architecture", p(25.0F));
		entry("hemolytic_plating", u, "anti_blood_warding", p(15.0F));
		entry("neutralizing_gasket", u, "anti_blood_warding", p(25.0F));
		entry("hemolytic_plating_block", u, "anti_blood_warding", p(25.0F));
		entry("lethean_dew", u, "anti_blood_warding", p(25.0F));
		entry("cleansing_hemolymph", u, "anti_blood_warding", p(35.0F));
		entry("lethean_brew", u, "anti_blood_warding", p(50.0F));
		entry("self_reflection_mirror", u, "vestments_instruments", a());
		entry("liber_immaculatus", u, "vestments_instruments", a());
		entry("silver_chalice", u, "vestments_instruments", c(10.0F));
		entry("unstained_helm", u, "vestments_instruments", p(35.0F));
		entry("unstained_shield", u, "vestments_instruments", p(35.0F));
		entry("unstained_warhammer", u, "vestments_instruments", p(35.0F));
		entry("absolution_dagger", u, "vestments_instruments", p(50.0F));
		entryAt("pale_silver_bars", u, "cleansing_facilities", p(35.0F), 720, 420, "altar_of_cleansing");
		entryAt("pallid_retort", u, "cleansing_facilities", p(35.0F), 840, 420, "altar_of_cleansing");
		entryAt("pallid_silver_chain", u, "cleansing_facilities", p(35.0F), 780, 420, "altar_of_cleansing");
		entryAt("saint_sarcophagus", u, "cleansing_facilities", p(35.0F), 900, 420, "altar_of_cleansing");
		entryAt("cleansed_sanguine_pane", u, "pale_architecture", p(25.0F), 610, 850, "cleansed_stone");
		entryAt("annettas_absolution_dagger", u, "vestments_instruments", p(50.0F), 330, 430, "absolution_dagger");
		entryAt("pale_silver_bell", u, "vestments_instruments", p(35.0F), 390, 430, "pale_silver_ingot");
		entryAt("silthmere_glaive", u, "vestments_instruments", p(50.0F), 450, 430);
		entryAt("unstained_boots", u, "vestments_instruments", p(35.0F), 160, 500, "pale_silver_ingot");
		entryAt("unstained_chestplate", u, "vestments_instruments", p(35.0F), 220, 500, "pale_silver_ingot");
		entryAt("unstained_leggings", u, "vestments_instruments", p(35.0F), 280, 500, "pale_silver_ingot");
	}

	private static MaterialGate a() {
		return MaterialGate.always();
	}

	private static MaterialGate d(int degree) {
		return MaterialGate.degree(degree);
	}

	private static MaterialGate p(float purity) {
		return MaterialGate.purity(purity);
	}

	private static MaterialGate c(float clarity) {
		return MaterialGate.clarity(clarity);
	}

	private static void bucket(MaterialAtlasPath path, String id, String label, int color,
			int centerX, int centerY, int plaqueX, int plaqueY) {
		BUCKETS.get(path).add(new MaterialAtlasBucket(path, id, label,
				color, centerX, centerY, plaqueX, plaqueY));
	}

	private static void entry(String materialId, MaterialAtlasPath path, String bucketId, MaterialGate gate,
			String... parentIds) {
		entryAt(materialId, path, bucketId, gate, null, null, parentIds);
	}

	private static void entryAt(String materialId, MaterialAtlasPath path, String bucketId, MaterialGate gate,
			Integer nodeX, Integer nodeY, String... parentIds) {
		MaterialAtlasBucket bucket = bucket(path, bucketId);
		ENTRIES.get(path).put(materialId, new MaterialAtlasEntry(path, materialId, bucket, gate,
				ENTRIES.get(path).size(), List.of(parentIds), nodeX, nodeY));
	}

	private static MaterialAtlasBucket bucket(MaterialAtlasPath path, String bucketId) {
		for (MaterialAtlasBucket bucket : BUCKETS.get(path)) {
			if (bucket.id().equals(bucketId)) {
				return bucket;
			}
		}
		throw new IllegalArgumentException("Unknown material atlas bucket " + path + ":" + bucketId);
	}

	private static void validateParentIds() {
		for (Map.Entry<MaterialAtlasPath, LinkedHashMap<String, MaterialAtlasEntry>> pathEntry : ENTRIES.entrySet()) {
			for (MaterialAtlasEntry entry : pathEntry.getValue().values()) {
				for (String parentId : entry.parentIds()) {
					if (!pathEntry.getValue().containsKey(parentId)) {
						throw new IllegalStateException("Material atlas entry " + entry.materialId()
								+ " references missing parent " + parentId);
					}
				}
			}
		}
	}
}
