package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Supplier;

public final class HarbingerRecipeMapDefinitions {
	public static final List<String> RITE_FAMILIES = List.of("Order", "Vessel", "Bloodline/Fane",
			"Body/Will", "Domain/World", "Qliphoth/Forbidden", "Puppetry", RecipeMapLayout.MISC_FAMILY);
	public static final List<String> CRAFTING_FAMILIES = List.of("Foundations", "Apparatus",
			"Ritual Infrastructure", "Constructs/Effigies", RecipeMapLayout.MISC_FAMILY);

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
		registerRites("Order", "sanguine_initiation", "votary_rite", "initiate_rite", "sanguine_brotherhood",
				"illuminatus_rite", "sanctified_rite", "archon_rite", "apotheos_rite");
		registerRites("Vessel", "pallid_vessel_rite", "crimson_vessel_rite", "ashen_vessel_rite", "horn_of_culmination_rite");
		registerRites("Bloodline/Fane", "bloodline_founding", "bloodline_recall", "founding_fane", "hematic_unbinding",
				"scarlet_summons", "covenant_vigil", "sanguine_dominion");
		registerRites("Body/Will", "sanguine_attunement", "vascular_mending", "hematic_fortification", "eternal_covenant",
				"chamber_of_will", "crimson_beacon");
		registerRites("Domain/World", "exsanguination", "rooted_vein", "hungering_earth", "sanguine_fervor", "sanguine_eclipse", "pallid_shadow");
		registerRites("Qliphoth/Forbidden", "ancestral_communion", "bloom_of_qliphoth");

		registerCrafting("Foundations", "dried_gourd", "hematic_iron_block", "iron_brazier", "liber_sanguinum", "living_staff");
		registerCrafting("Apparatus", "vial_centrifuge", "ghastly_alembic", "mnemonic_reliquary", "somatic_loom",
				"sporitic_thurible", "morphling_incubator", "mycelial_lantern");
		registerCrafting("Ritual Infrastructure", "runic_chisel_station", "visceral_mirror", "consecrated_bloodwell",
				"dendritic_distributor", "covenant_throne", "sanguine_monolith");
		registerCrafting("Constructs/Effigies", "semi_sentient_construct", "mason_effigy", "mind_spike", "vascular_effigy");
		registerRites("Puppetry", "puppeteer_trial_gorebound_hulk", "puppeteer_trial_marrow_spitter",
				"puppeteer_trial_mnemonist_puppet", "puppeteer_trial_veinwing_vulture");

		iconRites("cardinal_rite/sanguine_initiation", () -> new ItemStack(ItemInit.sanguine_formation.get()));
		iconRites("cardinal_rite/votary_rite", () -> new ItemStack(ItemInit.mnemonic_blueprint.get()));
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
		iconRites("cardinal_rite/bloom_of_qliphoth", () -> new ItemStack(BlockInit.qliphoth_bloom.get()));

		iconRites("cardinal_rite/puppeteer_trial_gorebound_hulk", () -> new ItemStack(ItemInit.gorebound_yoke.get()));
		iconRites("cardinal_rite/puppeteer_trial_marrow_spitter", () -> new ItemStack(ItemInit.marrow_spitter_carriage.get()));
		iconRites("cardinal_rite/puppeteer_trial_mnemonist_puppet", () -> new ItemStack(ItemInit.mnemonist_cradle.get()));
		iconRites("cardinal_rite/puppeteer_trial_scarlet_mummer", () -> new ItemStack(ItemInit.scarlet_gorget.get()));
		iconRites("cardinal_rite/puppeteer_trial_veinwing_vulture", () -> new ItemStack(ItemInit.veinwing_harness.get()));

		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/votary_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/votary_rite", "cardinal_rite/initiate_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/initiate_rite", "cardinal_rite/sanguine_brotherhood", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/sanguine_brotherhood", "cardinal_rite/illuminatus_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/illuminatus_rite", "cardinal_rite/sanctified_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/sanctified_rite", "cardinal_rite/archon_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/archon_rite", "cardinal_rite/apotheos_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/crimson_vessel_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/crimson_vessel_rite", "cardinal_rite/ashen_vessel_rite", RecipeMapLink.Kind.PROGRESSION);
		linkRites("cardinal_rite/ashen_vessel_rite", "cardinal_rite/horn_of_culmination_rite", RecipeMapLink.Kind.PROGRESSION);

		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/votary_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/initiate_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/sanguine_brotherhood", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/illuminatus_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/sanctified_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/archon_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_initiation", "cardinal_rite/apotheos_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/crimson_vessel_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/ashen_vessel_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/pallid_vessel_rite", "cardinal_rite/horn_of_culmination_rite", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/bloodline_recall", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/founding_fane", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/hematic_unbinding", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/scarlet_summons", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/covenant_vigil", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/bloodline_founding", "cardinal_rite/sanguine_dominion", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/vascular_mending", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/hematic_fortification", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/eternal_covenant", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/chamber_of_will", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/sanguine_attunement", "cardinal_rite/crimson_beacon", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/hungering_earth", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/rooted_vein", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/sanguine_fervor", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/sanguine_eclipse", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/exsanguination", "cardinal_rite/pallid_shadow", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/ancestral_communion", "cardinal_rite/bloom_of_qliphoth", RecipeMapLink.Kind.CONCEPTUAL);

		linkCrafting("blood_structure/dried_gourd", "blood_structure/hematic_iron_block", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/dried_gourd", "blood_structure/iron_brazier", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/dried_gourd", "blood_structure/liber_sanguinum", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/dried_gourd", "blood_structure/living_staff", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/ghastly_alembic", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/mnemonic_reliquary", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/somatic_loom", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/sporitic_thurible", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/morphling_incubator", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/vial_centrifuge", "blood_structure/mycelial_lantern", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/visceral_mirror", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/consecrated_bloodwell", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/dendritic_distributor", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/covenant_throne", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/runic_chisel_station", "blood_structure/sanguine_monolith", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/semi_sentient_construct", "blood_structure/mason_effigy", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/semi_sentient_construct", "blood_structure/mind_spike", RecipeMapLink.Kind.CONCEPTUAL);
		linkCrafting("blood_structure/semi_sentient_construct", "blood_structure/vascular_effigy", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/puppeteer_trial_gorebound_hulk", "cardinal_rite/puppeteer_trial_marrow_spitter", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/puppeteer_trial_gorebound_hulk", "cardinal_rite/puppeteer_trial_mnemonist_puppet", RecipeMapLink.Kind.CONCEPTUAL);
		linkRites("cardinal_rite/puppeteer_trial_gorebound_hulk", "cardinal_rite/puppeteer_trial_veinwing_vulture", RecipeMapLink.Kind.CONCEPTUAL);
		// </recipe-map-editor>
	}

	private HarbingerRecipeMapDefinitions() {}

	public static Set<String> ritePaths() { return Set.copyOf(RITES.keySet()); }
	public static Set<String> craftingPaths() {
		return CRAFTING.keySet().stream().filter(path -> path.startsWith("blood_structure/")).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}
	public static Set<String> puppetryPaths() {
		return RITES.entrySet().stream().filter(entry -> "Puppetry".equals(entry.getValue()))
				.map(Map.Entry::getKey).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}
	public static String riteFamily(String path) { return RITES.getOrDefault(path, RecipeMapLayout.MISC_FAMILY); }
	public static String craftingFamily(String path) { return CRAFTING.getOrDefault(path, RecipeMapLayout.MISC_FAMILY); }
	public static int riteOrder(String path) { return orderWithinFamily(RITES, path); }
	public static int craftingOrder(String path) { return orderWithinFamily(CRAFTING, path); }
	public static List<RecipeMapLink> riteLinks() { return List.copyOf(RITE_LINKS); }
	public static List<RecipeMapLink> craftingLinks() { return List.copyOf(CRAFTING_LINKS); }
	public static Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> ritePositions() { return Map.copyOf(RITE_POSITIONS); }
	public static Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> craftingPositions() { return Map.copyOf(CRAFTING_POSITIONS); }
	public static ItemStack riteIcon(String path, ItemStack fallback) { return resolveIcon(RITE_ICONS.get(riteKey(path)), fallback); }
	public static ItemStack craftingIcon(String path, ItemStack fallback) { return resolveIcon(CRAFTING_ICONS.get(craftingKey(path)), fallback); }

	private static void registerRites(String family, String... paths) {
		for (String path : paths) RITES.put("cardinal_rite/" + path, family);
	}

	private static void registerCrafting(String family, String... paths) {
		for (String path : paths) CRAFTING.put("blood_structure/" + path, family);
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
				ResourceLocation.fromNamespaceAndPath("hemomancy", path));
	}
}
