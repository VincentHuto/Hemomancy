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
		bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", 0xFFD04436, 615, 500, 485, 345);
		bucket(MaterialAtlasPath.HARBINGER, "vascular_craft", "Vascular Craft", 0xFFB64044, 660, 220, 535, 45);
		bucket(MaterialAtlasPath.HARBINGER, "alchemy_enzymes", "Alchemy & Enzymes", 0xFFD99B2D, 360, 350, 205, 115);
		bucket(MaterialAtlasPath.HARBINGER, "fungal_ecology", "Botany and Mycology", 0xFF714D41, 850, 610, 1244, 509);
		bucket(MaterialAtlasPath.HARBINGER, "morphlings", "Morphlings", 0xFFB45BA5, 760, 875, 680, 1005);
		bucket(MaterialAtlasPath.HARBINGER, "scars_patterns", "Scars & Patterns", 0xFF6FC2D8, 660, 1260, 960, 1216);
		bucket(MaterialAtlasPath.HARBINGER, "living_implements", "Living Implements", 0xFFC58B35, 280, 850, 115, 700);
		bucket(MaterialAtlasPath.HARBINGER, "architecture", "Architecture", 0xFF9A6A45, 470, 615, 370, 510);
		bucket(MaterialAtlasPath.HARBINGER, "new_category", "Biomaterials", 0xFF19711A, 1050, 390, 940, 240);
		bucket(MaterialAtlasPath.HARBINGER, "gourds_vessels", "Gourds & Vessels", 0xFFC78539, 240, 590, 80, 289);
		bucket(MaterialAtlasPath.HARBINGER, "spores_cultures", "Spores & Cultures", 0xFF8EA441, 1065, 805, 950, 915);
		bucket(MaterialAtlasPath.HARBINGER, "myco_realm_blocks", "Myco-Realm Blocks", 0xFF7B8F4D, 1045, 590, 945, 450);
		bucket(MaterialAtlasPath.HARBINGER, "masks_vestments", "Masks & Vestments", 0xFFC15872, 560, 930, 410, 755);
		bucket(MaterialAtlasPath.HARBINGER, "idols_fixtures", "Idols & Fixtures", 0xFF9E7A58, 815, 365, 690, 455);
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
		entryAt("sanguine_formation", h, "bloodcraft_core", a(), 655, 515, "mortal_display");
		entryAt("blood_crystal_shard", h, "bloodcraft_core", a(), 630, 460, "sanguine_formation");
		entryAt("blood_rock", h, "bloodcraft_core", a(), 200, 245);
		entryAt("blood_stained_stone", h, "bloodcraft_core", a(), 630, 515, "sanguine_formation");
		entryAt("hematic_iron_scrap", h, "bloodcraft_core", a(), 555, 615, "hematic_iron_ore");
		entryAt("hematic_iron_powder", h, "bloodcraft_core", d(2), 655, 460, "sanguine_formation");
		entryAt("vivianite_cluster", h, "bloodcraft_core", d(2), 610, 430, "blood_crystal_shard");
		entryAt("hematic_iron_block", h, "bloodcraft_core", d(2), 580, 570, "hematic_iron_ore");
		entryAt("hematic_iron_pillar", h, "bloodcraft_core", d(2), 555, 580, "hematic_iron_block");
		entryAt("sanguine_conduit", h, "bloodcraft_core", d(2), 685, 465, "sanguine_formation");
		entryAt("blood_crystal_block", h, "bloodcraft_core", d(3), 630, 430, "blood_crystal_shard");
		entryAt("suspended_vivianite", h, "bloodcraft_core", d(3), 610, 385, "vivianite_cluster");
		entryAt("sanguine_quintessence", h, "bloodcraft_core", d(5), 685, 515, "sanguine_conduit");
		entryAt("iron_brazier", h, "vascular_craft", d(1), 540, 195, "dictation_table");
		entryAt("blood_basin", h, "vascular_craft", d(2), 750, 195, "blood_pylon");
		entryAt("earthen_vein", h, "vascular_craft", d(2), 650, 250);
		entryAt("somatic_loom", h, "vascular_craft", d(2), 540, 160, "iron_brazier");
		entryAt("mnemonic_reliquary", h, "vascular_craft", d(2), 540, 105);
		entryAt("blood_pylon", h, "vascular_craft", d(3), 710, 195, "blood_trial_altar");
		entryAt("dictation_table", h, "vascular_craft", d(3), 650, 320, "sanguine_conduit");
		entryAt("mortal_display", h, "vascular_craft", d(3), 540, 335);
		entryAt("scarlet_vanity", h, "vascular_craft", d(3), 580, 230, "sanguine_conduit");
		entryAt("scrying_podium", h, "vascular_craft", d(3), 580, 195, "scarlet_vanity");
		entryAt("dendritic_distributor", h, "vascular_craft", d(4), 540, 135, "mnemonic_reliquary", "somatic_loom");
		entryAt("visceral_mirror", h, "vascular_craft", d(4), 580, 160, "scrying_podium");
		entryAt("consecrated_bloodwell", h, "vascular_craft", d(5), 675, 160);
		entryAt("covenant_throne", h, "vascular_craft", d(5), 750, 160, "sanguine_vigil");
		entryAt("sanguine_vigil", h, "vascular_craft", d(5), 710, 160, "consecrated_bloodwell");
		entryAt("sanguine_monolith", h, "vascular_craft", d(5), 710, 135, "consecrated_bloodwell");
		entryAt("blood_trial_altar", h, "vascular_craft", d(5), 805, 105);
		entryAt("bloody_vial", h, "alchemy_enzymes", a(), 365, 230);
		entryAt("bloody_flask", h, "alchemy_enzymes", a(), 265, 245, "blood_rock", "ghastly_alembic");
		entryAt("bleeding_bulb", h, "fungal_ecology", a(), 710, 520, "bleeding_heart");
		entryAt("blood_chum", h, "alchemy_enzymes", a(), 520, 365, "bleeding_bulb");
		entryAt("foul_paste", h, "alchemy_enzymes", a(), 535, 410, "stinkhorn_fungus", "infected_fungus");
		entryAt("bloody_jug", h, "alchemy_enzymes", d(2), 265, 465, "bloody_flask");
		entryAt("vial_rack", h, "alchemy_enzymes", d(2), 365, 255, "bloody_vial");
		entryAt("blood_gourd_white", h, "alchemy_enzymes", d(2), 193, 376);
		entryAt("ghastly_alembic", h, "alchemy_enzymes", d(2), 285, 250);
		entryAt("vial_centrifuge", h, "alchemy_enzymes", d(2), 365, 285, "vial_rack");
		entryAt("ferric_enzyme", h, "alchemy_enzymes", d(2), 415, 200, "recycled_enzyme");
		entryAt("fervent_enzyme", h, "alchemy_enzymes", d(2), 455, 175, "recycled_enzyme");
		entryAt("frigid_enzyme", h, "alchemy_enzymes", d(2), 420, 220, "recycled_enzyme");
		entryAt("incandescent_enzyme", h, "alchemy_enzymes", d(2), 455, 225, "recycled_enzyme");
		entryAt("neurotic_enzyme", h, "alchemy_enzymes", d(2), 460, 200, "recycled_enzyme");
		entryAt("recycled_enzyme", h, "alchemy_enzymes", d(2), 440, 200, "vial_centrifuge");
		entryAt("ruinous_enzyme", h, "alchemy_enzymes", d(2), 440, 230, "recycled_enzyme");
		entryAt("umbral_enzyme", h, "alchemy_enzymes", d(2), 420, 175, "recycled_enzyme");
		entryAt("vivacious_enzyme", h, "alchemy_enzymes", d(2), 440, 170, "recycled_enzyme");
		entryAt("aculeate_vitriol", h, "new_category", d(3), 1115, 320, "toxicognath", "telson", "calcified_blood_spine");
		entryAt("chromatic_sublimate", h, "new_category", d(3), 1115, 355, "cuttlefish_chromatophores", "puppeteering_thread", "serpent_scale");
		entryAt("fervent_husk", h, "new_category", d(3), 1165, 285, "sclerotic_oleum");
		entryAt("queens_physogastrism", h, "new_category", d(3), 1000, 285);
		entryAt("sclerotic_oleum", h, "new_category", d(3), 1115, 285, "queens_physogastrism", "chitinous_husk", "chalybeate_sclerite");
		entryAt("telson", h, "new_category", d(3), 1050, 320);
		entryAt("toxicognath", h, "new_category", d(3), 1000, 320);
		entryAt("bleeding_heart", h, "fungal_ecology", a(), 885, 520);
		entryAt("devils_tooth", h, "fungal_ecology", a(), 885, 610, "erythrocytic_mycelium");
		entryAt("infected_fungus", h, "fungal_ecology", a(), 810, 635, "erythrocytic_mycelium");
		entryAt("rafflesia", h, "fungal_ecology", a(), 885, 725, "erythrocytic_mycelium");
		entryAt("sarcodes", h, "fungal_ecology", a(), 870, 520);
		entryAt("stinkhorn_fungus", h, "fungal_ecology", a(), 785, 635, "erythrocytic_mycelium");
		entryAt("erythrocytic_dirt", h, "fungal_ecology", a(), 785, 670);
		entryAt("bog_body", h, "new_category", d(2), 1000, 395);
		entryAt("blood_wood_log", h, "fungal_ecology", d(3), 850, 670, "erythrocytic_dirt");
		entryAt("blood_wood_planks", h, "fungal_ecology", d(3), 840, 594, "blood_wood_log");
		entryAt("chitinous_husk", h, "new_category", d(3), 945, 285);
		entryAt("conscious_mass", h, "fungal_ecology", d(3), 850, 725, "erythrocytic_dirt");
		entryAt("curved_horn", h, "fungal_ecology", d(3), 265, 381, "blood_gourd_white");
		entryAt("cuttlefish_chromatophores", h, "new_category", d(3), 1050, 355);
		entryAt("erythrocytic_mycelium", h, "fungal_ecology", d(3), 1000, 635, "erythrocytic_dirt");
		entryAt("fargone_proboscis", h, "new_category", d(3), 1165, 320, "aculeate_vitriol");
		entryAt("serpent_scale", h, "new_category", d(3), 945, 355);
		entryAt("spore_sac", h, "fungal_ecology", d(3), 1010, 725, "erythrocytic_mycelium");
		entryAt("venous_pinion", h, "new_category", d(3), 945, 395);
		entryAt("hyphae_block", h, "fungal_ecology", d(4), 915, 725, "erythrocytic_mycelium");
		entryAt("infected_cap", h, "fungal_ecology", d(4), 810, 670, "infected_fungus");
		entryAt("infected_stem", h, "fungal_ecology", d(4), 915, 625);
		entryAt("infested_venous_stone", h, "fungal_ecology", d(4), 870, 610, "erythrocytic_dirt");
		entryAt("fungal_spine", h, "fungal_ecology", d(4), 870, 570);
		entryAt("fungal_podium", h, "fungal_ecology", d(4), 890, 650, "infected_fungus");
		entryAt("mycelial_crucible", h, "fungal_ecology", d(4), 915, 685, "fungal_podium", "morphling_incubator");
		entryAt("fungal_implantation_pylon", h, "fungal_ecology", d(5), 1050, 570, "fungal_podium");
		entryAt("fruiting_infected_cap", h, "fungal_ecology", d(5), 810, 725, "infected_cap");
		entryAt("morphling_jar", h, "morphlings", d(3), 650, 845, "morphling_polyp");
		entryAt("morphling_cradle", h, "morphlings", d(3), 775, 760, "morphling_incubator");
		entryAt("morphling_incubator", h, "morphlings", d(3), 650, 760, "morphling_polyp", "recycled_enzyme");
		entryAt("morphling_polyp", h, "morphlings", d(3), 705, 820);
		entryAt("morphling_bat", h, "morphlings", d(3), 805, 815, "morphling_polyp");
		entryAt("morphling_centipede", h, "morphlings", d(3), 865, 930, "morphling_polyp");
		entryAt("morphling_chitinite", h, "morphlings", d(3), 880, 895, "morphling_polyp");
		entryAt("morphling_cuttlefish", h, "morphlings", d(3), 840, 835, "morphling_polyp");
		entryAt("morphling_fungal", h, "morphlings", d(3), 650, 880, "morphling_polyp");
		entryAt("morphling_leeches", h, "morphlings", d(3), 700, 940, "morphling_polyp");
		entryAt("morphling_mole", h, "morphlings", d(3), 880, 870, "morphling_polyp");
		entryAt("morphling_pests", h, "morphlings", d(3), 770, 980, "morphling_polyp");
		entryAt("morphling_serpent", h, "morphlings", d(3), 675, 905, "morphling_polyp");
		entryAt("morphling_spider", h, "morphlings", d(3), 840, 960, "morphling_polyp");
		entryAt("morphling_tick", h, "morphlings", d(3), 730, 970, "morphling_polyp");
		entryAt("morphling_urchin", h, "morphlings", d(3), 805, 980, "morphling_polyp");
		entryAt("scar_station", h, "scars_patterns", d(4), 1140, 1204);
		entryAt("scar_blank", h, "scars_patterns", d(4), 1165, 1204, "scar_station");
		entryAt("scar_blight", h, "scars_patterns", d(4), 1150, 1264, "scar_station", "scar_blank");
		entryAt("scar_feral", h, "scars_patterns", d(4), 1205, 1204, "scar_station", "scar_blank");
		entryAt("scar_halo", h, "scars_patterns", d(4), 1190, 1239, "scar_station", "scar_blank");
		entryAt("scar_heart", h, "scars_patterns", d(4), 1150, 1134, "scar_station", "scar_blank");
		entryAt("scar_pyre", h, "scars_patterns", d(4), 1190, 1169, "scar_station", "scar_blank");
		entryAt("scar_rime", h, "scars_patterns", d(4), 1105, 1239, "scar_station", "scar_blank");
		entryAt("scar_shade", h, "scars_patterns", d(4), 1105, 1169, "scar_station", "scar_blank");
		entryAt("scar_thorn", h, "scars_patterns", d(4), 1085, 1204, "scar_station", "scar_blank");
		entryAt("scar_anvil", h, "scars_patterns", d(5), 1050, 1204, "scar_thorn");
		entryAt("scar_flux", h, "scars_patterns", d(5), 1250, 1204, "scar_feral");
		entryAt("scar_glacier", h, "scars_patterns", d(5), 1070, 1264, "scar_rime");
		entryAt("scar_marrow", h, "scars_patterns", d(5), 1150, 1104, "scar_heart");
		entryAt("scar_moon", h, "scars_patterns", d(5), 1070, 1144, "scar_shade");
		entryAt("scar_sol", h, "scars_patterns", d(5), 1225, 1144, "scar_pyre");
		entryAt("scar_veil", h, "scars_patterns", d(5), 1225, 1264, "scar_halo");
		entryAt("scar_wither", h, "scars_patterns", d(5), 1150, 1294, "scar_blight");
		entryAt("scar_chimera", h, "scars_patterns", d(6), 1280, 1204, "scar_flux");
		entryAt("scar_corona", h, "scars_patterns", d(6), 1250, 1119, "scar_sol");
		entryAt("scar_crucible", h, "scars_patterns", d(6), 1020, 1204, "scar_anvil");
		entryAt("scar_descendence", h, "scars_patterns", d(6), 1040, 1294, "scar_glacier");
		entryAt("scar_eye", h, "scars_patterns", d(6), 1040, 1119, "scar_moon");
		entryAt("scar_oblivion", h, "scars_patterns", d(6), 1150, 1339, "scar_wither");
		entryAt("scar_phoenix", h, "scars_patterns", d(6), 1150, 1074, "scar_marrow");
		entryAt("scar_transcendence", h, "scars_patterns", d(6), 1260, 1294, "scar_veil");
		entryAt("living_syringe", h, "living_implements", d(2), 235, 825);
		entryAt("puppeteering_thread", h, "new_category", d(2), 1000, 355);
		entryAt("hematic_iron_helm", h, "living_implements", d(2), 275, 835, "hematic_iron_scrap");
		entryAt("hematic_iron_knapper", h, "living_implements", d(2), 275, 825, "hematic_iron_scrap");
		entryAt("hematic_iron_sword", h, "living_implements", d(2), 275, 850, "hematic_iron_scrap");
		entryAt("barbed_blade", h, "living_implements", d(3), 275, 999);
		entryAt("living_staff", h, "living_implements", d(3), 163, 815);
		entryAt("chitinite_mace", h, "living_implements", d(4), 355, 1016);
		entryAt("vivianite_scalpel", h, "living_implements", d(4), 570, 465, "vivianite_cluster");
		entryAt("blood_lust_helm", h, "living_implements", d(5), 375, 1090);
		entryAt("sanguis_lancea", h, "living_implements", d(5), 123, 910);
		entryAt("venous_stone", h, "architecture", a(), 570, 615, "blood_stained_stone");
		entryAt("hematic_iron_ore", h, "architecture", a(), 570, 660);
		entryAt("polished_venous_stone", h, "architecture", d(2), 480, 610, "polished_venous_stone_bricks");
		entryAt("sanguine_glass", h, "architecture", d(2), 570, 575, "blood_crystal_shard");
		entryAt("vivianite_glass", h, "architecture", d(2), 570, 540, "sanguine_glass", "vivianite_cluster");
		entryAt("chiseled_hematic_iron_block", h, "architecture", d(3), 480, 650, "hematic_iron_block");
		entryAt("gilded_venous_stone", h, "architecture", d(3), 525, 575, "venous_stone");
		entryAt("polished_venous_stone_bricks", h, "architecture", d(3), 525, 615, "venous_stone");
		entryAt("mnemonic_ambergris", h, "new_category", d(1), 1165, 355, "chromatic_sublimate");
		entryAt("calcified_blood_spine", h, "new_category", d(1), 945, 320);
		entryAt("chalybeate_sclerite", h, "new_category", d(1), 1050, 285);
		entryAt("active_befouling_ash", h, "alchemy_enzymes", d(3), 220, 180, "ghastly_alembic");
		entryAt("active_smouldering_ash", h, "alchemy_enzymes", d(3), 240, 180, "ghastly_alembic");
		entryAt("crimson_lacquer", h, "alchemy_enzymes", d(3), 265, 180, "ghastly_alembic");
		entryAt("engram_block", h, "alchemy_enzymes", d(3), 285, 180, "ghastly_alembic");
		entryAt("enzyme_primer", h, "alchemy_enzymes", d(3), 225, 210, "ghastly_alembic");
		entryAt("ferric_binder", h, "alchemy_enzymes", d(3), 250, 210, "ghastly_alembic");
		entryAt("qliphoth_bloom", h, "alchemy_enzymes", d(3), 270, 210, "ghastly_alembic");
		entryAt("humoral_barometer", h, "architecture", d(3), 480, 570, "venous_stone");
		entryAt("ossuary_clock", h, "architecture", d(3), 430, 610, "venous_stone");
		entryAt("sanguine_pane", h, "architecture", d(3), 380, 660, "venous_stone");
		entryAt("venous_stone_slab", h, "architecture", d(3), 430, 660, "venous_stone");
		entryAt("venous_stone_wall", h, "architecture", d(3), 380, 710, "venous_stone");
		entryAt("vivianite_pane", h, "architecture", d(3), 430, 710, "venous_stone");
		entryAt("liber_sanguinum", h, "bloodcraft_core", a(), 720, 570);
		entryAt("blood_gourd_black", h, "gourds_vessels", d(2), 240, 325, "blood_gourd_white");
		entryAt("blood_gourd_red", h, "gourds_vessels", d(2), 193, 311, "blood_gourd_white");
		entryAt("dried_gourd", h, "gourds_vessels", d(2), 113, 391, "blood_gourd_white", "gourd");
		entryAt("gourd", h, "gourds_vessels", d(2), 153, 456, "gourd_seeds");
		entryAt("gourd_seeds", h, "gourds_vessels", d(2), 200, 510);
		entryAt("gourd_slice", h, "gourds_vessels", d(2), 113, 506, "gourd");
		entryAt("gourd_stew", h, "gourds_vessels", d(2), 80, 456, "gourd_slice");
		entryAt("gourdvine_tap", h, "gourds_vessels", d(2), 135, 325, "blood_gourd_white");
		entryAt("hemorath_rib", h, "gourds_vessels", d(2), 200, 430, "blood_gourd_white");
		entryAt("sanguine_salve", h, "gourds_vessels", d(3), 225, 594, "bloody_jug");
		entryAt("scrying_dish", h, "gourds_vessels", d(3), 285, 570, "bloody_jug");
		entryAt("vascular_poultice", h, "gourds_vessels", d(3), 225, 635, "sanguine_salve");
		entryAt("vitality_chalice", h, "gourds_vessels", d(3), 330, 540, "bloody_jug");
		entryAt("anastomotic_brazier", h, "idols_fixtures", d(4), 760, 285, "dictation_table");
		entryAt("hematic_armature", h, "idols_fixtures", d(4), 845, 325, "dictation_table");
		entryAt("hematic_iron_chain", h, "idols_fixtures", d(4), 760, 325, "dictation_table");
		entryAt("hematic_stake", h, "idols_fixtures", d(4), 805, 325, "dictation_table");
		entryAt("humane_idol", h, "idols_fixtures", d(4), 705, 395, "dictation_table");
		entryAt("mason_effigy", h, "idols_fixtures", d(4), 775, 395, "dictation_table");
		entryAt("mnemonic_candle", h, "idols_fixtures", d(4), 805, 285, "dictation_table");
		entryAt("mycelial_lantern", h, "idols_fixtures", d(4), 845, 285, "dictation_table");
		entryAt("puppeteers_spindle", h, "idols_fixtures", d(4), 885, 325, "dictation_table");
		entryAt("sanguine_omen", h, "idols_fixtures", d(4), 810, 210, "dictation_table");
		entryAt("semi_sentient_construct", h, "idols_fixtures", d(4), 845, 465, "dictation_table");
		entryAt("serpentine_idol", h, "idols_fixtures", d(4), 845, 395, "dictation_table");
		entryAt("specimen_jar", h, "idols_fixtures", d(4), 885, 285, "dictation_table");
		entryAt("witness_organ", h, "idols_fixtures", d(4), 775, 465, "dictation_table");
		entryAt("annettas_sanguis_lancea", h, "living_implements", d(3), 105, 835, "sanguis_lancea");
		entryAt("sporitic_thurible", h, "living_implements", d(3), 200, 900, "living_staff");
		entryAt("barbed_chestplate", h, "masks_vestments", d(4), 200, 1062, "barbed_blade");
		entryAt("barbed_shield", h, "masks_vestments", d(4), 410, 975, "barbed_blade");
		entryAt("chitinite_chestplate", h, "masks_vestments", d(4), 235, 1062, "chitinite_mace");
		entryAt("chitinite_shield", h, "masks_vestments", d(4), 445, 975, "chitinite_mace");
		entryAt("prismatic_chestplate", h, "masks_vestments", d(4), 270, 1062);
		entryAt("silent_archon_chestplate", h, "masks_vestments", d(5), 310, 1062);
		entryAt("blood_lust_helm_grinning", h, "masks_vestments", d(5), 233, 1134, "blood_lust_helm", "grinning_mask");
		entryAt("blood_lust_helm_lodestone", h, "masks_vestments", d(5), 273, 1134, "blood_lust_helm", "lodestone_faceplate");
		entryAt("blood_lust_helm_tengu", h, "masks_vestments", d(5), 193, 1134, "blood_lust_helm", "tengu_mask");
		entryAt("blood_lust_helm_velorum", h, "masks_vestments", d(5), 313, 1134, "blood_lust_helm", "velorum_mask");
		entryAt("edacious_blood_lust_chest", h, "masks_vestments", d(5), 399, 1216, "blood_lust_helm");
		entryAt("grinning_mask", h, "masks_vestments", d(5), 233, 1204);
		entryAt("lodestone_faceplate", h, "masks_vestments", d(5), 273, 1204);
		entryAt("phantasmal_blood_lust_chest", h, "masks_vestments", d(5), 365, 1216, "blood_lust_helm");
		entryAt("sheolic_blood_lust_chest", h, "masks_vestments", d(5), 455, 1216, "blood_lust_helm");
		entryAt("tengu_mask", h, "masks_vestments", d(5), 193, 1204);
		entryAt("velorum_mask", h, "masks_vestments", d(5), 323, 1204);
		entryAt("blood_wood_leaves", h, "myco_realm_blocks", d(4), 935, 540, "erythrocytic_mycelium");
		entryAt("calcified_erythrocoral", h, "myco_realm_blocks", d(4), 1326, 575, "erythrocytic_mycelium");
		entryAt("calcified_hyphae", h, "myco_realm_blocks", d(4), 1326, 515, "erythrocytic_mycelium");
		entryAt("erythrocoral_block", h, "myco_realm_blocks", d(4), 935, 580, "erythrocytic_mycelium");
		entryAt("erythrocoral_fan", h, "myco_realm_blocks", d(4), 1181, 575, "erythrocytic_mycelium");
		entryAt("erythrocoral_tendril", h, "myco_realm_blocks", d(4), 1256, 575, "erythrocytic_mycelium");
		entryAt("hematic_iron_bars", h, "myco_realm_blocks", d(4), 935, 625, "erythrocytic_mycelium");
		entryAt("hematic_iron_door", h, "myco_realm_blocks", d(4), 1181, 635, "erythrocytic_mycelium");
		entryAt("hematic_iron_trapdoor", h, "myco_realm_blocks", d(4), 1256, 635, "erythrocytic_mycelium");
		entryAt("hemorrhagic_crust", h, "myco_realm_blocks", d(4), 970, 685, "erythrocytic_mycelium");
		entryAt("hyphae", h, "myco_realm_blocks", d(4), 1256, 515, "erythrocytic_mycelium");
		entryAt("infested_wood", h, "myco_realm_blocks", d(4), 1181, 515, "erythrocytic_mycelium");
		entryAt("mycelium_erythrocytic_dirt", h, "myco_realm_blocks", d(4), 1326, 635, "erythrocytic_mycelium");
		entryAt("sporite_crystal", h, "myco_realm_blocks", d(4), 1216, 695, "erythrocytic_mycelium");
		entryAt("constrictor_cord", h, "new_category", d(3), 975, 440);
		entryAt("dried_leech", h, "new_category", d(3), 1135, 515);
		entryAt("erythrocoral_fragment", h, "new_category", d(3), 1296, 505);
		entryAt("scale_grip", h, "new_category", d(3), 1105, 440);
		entryAt("swollen_leech", h, "new_category", d(3), 1216, 510);
		entryAt("tendon_line", h, "new_category", d(3), 1040, 440);
		entryAt("hallowed_residuum_hemorath", h, "qliphoth_reagents", d(5), 860, 105, "blood_trial_altar");
		entryAt("hallowed_residuum_putriciel", h, "qliphoth_reagents", d(5), 900, 105, "blood_trial_altar");
		entryAt("hallowed_residuum_seraphae", h, "qliphoth_reagents", d(5), 955, 105, "blood_trial_altar");
		entryAt("hallowed_residuum_velorum", h, "qliphoth_reagents", d(5), 1010, 105, "blood_trial_altar");
		entryAt("memory_of_vesper", h, "qliphoth_reagents", d(5), 1066, 77, "blood_trial_altar");
		entryAt("monolith_fragment", h, "qliphoth_reagents", d(5), 900, 65, "blood_trial_altar");
		entryAt("monolith_imbued_cloth", h, "qliphoth_reagents", d(5), 955, 65, "blood_trial_altar");
		entryAt("monolithic_cornerstone", h, "qliphoth_reagents", d(5), 1010, 65, "blood_trial_altar");
		entryAt("qliphoth_pome", h, "qliphoth_reagents", d(5), 860, 65, "blood_trial_altar");
		entryAt("qliphoth_seed", h, "qliphoth_reagents", d(5), 805, 65, "blood_trial_altar");
		entryAt("vicars_consecration_kit", h, "qliphoth_reagents", d(5), 850, 160, "blood_trial_altar");
		entryAt("anastocordyceps_nexus", h, "spores_cultures", d(4), 1135, 850, "erythrocytic_mycelium");
		entryAt("antiphonomyces_resonans", h, "spores_cultures", d(4), 1135, 895, "erythrocytic_mycelium");
		entryAt("ferric_spores", h, "spores_cultures", d(3), 1165, 745, "erythrocytic_mycelium");
		entryAt("fervent_spores", h, "spores_cultures", d(3), 1206, 745, "erythrocytic_mycelium");
		entryAt("frigid_spores", h, "spores_cultures", d(3), 1241, 745, "erythrocytic_mycelium");
		entryAt("incandescent_spores", h, "spores_cultures", d(3), 1281, 745, "erythrocytic_mycelium");
		entryAt("lumina_devorans", h, "spores_cultures", d(4), 1186, 850, "erythrocytic_mycelium");
		entryAt("neurotic_spores", h, "spores_cultures", d(3), 1281, 780, "erythrocytic_mycelium");
		entryAt("noctifly_agaric", h, "spores_cultures", d(4), 1241, 850, "erythrocytic_mycelium");
		entryAt("ruinous_spores", h, "spores_cultures", d(3), 1165, 780, "erythrocytic_mycelium");
		entryAt("sanguiflora_cadens", h, "spores_cultures", d(4), 1296, 850, "erythrocytic_mycelium");
		entryAt("saprovitta_vestigium", h, "spores_cultures", d(4), 1296, 900, "erythrocytic_mycelium");
		entryAt("talaromyces_minus", h, "spores_cultures", d(4), 1186, 895, "erythrocytic_mycelium");
		entryAt("thanomyces_resurgens", h, "spores_cultures", d(4), 1241, 895, "erythrocytic_mycelium");
		entryAt("umbral_spores", h, "spores_cultures", d(3), 1206, 780, "erythrocytic_mycelium");
		entryAt("vivacious_spores", h, "spores_cultures", d(3), 1241, 780, "erythrocytic_mycelium");
		entryAt("echo_of_heart", h, "vascular_craft", d(4), 620, 65, "visceral_mirror");
		entryAt("echo_of_kidneys", h, "vascular_craft", d(4), 560, 65, "visceral_mirror");
		entryAt("echo_of_liver", h, "vascular_craft", d(4), 595, 65, "visceral_mirror");
		entryAt("echo_of_lungs", h, "vascular_craft", d(4), 655, 65, "visceral_mirror");
		entryAt("echo_of_spleen", h, "vascular_craft", d(4), 685, 65, "visceral_mirror");
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
