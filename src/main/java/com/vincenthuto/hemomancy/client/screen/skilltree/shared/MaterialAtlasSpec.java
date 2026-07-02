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
		bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", 0xFFD04436, 520, 230, 608, 530);
		bucket(MaterialAtlasPath.HARBINGER, "vascular_craft", "Vascular Craft", 0xFFB64044, 805, 330, 610, 156);
		bucket(MaterialAtlasPath.HARBINGER, "alchemy_enzymes", "Alchemy & Enzymes", 0xFFD99B2D, 235, 330, 631, 844);
		bucket(MaterialAtlasPath.HARBINGER, "fungal_ecology", "Botany and Mycology", 0xFF714D41, 850, 620, 875, 490);
		bucket(MaterialAtlasPath.HARBINGER, "morphlings", "Morphlings", 0xFFB45BA5, 720, 820, 880, 1030);
		bucket(MaterialAtlasPath.HARBINGER, "scars_patterns", "Scars & Patterns", 0xFF6FC2D8, 520, 870, 528, 947);
		bucket(MaterialAtlasPath.HARBINGER, "living_implements", "Living Implements", 0xFFC58B35, 320, 780, 98, 570);
		bucket(MaterialAtlasPath.HARBINGER, "architecture", "Architecture", 0xFF9A6A45, 180, 560, 426, 648);
		bucket(MaterialAtlasPath.HARBINGER, "new_category", "Biomaterials", 0xFF19711A, 562, 624, 882, 226);
		bucket(MaterialAtlasPath.UNSTAINED, "still_waters_core", "Still Waters Core", 0xFF80B0A0, 520, 230, 520, 170);
		bucket(MaterialAtlasPath.UNSTAINED, "cleansing_facilities", "Cleansing Facilities", 0xFF8FB8D8, 790, 360, 870, 330);
		bucket(MaterialAtlasPath.UNSTAINED, "lethean_flora", "Lethean Flora", 0xFFA6C58A, 760, 720, 840, 760);
		bucket(MaterialAtlasPath.UNSTAINED, "pale_architecture", "Pale Architecture", 0xFFD5D0B6, 520, 850, 520, 940);
		bucket(MaterialAtlasPath.UNSTAINED, "anti_blood_warding", "Anti-Blood Warding", 0xFFB7C6CE, 250, 720, 145, 760);
		bucket(MaterialAtlasPath.UNSTAINED, "vestments_instruments", "Vestments & Instruments", 0xFFB09AC8, 250, 360, 120, 330);
	}

	private static void registerHarbingerEntries() {
		MaterialAtlasPath h = MaterialAtlasPath.HARBINGER;
		entryAt("sanguine_formation", h, "bloodcraft_core", a(), 513, 468, "mortal_display");
		entryAt("blood_crystal_shard", h, "bloodcraft_core", a(), 461, 408, "sanguine_formation");
		entryAt("blood_rock", h, "bloodcraft_core", a(), 179, 425);
		entryAt("blood_stained_stone", h, "bloodcraft_core", a(), 461, 468, "sanguine_formation");
		entryAt("hematic_iron_scrap", h, "bloodcraft_core", a(), 295, 599, "hematic_iron_ore");
		entryAt("hematic_iron_powder", h, "bloodcraft_core", d(2), 513, 408, "sanguine_formation");
		entryAt("vivianite_cluster", h, "bloodcraft_core", d(2), 413, 361, "blood_crystal_shard");
		entryAt("hematic_iron_block", h, "bloodcraft_core", d(2), 358, 540, "hematic_iron_ore");
		entryAt("hematic_iron_pillar", h, "bloodcraft_core", d(2), 295, 559, "hematic_iron_block");
		entryAt("sanguine_conduit", h, "bloodcraft_core", d(2), 568, 408, "sanguine_formation");
		entryAt("blood_crystal_block", h, "bloodcraft_core", d(3), 461, 361, "blood_crystal_shard");
		entryAt("suspended_vivianite", h, "bloodcraft_core", d(3), 413, 307, "vivianite_cluster");
		entryAt("sanguine_quintessence", h, "bloodcraft_core", d(5), 568, 468, "sanguine_conduit");
		entryAt("iron_brazier", h, "vascular_craft", d(1), 513, 307, "dictation_table");
		entryAt("blood_basin", h, "vascular_craft", d(2), 728, 307, "blood_pylon");
		entryAt("earthen_vein", h, "vascular_craft", d(2), 621, 408);
		entryAt("somatic_loom", h, "vascular_craft", d(2), 513, 249, "iron_brazier");
		entryAt("mnemonic_reliquary", h, "vascular_craft", d(2), 513, 158);
		entryAt("blood_pylon", h, "vascular_craft", d(3), 677, 307, "blood_trial_altar");
		entryAt("dictation_table", h, "vascular_craft", d(3), 513, 361, "sanguine_conduit");
		entryAt("mortal_display", h, "vascular_craft", d(3), 513, 525);
		entryAt("scarlet_vanity", h, "vascular_craft", d(3), 568, 361, "sanguine_conduit");
		entryAt("scrying_podium", h, "vascular_craft", d(3), 568, 307, "scarlet_vanity");
		entryAt("dendritic_distributor", h, "vascular_craft", d(4), 513, 202, "mnemonic_reliquary", "somatic_loom");
		entryAt("visceral_mirror", h, "vascular_craft", d(4), 568, 249, "scrying_podium");
		entryAt("consecrated_bloodwell", h, "vascular_craft", d(5), 621, 249);
		entryAt("covenant_throne", h, "vascular_craft", d(5), 728, 249, "sanguine_vigil");
		entryAt("sanguine_vigil", h, "vascular_craft", d(5), 677, 249, "consecrated_bloodwell");
		entryAt("sanguine_monolith", h, "vascular_craft", d(5), 677, 202, "consecrated_bloodwell");
		entryAt("blood_trial_altar", h, "vascular_craft", d(5), 621, 307);
		entryAt("bloody_vial", h, "alchemy_enzymes", a(), 513, 576);
		entryAt("bloody_flask", h, "alchemy_enzymes", a(), 179, 364, "blood_rock", "ghastly_alembic");
		entryAt("bleeding_bulb", h, "fungal_ecology", a(), 847, 445, "bleeding_heart");
		entryAt("blood_chum", h, "alchemy_enzymes", a(), 895, 445, "bleeding_bulb");
		entryAt("foul_paste", h, "alchemy_enzymes", a(), 847, 525, "stinkhorn_fungus", "infected_fungus");
		entryAt("bloody_jug", h, "alchemy_enzymes", d(2), 179, 319, "bloody_flask");
		entryAt("vial_rack", h, "alchemy_enzymes", d(2), 513, 628, "bloody_vial");
		entryAt("blood_gourd_white", h, "alchemy_enzymes", d(2), 248, 425, "blood_rock");
		entryAt("ghastly_alembic", h, "alchemy_enzymes", d(2), 248, 378);
		entryAt("vial_centrifuge", h, "alchemy_enzymes", d(2), 513, 691, "vial_rack");
		entryAt("ferric_enzyme", h, "alchemy_enzymes", d(2), 579, 741, "recycled_enzyme");
		entryAt("fervent_enzyme", h, "alchemy_enzymes", d(2), 683, 691, "recycled_enzyme");
		entryAt("frigid_enzyme", h, "alchemy_enzymes", d(2), 595, 782, "recycled_enzyme");
		entryAt("incandescent_enzyme", h, "alchemy_enzymes", d(2), 683, 787, "recycled_enzyme");
		entryAt("neurotic_enzyme", h, "alchemy_enzymes", d(2), 701, 741, "recycled_enzyme");
		entryAt("recycled_enzyme", h, "alchemy_enzymes", d(2), 640, 741, "vial_centrifuge");
		entryAt("ruinous_enzyme", h, "alchemy_enzymes", d(2), 640, 801, "recycled_enzyme");
		entryAt("umbral_enzyme", h, "alchemy_enzymes", d(2), 595, 691, "recycled_enzyme");
		entryAt("vivacious_enzyme", h, "alchemy_enzymes", d(2), 640, 680, "recycled_enzyme");
		entryAt("aculeate_vitriol", h, "new_category", d(3), 986, 315, "toxicognath", "telson", "calcified_blood_spine");
		entryAt("chromatic_sublimate", h, "new_category", d(3), 986, 352, "cuttlefish_chromatophores", "puppeteering_thread", "serpent_scale");
		entryAt("fervent_husk", h, "new_category", d(3), 1033, 280, "sclerotic_oleum");
		entryAt("queens_physogastrism", h, "new_category", d(3), 882, 280);
		entryAt("sclerotic_oleum", h, "new_category", d(3), 986, 280, "queens_physogastrism", "chitinous_husk", "chalybeate_sclerite");
		entryAt("telson", h, "new_category", d(3), 925, 315);
		entryAt("toxicognath", h, "new_category", d(3), 882, 315);
		entryAt("bleeding_heart", h, "fungal_ecology", a(), 799, 445);
		entryAt("devils_tooth", h, "fungal_ecology", a(), 800, 525, "erythrocytic_mycelium");
		entryAt("infected_fungus", h, "fungal_ecology", a(), 895, 576, "erythrocytic_mycelium");
		entryAt("rafflesia", h, "fungal_ecology", a(), 800, 628, "erythrocytic_mycelium");
		entryAt("sarcodes", h, "fungal_ecology", a(), 757, 445);
		entryAt("stinkhorn_fungus", h, "fungal_ecology", a(), 847, 576, "erythrocytic_mycelium");
		entryAt("erythrocytic_dirt", h, "fungal_ecology", a(), 757, 576);
		entryAt("bog_body", h, "new_category", d(2), 882, 392);
		entryAt("blood_wood_log", h, "fungal_ecology", d(3), 709, 576, "erythrocytic_dirt");
		entryAt("blood_wood_planks", h, "fungal_ecology", d(3), 709, 525, "blood_wood_log");
		entryAt("chitinous_husk", h, "new_category", d(3), 831, 280);
		entryAt("conscious_mass", h, "fungal_ecology", d(3), 709, 628, "erythrocytic_dirt");
		entryAt("curved_horn", h, "fungal_ecology", d(3), 296, 425, "blood_gourd_white");
		entryAt("cuttlefish_chromatophores", h, "new_category", d(3), 925, 352);
		entryAt("erythrocytic_mycelium", h, "fungal_ecology", d(3), 800, 576, "erythrocytic_dirt");
		entryAt("fargone_proboscis", h, "new_category", d(3), 1033, 315, "aculeate_vitriol");
		entryAt("serpent_scale", h, "new_category", d(3), 831, 352);
		entryAt("spore_sac", h, "fungal_ecology", d(3), 757, 628, "erythrocytic_mycelium");
		entryAt("venous_pinion", h, "new_category", d(3), 831, 392);
		entryAt("hyphae_block", h, "fungal_ecology", d(4), 848, 628, "erythrocytic_mycelium");
		entryAt("infected_cap", h, "fungal_ecology", d(4), 895, 612, "infected_fungus");
		entryAt("infected_stem", h, "fungal_ecology", d(4), 895, 540);
		entryAt("infested_venous_stone", h, "fungal_ecology", d(4), 757, 525, "erythrocytic_dirt");
		entryAt("fungal_spine", h, "fungal_ecology", d(4), 757, 488);
		entryAt("fungal_podium", h, "fungal_ecology", d(4), 951, 576, "infected_fungus");
		entryAt("mycelial_crucible", h, "fungal_ecology", d(4), 1014, 612, "fungal_podium", "morphling_incubator");
		entryAt("fungal_implantation_pylon", h, "fungal_ecology", d(5), 1014, 540, "fungal_podium");
		entryAt("fruiting_infected_cap", h, "fungal_ecology", d(5), 951, 612, "infected_cap");
		entryAt("morphling_jar", h, "morphlings", d(3), 775, 815, "morphling_polyp");
		entryAt("morphling_cradle", h, "morphlings", d(3), 911, 701, "morphling_incubator");
		entryAt("morphling_incubator", h, "morphlings", d(3), 799, 717, "morphling_polyp", "recycled_enzyme");
		entryAt("morphling_polyp", h, "morphlings", d(3), 847, 775);
		entryAt("morphling_bat", h, "morphlings", d(3), 951, 775, "morphling_polyp");
		entryAt("morphling_centipede", h, "morphlings", d(3), 1014, 925, "morphling_polyp");
		entryAt("morphling_chitinite", h, "morphlings", d(3), 1030, 881, "morphling_polyp");
		entryAt("morphling_cuttlefish", h, "morphlings", d(3), 983, 801, "morphling_polyp");
		entryAt("morphling_fungal", h, "morphlings", d(3), 775, 859, "morphling_polyp");
		entryAt("morphling_leeches", h, "morphlings", d(3), 831, 941, "morphling_polyp");
		entryAt("morphling_mole", h, "morphlings", d(3), 1030, 848, "morphling_polyp");
		entryAt("morphling_pests", h, "morphlings", d(3), 906, 995, "morphling_polyp");
		entryAt("morphling_serpent", h, "morphlings", d(3), 799, 897, "morphling_polyp");
		entryAt("morphling_spider", h, "morphlings", d(3), 983, 968, "morphling_polyp");
		entryAt("morphling_tick", h, "morphlings", d(3), 863, 979, "morphling_polyp");
		entryAt("morphling_urchin", h, "morphlings", d(3), 951, 995, "morphling_polyp");
		entryAt("scar_station", h, "scars_patterns", d(4), 296, 897);
		entryAt("scar_blank", h, "scars_patterns", d(4), 328, 897, "scar_station");
		entryAt("scar_blight", h, "scars_patterns", d(4), 312, 981, "scar_station", "scar_blank");
		entryAt("scar_feral", h, "scars_patterns", d(4), 380, 897, "scar_station", "scar_blank");
		entryAt("scar_halo", h, "scars_patterns", d(4), 364, 948, "scar_station", "scar_blank");
		entryAt("scar_heart", h, "scars_patterns", d(4), 312, 801, "scar_station", "scar_blank");
		entryAt("scar_pyre", h, "scars_patterns", d(4), 364, 848, "scar_station", "scar_blank");
		entryAt("scar_rime", h, "scars_patterns", d(4), 247, 948, "scar_station", "scar_blank");
		entryAt("scar_shade", h, "scars_patterns", d(4), 247, 848, "scar_station", "scar_blank");
		entryAt("scar_thorn", h, "scars_patterns", d(4), 225, 897, "scar_station", "scar_blank");
		entryAt("scar_anvil", h, "scars_patterns", d(5), 182, 897, "scar_thorn");
		entryAt("scar_flux", h, "scars_patterns", d(5), 435, 897, "scar_feral");
		entryAt("scar_glacier", h, "scars_patterns", d(5), 206, 983, "scar_rime");
		entryAt("scar_marrow", h, "scars_patterns", d(5), 312, 761, "scar_heart");
		entryAt("scar_moon", h, "scars_patterns", d(5), 206, 815, "scar_shade");
		entryAt("scar_sol", h, "scars_patterns", d(5), 404, 815, "scar_pyre");
		entryAt("scar_veil", h, "scars_patterns", d(5), 404, 983, "scar_halo");
		entryAt("scar_wither", h, "scars_patterns", d(5), 312, 1032, "scar_blight");
		entryAt("scar_chimera", h, "scars_patterns", d(6), 474, 897, "scar_flux");
		entryAt("scar_corona", h, "scars_patterns", d(6), 435, 775, "scar_sol");
		entryAt("scar_crucible", h, "scars_patterns", d(6), 142, 897, "scar_anvil");
		entryAt("scar_descendence", h, "scars_patterns", d(6), 162, 1024, "scar_glacier");
		entryAt("scar_eye", h, "scars_patterns", d(6), 162, 775, "scar_moon");
		entryAt("scar_oblivion", h, "scars_patterns", d(6), 312, 1089, "scar_wither");
		entryAt("scar_phoenix", h, "scars_patterns", d(6), 312, 717, "scar_marrow");
		entryAt("scar_transcendence", h, "scars_patterns", d(6), 452, 1027, "scar_veil");
		entryAt("living_syringe", h, "living_implements", d(2), 190, 540);
		entryAt("puppeteering_thread", h, "new_category", d(2), 882, 352);
		entryAt("hematic_iron_helm", h, "living_implements", d(2), 238, 576, "hematic_iron_scrap");
		entryAt("hematic_iron_knapper", h, "living_implements", d(2), 238, 540, "hematic_iron_scrap");
		entryAt("hematic_iron_sword", h, "living_implements", d(2), 238, 612, "hematic_iron_scrap");
		entryAt("barbed_blade", h, "living_implements", d(3), 142, 655);
		entryAt("living_staff", h, "living_implements", d(3), 190, 504);
		entryAt("chitinite_mace", h, "living_implements", d(4), 190, 612);
		entryAt("vivianite_scalpel", h, "living_implements", d(4), 358, 361, "vivianite_cluster");
		entryAt("blood_lust_helm", h, "living_implements", d(5), 238, 655);
		entryAt("sanguis_lancea", h, "living_implements", d(5), 190, 655);
		entryAt("venous_stone", h, "architecture", a(), 413, 491, "blood_stained_stone");
		entryAt("hematic_iron_ore", h, "architecture", a(), 413, 540);
		entryAt("polished_venous_stone", h, "architecture", d(2), 295, 488, "polished_venous_stone_bricks");
		entryAt("sanguine_glass", h, "architecture", d(2), 413, 445, "blood_crystal_shard");
		entryAt("vivianite_glass", h, "architecture", d(2), 413, 408, "sanguine_glass", "vivianite_cluster");
		entryAt("chiseled_hematic_iron_block", h, "architecture", d(3), 295, 525, "hematic_iron_block");
		entryAt("gilded_venous_stone", h, "architecture", d(3), 358, 445, "venous_stone");
		entryAt("polished_venous_stone_bricks", h, "architecture", d(3), 358, 491, "venous_stone");
		entryAt("mnemonic_ambergris", h, "new_category", d(1), 1033, 352, "chromatic_sublimate");
		entryAt("calcified_blood_spine", h, "new_category", d(1), 831, 315);
		entryAt("chalybeate_sclerite", h, "new_category", d(1), 925, 280);
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
