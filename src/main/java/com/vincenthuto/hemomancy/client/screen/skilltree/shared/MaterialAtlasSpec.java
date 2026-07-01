package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MaterialAtlasSpec {
	private static final int HUB_X = 520;
	private static final int HUB_Y = 520;

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

	public static MaterialAtlasEntry entryFor(MaterialAtlasPath path, MaterialEntry material) {
		MaterialAtlasEntry entry = ENTRIES.getOrDefault(path, new LinkedHashMap<>()).get(material.name());
		if (entry != null) {
			return entry;
		}
		MaterialAtlasBucket fallback = BUCKETS.get(path).isEmpty()
				? new MaterialAtlasBucket(path, "unknown", "Unknown", material.name(),
						path.accentColor(), HUB_X, HUB_Y, HUB_X, HUB_Y)
				: BUCKETS.get(path).getFirst();
		return new MaterialAtlasEntry(path, material.name(), fallback, MaterialGate.always(), Integer.MAX_VALUE, List.of(),
				null, null);
	}

	public static int hubX(MaterialAtlasPath path) {
		return HUB_X;
	}

	public static int hubY(MaterialAtlasPath path) {
		return HUB_Y;
	}

	private static void registerBuckets() {
		bucket(MaterialAtlasPath.HARBINGER, "bloodcraft_core", "Bloodcraft Core", "sanguine_formation", 0xFFD04436, 520, 230, 520, 170);
		bucket(MaterialAtlasPath.HARBINGER, "vascular_craft", "Vascular Craft", "iron_brazier", 0xFFB64044, 805, 330, 890, 300);
		bucket(MaterialAtlasPath.HARBINGER, "alchemy_enzymes", "Alchemy & Enzymes", "bloody_vial", 0xFFD99B2D, 235, 330, 135, 300);
		bucket(MaterialAtlasPath.HARBINGER, "fungal_ecology", "Fungal Ecology", "bleeding_heart", 0xFF7EA33B, 850, 620, 915, 650);
		bucket(MaterialAtlasPath.HARBINGER, "morphlings", "Morphlings", "morphling_jar", 0xFFB45BA5, 720, 820, 760, 910);
		bucket(MaterialAtlasPath.HARBINGER, "scars_patterns", "Scars & Patterns", "scar_station", 0xFF6FC2D8, 520, 870, 520, 955);
		bucket(MaterialAtlasPath.HARBINGER, "living_implements", "Living Implements", "living_syringe", 0xFFC58B35, 320, 780, 210, 850);
		bucket(MaterialAtlasPath.HARBINGER, "architecture", "Architecture", "venous_stone", 0xFF9A6A45, 180, 560, 90, 600);

		bucket(MaterialAtlasPath.UNSTAINED, "still_waters_core", "Still Waters Core", "hemolytic_solution", 0xFF80B0A0, 520, 230, 520, 170);
		bucket(MaterialAtlasPath.UNSTAINED, "cleansing_facilities", "Cleansing Facilities", "altar_of_cleansing", 0xFF8FB8D8, 790, 360, 870, 330);
		bucket(MaterialAtlasPath.UNSTAINED, "lethean_flora", "Lethean Flora", "lethean_poppy", 0xFFA6C58A, 760, 720, 840, 760);
		bucket(MaterialAtlasPath.UNSTAINED, "pale_architecture", "Pale Architecture", "cleansed_stone", 0xFFD5D0B6, 520, 850, 520, 940);
		bucket(MaterialAtlasPath.UNSTAINED, "anti_blood_warding", "Anti-Blood Warding", "hemolytic_plating", 0xFFB7C6CE, 250, 720, 145, 760);
		bucket(MaterialAtlasPath.UNSTAINED, "vestments_instruments", "Vestments & Instruments", "self_reflection_mirror", 0xFFB09AC8, 250, 360, 120, 330);
	}

	private static void registerHarbingerEntries() {
		MaterialAtlasPath h = MaterialAtlasPath.HARBINGER;
		entry("sanguine_formation", h, "bloodcraft_core", a());
		entry("blood_crystal_shard", h, "bloodcraft_core", a());
		entry("blood_rock", h, "bloodcraft_core", a());
		entry("blood_stained_stone", h, "bloodcraft_core", a());
		entry("hematic_iron_scrap", h, "bloodcraft_core", a());
		entry("hematic_iron_powder", h, "bloodcraft_core", d(2));
		entry("vivianite_cluster", h, "bloodcraft_core", d(2));
		entry("hematic_iron_block", h, "bloodcraft_core", d(2));
		entry("hematic_iron_pillar", h, "bloodcraft_core", d(2));
		entry("sanguine_conduit", h, "bloodcraft_core", d(2));
		entry("blood_crystal_block", h, "bloodcraft_core", d(3));
		entry("suspended_vivianite", h, "bloodcraft_core", d(3));
		entry("sanguine_quintessence", h, "bloodcraft_core", d(5));

		entry("iron_brazier", h, "vascular_craft", d(1));
		entry("blood_basin", h, "vascular_craft", d(2));
		entry("earthen_vein", h, "vascular_craft", d(2));
		entry("somatic_loom", h, "vascular_craft", d(2));
		entry("mnemonic_reliquary", h, "vascular_craft", d(2));
		entry("blood_pylon", h, "vascular_craft", d(3));
		entry("dictation_table", h, "vascular_craft", d(3));
		entry("mortal_display", h, "vascular_craft", d(3));
		entry("scarlet_vanity", h, "vascular_craft", d(3));
		entry("scrying_podium", h, "vascular_craft", d(3));
		entry("dendritic_distributor", h, "vascular_craft", d(4));
		entry("visceral_mirror", h, "vascular_craft", d(4));
		entry("consecrated_bloodwell", h, "vascular_craft", d(5));
		entry("covenant_throne", h, "vascular_craft", d(5));
		entry("sanguine_vigil", h, "vascular_craft", d(5));
		entry("sanguine_monolith", h, "vascular_craft", d(5));
		entry("blood_trial_altar", h, "vascular_craft", d(5));

		entry("bloody_vial", h, "alchemy_enzymes", a());
		entry("bloody_flask", h, "alchemy_enzymes", a());
		entry("bleeding_bulb", h, "alchemy_enzymes", a());
		entry("blood_chum", h, "alchemy_enzymes", a());
		entry("foul_paste", h, "alchemy_enzymes", a());
		entry("bloody_jug", h, "alchemy_enzymes", d(2));
		entry("vial_rack", h, "alchemy_enzymes", d(2));
		entry("blood_gourd_white", h, "alchemy_enzymes", d(2));
		entry("ghastly_alembic", h, "alchemy_enzymes", d(2));
		entry("vial_centrifuge", h, "alchemy_enzymes", d(2));
		entry("ferric_enzyme", h, "alchemy_enzymes", d(2));
		entry("fervent_enzyme", h, "alchemy_enzymes", d(2));
		entry("frigid_enzyme", h, "alchemy_enzymes", d(2));
		entry("incandescent_enzyme", h, "alchemy_enzymes", d(2));
		entry("neurotic_enzyme", h, "alchemy_enzymes", d(2));
		entry("recycled_enzyme", h, "alchemy_enzymes", d(2));
		entry("ruinous_enzyme", h, "alchemy_enzymes", d(2));
		entry("umbral_enzyme", h, "alchemy_enzymes", d(2));
		entry("vivacious_enzyme", h, "alchemy_enzymes", d(2));
		entry("aculeate_vitriol", h, "alchemy_enzymes", d(3));
		entry("chromatic_sublimate", h, "alchemy_enzymes", d(3));
		entry("fervent_husk", h, "alchemy_enzymes", d(3));
		entry("queens_physogastrism", h, "alchemy_enzymes", d(3));
		entry("sclerotic_oleum", h, "alchemy_enzymes", d(3));
		entry("telson", h, "alchemy_enzymes", d(3));
		entry("toxicognath", h, "alchemy_enzymes", d(3));

		entry("bleeding_heart", h, "fungal_ecology", a());
		entry("devils_tooth", h, "fungal_ecology", a());
		entry("infected_fungus", h, "fungal_ecology", a());
		entry("rafflesia", h, "fungal_ecology", a());
		entry("sarcodes", h, "fungal_ecology", a());
		entry("stinkhorn_fungus", h, "fungal_ecology", a());
		entry("erythrocytic_dirt", h, "fungal_ecology", a());
		entry("bog_body", h, "fungal_ecology", d(2));
		entry("blood_wood_log", h, "fungal_ecology", d(3));
		entry("blood_wood_planks", h, "fungal_ecology", d(3));
		entry("chitinous_husk", h, "fungal_ecology", d(3));
		entry("conscious_mass", h, "fungal_ecology", d(3));
		entry("curved_horn", h, "fungal_ecology", d(3));
		entry("cuttlefish_chromatophores", h, "fungal_ecology", d(3));
		entry("erythrocytic_mycelium", h, "fungal_ecology", d(3));
		entry("fargone_proboscis", h, "fungal_ecology", d(3));
		entry("serpent_scale", h, "fungal_ecology", d(3));
		entry("spore_sac", h, "fungal_ecology", d(3));
		entry("venous_pinion", h, "fungal_ecology", d(3));
		entry("hyphae_block", h, "fungal_ecology", d(4));
		entry("infected_cap", h, "fungal_ecology", d(4));
		entry("infected_stem", h, "fungal_ecology", d(4));
		entry("infested_venous_stone", h, "fungal_ecology", d(4));
		entry("fungal_spine", h, "fungal_ecology", d(4));
		entry("fungal_podium", h, "fungal_ecology", d(4));
		entry("mycelial_crucible", h, "fungal_ecology", d(4));
		entry("fungal_implantation_pylon", h, "fungal_ecology", d(5));
		entry("fruiting_infected_cap", h, "fungal_ecology", d(5));

		entry("morphling_jar", h, "morphlings", d(3));
		entry("morphling_cradle", h, "morphlings", d(3));
		entry("morphling_incubator", h, "morphlings", d(3));
		entry("morphling_polyp", h, "morphlings", d(3));
		entry("morphling_bat", h, "morphlings", d(3));
		entry("morphling_centipede", h, "morphlings", d(3));
		entry("morphling_chitinite", h, "morphlings", d(3));
		entry("morphling_cuttlefish", h, "morphlings", d(3));
		entry("morphling_fungal", h, "morphlings", d(3));
		entry("morphling_leeches", h, "morphlings", d(3));
		entry("morphling_mole", h, "morphlings", d(3));
		entry("morphling_pests", h, "morphlings", d(3));
		entry("morphling_serpent", h, "morphlings", d(3));
		entry("morphling_spider", h, "morphlings", d(3));
		entry("morphling_tick", h, "morphlings", d(3));
		entry("morphling_urchin", h, "morphlings", d(3));

		entry("scar_station", h, "scars_patterns", d(4));
		entry("scar_blank", h, "scars_patterns", d(4));
		entry("scar_blight", h, "scars_patterns", d(4));
		entry("scar_feral", h, "scars_patterns", d(4));
		entry("scar_halo", h, "scars_patterns", d(4));
		entry("scar_heart", h, "scars_patterns", d(4));
		entry("scar_pyre", h, "scars_patterns", d(4));
		entry("scar_rime", h, "scars_patterns", d(4));
		entry("scar_shade", h, "scars_patterns", d(4));
		entry("scar_thorn", h, "scars_patterns", d(4));
		entry("scar_anvil", h, "scars_patterns", d(5));
		entry("scar_flux", h, "scars_patterns", d(5));
		entry("scar_glacier", h, "scars_patterns", d(5));
		entry("scar_marrow", h, "scars_patterns", d(5));
		entry("scar_moon", h, "scars_patterns", d(5));
		entry("scar_sol", h, "scars_patterns", d(5));
		entry("scar_veil", h, "scars_patterns", d(5));
		entry("scar_wither", h, "scars_patterns", d(5));
		entry("scar_chimera", h, "scars_patterns", d(6));
		entry("scar_corona", h, "scars_patterns", d(6));
		entry("scar_crucible", h, "scars_patterns", d(6));
		entry("scar_descendence", h, "scars_patterns", d(6));
		entry("scar_eye", h, "scars_patterns", d(6));
		entry("scar_oblivion", h, "scars_patterns", d(6));
		entry("scar_phoenix", h, "scars_patterns", d(6));
		entry("scar_transcendence", h, "scars_patterns", d(6));

		entry("living_syringe", h, "living_implements", d(2));
		entry("puppeteering_thread", h, "living_implements", d(2));
		entry("hematic_iron_helm", h, "living_implements", d(2));
		entry("hematic_iron_knapper", h, "living_implements", d(2));
		entry("hematic_iron_sword", h, "living_implements", d(2));
		entry("barbed_blade", h, "living_implements", d(3));
		entry("living_axe", h, "living_implements", d(3));
		entry("living_blade", h, "living_implements", d(3));
		entry("living_spear", h, "living_implements", d(3));
		entry("living_staff", h, "living_implements", d(3));
		entry("chitinite_mace", h, "living_implements", d(4));
		entry("living_baghnakh", h, "living_implements", d(4));
		entry("living_crossbow", h, "living_implements", d(4));
		entry("living_flail", h, "living_implements", d(4));
		entry("vivianite_scalpel", h, "living_implements", d(4));
		entry("blood_lust_helm", h, "living_implements", d(5));
		entry("living_torch", h, "living_implements", d(5));
		entry("sanguis_lancea", h, "living_implements", d(5));

		entry("venous_stone", h, "architecture", a());
		entry("hematic_iron_ore", h, "architecture", a());
		entry("polished_venous_stone", h, "architecture", d(2));
		entry("sanguine_glass", h, "architecture", d(2));
		entry("vivianite_glass", h, "architecture", d(2));
		entry("chiseled_hematic_iron_block", h, "architecture", d(3));
		entry("gilded_venous_stone", h, "architecture", d(3));
		entry("polished_venous_stone_bricks", h, "architecture", d(3));
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

	private static void bucket(MaterialAtlasPath path, String id, String label, String rootMaterialId, int color,
			int centerX, int centerY, int plaqueX, int plaqueY) {
		BUCKETS.get(path).add(new MaterialAtlasBucket(path, id, label, rootMaterialId,
				color, centerX, centerY, plaqueX, plaqueY));
	}

	private static void entry(String materialId, MaterialAtlasPath path, String bucketId, MaterialGate gate,
			String... parentIds) {
		entryAt(materialId, path, bucketId, gate, null, null, parentIds);
	}

	private static void entryAt(String materialId, MaterialAtlasPath path, String bucketId, MaterialGate gate,
			int nodeX, int nodeY, String... parentIds) {
		entryAt(materialId, path, bucketId, gate, Integer.valueOf(nodeX), Integer.valueOf(nodeY), parentIds);
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
			for (MaterialAtlasBucket bucket : BUCKETS.get(pathEntry.getKey())) {
				if (!pathEntry.getValue().containsKey(bucket.rootMaterialId())) {
					throw new IllegalStateException("Material atlas bucket " + bucket.id()
							+ " references missing root material " + bucket.rootMaterialId());
				}
			}
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
