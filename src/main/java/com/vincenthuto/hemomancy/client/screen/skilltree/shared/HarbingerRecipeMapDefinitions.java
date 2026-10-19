package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.LegacyBloodStructureIds;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Supplier;

public final class HarbingerRecipeMapDefinitions {
	public static final String SIGIL_FAMILY = "Ichorian Sigils";
	private static final Map<String, RecipeMapPurposeGroup> FAMILY_GROUPS = new LinkedHashMap<>();
	private static final Map<CardinalRitePurpose, String> FLOOR_FAMILIES = new EnumMap<>(CardinalRitePurpose.class);

	static {
		families(RecipeMapPurposeGroup.FLOORLESS, "Floorless Crafts");
		families(RecipeMapPurposeGroup.SELF, "Order", "Body/Will", "Threshold Floors");
		families(RecipeMapPurposeGroup.WORLD, "Domain/World", "Fane", "Qliphoth", "Puppetry", "Trials", "Dominion Floors");
		families(RecipeMapPurposeGroup.MAKING, "Vessel", "Apparatus", "Stations", "Ritual Infrastructure",
				"Constructs/Effigies", "Station Upgrades", "Working Floors");
		families(RecipeMapPurposeGroup.BOND, "Bloodline", "Ancestral", "Communion Floors");
		families(RecipeMapPurposeGroup.REFERENCE, SIGIL_FAMILY, RecipeMapLayout.MISC_FAMILY);
		FLOOR_FAMILIES.put(CardinalRitePurpose.SELF, "Threshold Floors");
		FLOOR_FAMILIES.put(CardinalRitePurpose.WORLD, "Dominion Floors");
		FLOOR_FAMILIES.put(CardinalRitePurpose.MAKING, "Working Floors");
		FLOOR_FAMILIES.put(CardinalRitePurpose.BOND, "Communion Floors");
	}

	/** Every family of the Rites &amp; Crafts map, purpose group by purpose group. */
	public static final List<String> FAMILIES = List.copyOf(FAMILY_GROUPS.keySet());

	private static final Map<String, String> RITES = new LinkedHashMap<>();
	private static final Map<String, String> CRAFTING = new LinkedHashMap<>();
	private static final List<RecipeMapLink> RITE_LINKS = new ArrayList<>();
	private static final List<RecipeMapLink> CRAFTING_LINKS = new ArrayList<>();
	private static final Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> RITE_POSITIONS = new LinkedHashMap<>();
	private static final Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> CRAFTING_POSITIONS = new LinkedHashMap<>();
	private static final Map<RecipeMapKey, Supplier<ItemStack>> RITE_ICONS = new LinkedHashMap<>();
	private static final Map<RecipeMapKey, Supplier<ItemStack>> CRAFTING_ICONS = new LinkedHashMap<>();

	static {
		// <recipe-map-editor>
		registerRites("Order", "initiate_rite", "sanguine_brotherhood",
				"illuminatus_rite", "sanctified_rite", "archon_rite", "apotheos_rite");
		registerRites("Body/Will", "vascular_mending", "sanguine_attunement", "crimson_beacon", "hematic_fortification",
				"eternal_covenant", "chamber_of_will");
		registerRites("Domain/World", "rooted_vein", "hungering_earth", "sanguine_fervor", "sanguine_eclipse", "pallid_shadow");
		registerRites("Fane", "founding_fane", "sanguine_dominion", "hematic_succession", "mnemonic_restoration");
		registerRites("Qliphoth", "bloom_of_qliphoth");
		registerRites("Puppetry", "puppeteer_trial_marrow_spitter", "puppeteer_trial_veinwing_vulture",
				"puppeteer_trial_gorebound_hulk", "puppeteer_trial_mnemonist_puppet");
		registerRites("Trials", "puppeteer_trial_cinder_bellows", "puppeteer_trial_sanguine_hound", "puppeteer_trial_scarlet_mummer");
		registerRites("Vessel", "pallid_vessel_rite", "crimson_vessel_rite", "ashen_vessel_rite", "horn_of_culmination_rite",
				"exsanguination");
		registerRites("Station Upgrades", "first_condensation", "sanguine_athanor", "steady_separation", "second_fraction",
				"true_groove", "enduring_pattern", "armature_consecration", "monolithic_armature",
				"eightfold_script", "palimpsest", "ganglionic_weave", "cortical_weave");
		registerRites("Bloodline", "bloodline_recall", "hematic_unbinding", "bloodline_founding", "scarlet_summons",
				"covenant_vigil");
		registerRites("Ancestral", "ancestral_communion");

		registerCrafting("Floorless Crafts", "dried_gourd", "hematic_iron_block", "iron_brazier", "liber_sanguinum",
				"living_staff", "vial_centrifuge", "ghastly_alembic", "hematic_stake");
		registerCrafting("Apparatus", "mnemonic_reliquary", "somatic_loom", "sporitic_thurible", "morphling_incubator",
				"mycelial_lantern");
		registerCrafting("Ritual Infrastructure", "runic_chisel_station", "visceral_mirror", "dendritic_distributor",
				"sanguine_monolith", "covenant_throne");
		registerCrafting("Constructs/Effigies", "semi_sentient_construct", "mason_effigy", "mind_spike", "vacant_effigy",
				"vascular_effigy");
		registerCrafting("Stations", "enzymatic_scriptorium", "resonant_forge", "scarlet_vanity", "warp_chair",
				"fungal_implantation_pylon");

		iconRites("cardinal_rite/initiate_rite", () -> new ItemStack(ItemInit.liber_sanguinum.get()));
		iconRites("cardinal_rite/sanguine_brotherhood", () -> new ItemStack(ItemInit.sanguine_conduit.get()));
		iconRites("cardinal_rite/illuminatus_rite", () -> new ItemStack(ItemInit.crimson_vestment_fitting.get()));
		iconRites("cardinal_rite/sanctified_rite", () -> new ItemStack(ItemInit.vicars_consecration_kit.get()));
		iconRites("cardinal_rite/archon_rite", () -> new ItemStack(ItemInit.silent_archon_helm.get()));
		iconRites("cardinal_rite/apotheos_rite", () -> new ItemStack(ItemInit.sanguine_quintessence.get()));

		iconRites("cardinal_rite/pallid_vessel_rite", () -> new ItemStack(ItemInit.blood_gourd_white.get()));
		iconRites("cardinal_rite/crimson_vessel_rite", () -> new ItemStack(ItemInit.blood_gourd_red.get()));
		iconRites("cardinal_rite/ashen_vessel_rite", () -> new ItemStack(ItemInit.blood_gourd_black.get()));
		iconRites("cardinal_rite/horn_of_culmination_rite", () -> new ItemStack(ItemInit.curved_horn.get()));

		iconRites("cardinal_rite/bloodline_founding", () -> new ItemStack(ItemInit.unsigned_ancestral_ledger.get()));
		iconRites("cardinal_rite/bloodline_recall", () -> new ItemStack(ItemInit.harbinger_assignment_ledger.get()));
		iconRites("cardinal_rite/founding_fane", () -> new ItemStack(BlockInit.cardinal_focus.get()));
		iconRites("cardinal_rite/hematic_unbinding", () -> new ItemStack(ItemInit.hematic_suture_needle.get()));
		iconRites("cardinal_rite/scarlet_summons", () -> new ItemStack(ItemInit.blood_thrall_effigy.get()));
		iconRites("cardinal_rite/covenant_vigil", () -> new ItemStack(BlockInit.sanguine_vigil.get()));
		iconRites("cardinal_rite/sanguine_dominion", () -> new ItemStack(BlockInit.covenant_throne.get()));

		iconRites("cardinal_rite/sanguine_attunement", () -> new ItemStack(ItemInit.charm_of_vascularium.get()));
		iconRites("cardinal_rite/vascular_mending", () -> new ItemStack(ItemInit.vascular_poultice.get()));
		iconRites("cardinal_rite/hematic_fortification", () -> new ItemStack(ItemInit.hemolytic_plating.get()));
		iconRites("cardinal_rite/eternal_covenant", () -> new ItemStack(ItemInit.covenant_mantle.get()));
		iconRites("cardinal_rite/chamber_of_will", () -> new ItemStack(ItemInit.spawn_egg_will.get()));
		iconRites("cardinal_rite/crimson_beacon", () -> new ItemStack(ItemInit.memory_hematic_beacon.get()));

		iconRites("cardinal_rite/exsanguination", () -> new ItemStack(ItemInit.memory_exsanguinate.get()));
		iconRites("cardinal_rite/rooted_vein", () -> new ItemStack(BlockInit.earthen_vein.get()));
		iconRites("cardinal_rite/hungering_earth", () -> new ItemStack(BlockInit.earthen_vein.get()));
		iconRites("cardinal_rite/sanguine_fervor", () -> new ItemStack(ItemInit.blood_lust_helm.get()));
		iconRites("cardinal_rite/sanguine_eclipse", () -> new ItemStack(ItemInit.memory_blood_eclipse.get()));
		iconRites("cardinal_rite/pallid_shadow", () -> new ItemStack(ItemInit.pallid_icon.get()));

		iconRites("cardinal_rite/ancestral_communion", () -> new ItemStack(ItemInit.memory_of_vesper.get()));
		iconRites("cardinal_rite/bloom_of_qliphoth", () -> new ItemStack(ItemInit.qliphoth_seed.get()));

		iconRites("cardinal_rite/puppeteer_trial_gorebound_hulk", () -> new ItemStack(ItemInit.gorebound_yoke.get()));
		iconRites("cardinal_rite/puppeteer_trial_marrow_spitter", () -> new ItemStack(ItemInit.marrow_spitter_carriage.get()));
		iconRites("cardinal_rite/puppeteer_trial_mnemonist_puppet", () -> new ItemStack(ItemInit.mnemonist_cradle.get()));
		iconRites("cardinal_rite/puppeteer_trial_scarlet_mummer", () -> new ItemStack(ItemInit.scarlet_gorget.get()));
		iconRites("cardinal_rite/puppeteer_trial_veinwing_vulture", () -> new ItemStack(ItemInit.veinwing_harness.get()));
		iconRites("cardinal_rite/puppeteer_trial_cinder_bellows", () -> new ItemStack(ItemInit.cinder_bellows_frame.get()));
		iconRites("cardinal_rite/puppeteer_trial_sanguine_hound", () -> new ItemStack(ItemInit.sanguine_hound_bridle.get()));
		iconRites("cardinal_rite/hematic_succession", () -> new ItemStack(BlockInit.vacant_effigy.get()));
		iconRites("cardinal_rite/mnemonic_restoration", () -> new ItemStack(ItemInit.bound_mnemonic_remnant.get()));

		iconRites("cardinal_rite/first_condensation", () -> new ItemStack(ItemInit.hematic_condenser_kit.get()));
		iconRites("cardinal_rite/sanguine_athanor", () -> new ItemStack(ItemInit.sanguine_athanor_kit.get()));
		iconRites("cardinal_rite/steady_separation", () -> new ItemStack(ItemInit.centrifugal_governor_kit.get()));
		iconRites("cardinal_rite/second_fraction", () -> new ItemStack(ItemInit.fractionating_rotor_kit.get()));
		iconRites("cardinal_rite/true_groove", () -> new ItemStack(ItemInit.precision_governor_kit.get()));
		iconRites("cardinal_rite/enduring_pattern", () -> new ItemStack(ItemInit.master_cam_kit.get()));
		iconRites("cardinal_rite/armature_consecration", () -> new ItemStack(ItemInit.vicars_consecration_kit.get()));
		iconRites("cardinal_rite/monolithic_armature", () -> new ItemStack(ItemInit.monolithic_cornerstone.get()));
		iconRites("cardinal_rite/eightfold_script", () -> new ItemStack(ItemInit.rubricators_quill.get()));
		iconRites("cardinal_rite/palimpsest", () -> new ItemStack(ItemInit.palimpsest_burin.get()));
		iconRites("cardinal_rite/ganglionic_weave", () -> new ItemStack(ItemInit.ganglionic_harness.get()));
		iconRites("cardinal_rite/cortical_weave", () -> new ItemStack(ItemInit.cortical_crown.get()));

		linkRites("cardinal_rite/initiate_rite", "cardinal_rite/sanguine_brotherhood", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_brotherhood", "cardinal_rite/illuminatus_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/illuminatus_rite", "cardinal_rite/sanctified_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/sanctified_rite", "cardinal_rite/archon_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/archon_rite", "cardinal_rite/apotheos_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/crimson_vessel_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/crimson_vessel_rite", "cardinal_rite/ashen_vessel_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/ashen_vessel_rite", "cardinal_rite/horn_of_culmination_rite", RecipeMapLink.Kind.PROGRESSION);

		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/ashen_vessel_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/horn_of_culmination_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/bloodline_recall", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/hematic_unbinding", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/scarlet_summons", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/founding_fane", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/founding_fane", "cardinal_rite/sanguine_dominion", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/founding_fane", "cardinal_rite/hematic_succession", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/founding_fane", "cardinal_rite/covenant_vigil", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/hematic_succession", "cardinal_rite/mnemonic_restoration", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/vascular_mending", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/hematic_fortification", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/eternal_covenant", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/chamber_of_will", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/crimson_beacon", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/rooted_vein", "cardinal_rite/exsanguination", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/hungering_earth", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/sanguine_fervor", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/sanguine_eclipse", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/pallid_shadow", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/ancestral_communion", "cardinal_rite/bloom_of_qliphoth", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/puppeteer_trial_marrow_spitter", "cardinal_rite/puppeteer_trial_gorebound_hulk", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/puppeteer_trial_veinwing_vulture", "cardinal_rite/puppeteer_trial_gorebound_hulk", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/puppeteer_trial_gorebound_hulk", "cardinal_rite/puppeteer_trial_mnemonist_puppet", RecipeMapLink.Kind.CONCEPTUAL);

		linkCrafting("blood_structure/dried_gourd", "blood_structure/hematic_iron_block", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/dried_gourd", "blood_structure/iron_brazier", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/dried_gourd", "blood_structure/liber_sanguinum", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/dried_gourd", "blood_structure/living_staff", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/ghastly_alembic", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/mnemonic_reliquary", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/ghastly_alembic", "blood_structure/somatic_loom", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/mycelial_lantern", "blood_structure/sporitic_thurible", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/morphling_incubator", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/mycelial_lantern", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/visceral_mirror", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/hematic_stake", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/dendritic_distributor", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/covenant_throne", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/sanguine_monolith", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/sanguine_monolith", "blood_structure/covenant_throne", RecipeMapLink.Kind.PROGRESSION);
		linkCrafting("blood_structure/semi_sentient_construct", "blood_structure/mason_effigy", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/semi_sentient_construct", "blood_structure/mind_spike", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/semi_sentient_construct", "blood_structure/vacant_effigy", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vacant_effigy", "blood_structure/vascular_effigy", RecipeMapLink.Kind.CONCEPTUAL);
		// </recipe-map-editor>
	}

	private HarbingerRecipeMapDefinitions() {}

	public static Set<String> ritePaths() { return Set.copyOf(RITES.keySet()); }
	public static Set<String> craftingPaths() {
		return CRAFTING.keySet().stream().filter(path -> path.startsWith("blood_structure/")
				|| path.startsWith(LegacyBloodStructureIds.WORKING_PREFIX)).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}
	public static Set<String> puppetryPaths() {
		return RITES.entrySet().stream().filter(entry -> "Puppetry".equals(entry.getValue()))
				.map(Map.Entry::getKey).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}
	public static String riteFamily(String path) { return RITES.getOrDefault(path, RecipeMapLayout.MISC_FAMILY); }
	public static String craftingFamily(String path) {
		return CRAFTING.getOrDefault(LegacyBloodStructureIds.remapPath(path), RecipeMapLayout.MISC_FAMILY);
	}
	public static int riteOrder(String path) { return orderWithinFamily(RITES, path); }
	public static int craftingOrder(String path) { return orderWithinFamily(CRAFTING, LegacyBloodStructureIds.remapPath(path)); }
	public static List<RecipeMapLink> riteLinks() { return List.copyOf(RITE_LINKS); }
	public static List<RecipeMapLink> craftingLinks() { return List.copyOf(CRAFTING_LINKS); }
	/** Rite and craft links together, for the merged map. */
	public static List<RecipeMapLink> links() {
		List<RecipeMapLink> links = new ArrayList<>(RITE_LINKS);
		links.addAll(CRAFTING_LINKS);
		return links;
	}
	/** The purpose group a family belongs to; unknown families count as reference. */
	public static RecipeMapPurposeGroup group(String family) {
		return FAMILY_GROUPS.getOrDefault(family, RecipeMapPurposeGroup.REFERENCE);
	}
	/** The floors family of a purpose: where its standard and special floors are listed. */
	public static String floorFamily(CardinalRitePurpose purpose) {
		return purpose == null ? RecipeMapLayout.MISC_FAMILY : FLOOR_FAMILIES.get(purpose);
	}
	public static Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> ritePositions() { return Map.copyOf(RITE_POSITIONS); }
	public static Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> craftingPositions() { return Map.copyOf(CRAFTING_POSITIONS); }
	public static ItemStack riteIcon(String path, ItemStack fallback) { return resolveIcon(RITE_ICONS.get(riteKey(path)), fallback); }
	public static ItemStack craftingIcon(String path, ItemStack fallback) { return resolveIcon(CRAFTING_ICONS.get(craftingKey(path)), fallback); }

	private static void families(RecipeMapPurposeGroup group, String... families) {
		for (String family : families) FAMILY_GROUPS.put(family, group);
	}

	private static void registerRites(String family, String... paths) {
		for (String path : paths) RITES.put("cardinal_rite/" + path, family);
	}

	private static void registerCrafting(String family, String... paths) {
		for (String path : paths) CRAFTING.put(LegacyBloodStructureIds.remapPath("blood_structure/" + path), family);
	}

	private static void linkRites(String from, String to, RecipeMapLink.Kind kind) {
		RITE_LINKS.add(new RecipeMapLink(riteKey(from), riteKey(to), kind));
	}

	private static void linkCrafting(String from, String to, RecipeMapLink.Kind kind) {
		CRAFTING_LINKS.add(new RecipeMapLink(craftingKey(from), craftingKey(to), kind));
	}

	private static void positionRites(String path, int x, int y) {
		RITE_POSITIONS.put(riteKey(path), new RecipeMapLayout.AuthoredPosition(x, y));
	}

	private static void positionCrafting(String path, int x, int y) {
		CRAFTING_POSITIONS.put(craftingKey(path), new RecipeMapLayout.AuthoredPosition(x, y));
	}

	private static void iconRites(String path, Supplier<ItemStack> icon) {
		RITE_ICONS.put(riteKey(path), icon);
	}

	private static void iconCrafting(String path, Supplier<ItemStack> icon) {
		CRAFTING_ICONS.put(craftingKey(path), icon);
	}

	private static ItemStack resolveIcon(Supplier<ItemStack> icon, ItemStack fallback) {
		ItemStack resolved = resolveDisplayOverride(icon, fallback);
		return resolved == null ? ItemStack.EMPTY : resolved.copy();
	}

	static <T> T resolveDisplayOverride(Supplier<T> override, T fallback) {
		return override == null ? fallback : override.get();
	}

	private static int orderWithinFamily(Map<String, String> definitions, String path) {
		String family = definitions.get(path);
		if (family == null) return Integer.MAX_VALUE;
		int order = 0;
		for (Map.Entry<String, String> entry : definitions.entrySet()) {
			if (family.equals(entry.getValue())) {
				if (path.equals(entry.getKey())) return order;
				order++;
			}
		}
		return Integer.MAX_VALUE;
	}

	private static RecipeMapKey riteKey(String path) {
		String fullPath = path.startsWith("cardinal_rite/") ? path : "cardinal_rite/" + path;
		return new RecipeMapKey(RecipeMapEntry.Kind.RITE,
				ResourceLocation.fromNamespaceAndPath("hemomancy", fullPath));
	}

	private static RecipeMapKey craftingKey(String path) {
		return new RecipeMapKey(RecipeMapEntry.Kind.CRAFTING,
				ResourceLocation.fromNamespaceAndPath("hemomancy", LegacyBloodStructureIds.remapPath(path)));
	}
}
