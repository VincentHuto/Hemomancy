package com.vincenthuto.hemomancy.common.init;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

/**
 * The banded sections of the main creative tab, in display order. {@link #HEMATIC}, the Harbinger main line, holds
 * what the {@link CreativeTabGroup} lists name (plus any blood memory not filed elsewhere), in their order. The other
 * sections list their entries here and keep registry order. Anything filed nowhere falls to {@link #MISC} at the
 * bottom. Every section ends with its spawn eggs. The client draws one banner per
 * section and starts each group, and the eggs, on a fresh row ({@code client/screen/inventory/CreativeTabChrome}).
 */
public enum CreativeTabSection {
	HEMATIC("hematic", Set.of()),
	UNSTAINED("unstained", Set.of(
			"hemolytic_solution", "hemolytic_plating", "neutralizing_gasket", "consecrated_copper_ingot",
			"pale_humor_flask", "hemolytic_vial", "draught_of_still_waters", "pallid_infusion", "lethean_dew",
			"tears_of_silthmere", "lethean_poppy_wreath", "silver_chalice", "pallid_icon", "pale_silver_ingot",
			"pale_distillate", "unstained_helm", "unstained_chestplate", "unstained_leggings", "unstained_boots","vestment_of_the_final_molt",
			"liber_immaculatus", "ahaematic_colloid", "lethean_chalice", "unstained_warhammer", "unstained_shield",
			"absolution_dagger", "annettas_absolution_dagger", "silthmere_glaive", "pale_silver_bell",
			"cleansed_blood_crystal_shard", "cleansed_sanguine_glass", "cleansed_sanguine_pane", "cleansed_stone",
			"suspended_cleansed_blood_crystal", "hemolytic_plating_block", "pale_silver_block", "pale_silver_bars",
			"pale_silver_bells", "pallid_lantern", "pallid_silver_chain", "lethean_poppy", "ghost_pipe",
			"unstained_podium", "altar_of_cleansing", "pallid_retort", "stillwater_condenser", "verdigris_lattice",
			"virid_salis_trail", "self_reflection_mirror", "spawn_egg_verdigris_moth")),
	CIRCUS("circus", Set.of(
			"circus_waybill", "puppeteering_thread", "veinwing_harness", "cinder_bellows_frame",
			"marrow_spitter_carriage", "gorebound_yoke", "mnemonist_cradle", "scarlet_gorget", "sanguine_hound_bridle",
			"tendon_line", "ringmaster_topper", "marionette_crossbar", "puppeteers_wool", "puppeteers_spindle",
			"circus_stage_record", "circus_loft_record", "circus_quarters_record", "circus_carousel_record",
			"laughing_mask", "mourning_mask", "blank_mask", "troupe_rigging", "circus_curtain",
			"spawn_egg_blood_drunk_puppeteer", "spawn_egg_enthralled_doll", "spawn_egg_veinwing_vulture",
			"spawn_egg_marrow_spitter", "spawn_egg_gorebound_hulk", "spawn_egg_mnemonist_puppet",
			"spawn_egg_scarlet_mummer", "spawn_egg_sanguine_hound")),
	BRINED_COURT("brined_court", withMorphlings(
			"raw_pelagic_herring", "cooked_pelagic_herring", "cuttlefish_chromatophores", "erythrocoral_fragment",
			"mnemonic_ambergris", "salt_stained_voyager_log", "relay_chart", "brine_sealed_letter", "chalybeate_sclerite", "blood_chum", "erythrocoral_block", "calcified_erythrocoral", "mnemonic_ambergris_block",
			"hematic_algal_crust", "tidepool_anemone", "bone_worm_colony", "giant_tube_worm_colony",
			"erythrocoral_fan", "erythrocoral_tendril",
			// The Court's set pieces, placeable for building and testing (review fixes 2026-10-30).
			"sunburst_bloom", "sunburst_frond", "sunburst_crust", "sunburst_lamp", "petrified_follower", "house_marker", "relay_marker", "messenger_buoy",
			"holdfast_root_mass", "nest_cup", "fold_veil", "rope_pod", "current_tissue", "current_strand", "sclerotium_shell",
			"bell_sleeper", "marrow_plume", "kindled_tissue", "hydra_crown", "vent_offering_basin", "marimo_moss", "marimo_heart",
			"spawn_egg_barbed_urchin", "spawn_egg_chiton",
			"spawn_egg_pyrosome", "spawn_egg_pelagic_herring", "spawn_egg_ice_fish", "spawn_egg_siphonophore",
			"spawn_egg_bloody_belly_comb_jelly", "spawn_egg_hagfish", "spawn_egg_vampire_squid",
			"spawn_egg_chalybeate_snail", "spawn_egg_hemojelly", "spawn_egg_blood_lantern_jelly",
			"spawn_egg_mnemonic_whale", "spawn_egg_brined_votary", "spawn_egg_prism_cuttle",
			"spawn_egg_harbinger_voyager", "spawn_egg_harbinger_votary_wayfarer")),
	SAINTS("saints", Set.of(
			"hallowed_residuum_fragment_hemorath", "hallowed_residuum_fragment_seraphae",
			"hallowed_residuum_fragment_putriciel", "hallowed_residuum_fragment_velorum", "recollection_hemorath",
			"recollection_seraphae", "recollection_putriciel", "recollection_velorum", "velorum_mask", "hemorath_rib",
			"consecrated_syringe", "hallowed_residuum_hemorath", "hallowed_residuum_seraphae",
			"hallowed_residuum_putriciel", "hallowed_residuum_velorum", "saint_relic_hemorath", "saint_relic_seraphae",
			"saint_relic_putriciel", "saint_relic_velorum", "memory_crimson_tithe", "memory_unclosing_eye",
			"memory_bloom_of_rot", "memory_endless_hour", "saint_sarcophagus", "blood_basin",
			"bone_bricks", "cracked_bone_bricks", "chiseled_bone_bricks", "bone_brick_wall", "bone_brick_slab",
			"bone_brick_stairs", "healed_quartz_bricks", "healed_calcite", "radiant_healed_quartz", "bone_outgrowth",
			"packed_ash", "charred_planks", "charred_planks_slab", "charred_planks_stairs", "charred_log",
			"charred_bookshelf", "sodden_mud_bricks", "spore_shelf", "rimed_deepslate_bricks",
			"rimed_deepslate_brick_wall", "rimed_deepslate_brick_slab", "rimed_deepslate_brick_stairs",
			"rime_lantern", "hanging_icicle", "frozen_drip", "adipocere_crust", "dark_lacquer_planks", "dark_lacquer_planks_slab",
			"dark_lacquer_planks_stairs", "blood_gutter", "venous_basin", "ossuary_clock",
			// The mausoleum set pieces, placeable for building and testing (review fixes 2026-10-30).
			"sealed_niche", "opened_niche", "reliquary_sconce", "rot_pyre", "frozen_devotee", "containment_anchor_block",
			"chapel_focus", "witness_stone", "litany_stone", "offering_stand", "pulse_pylon", "spawn_mark")),
	GARDENS("gardens", Set.of(
			"qliphoth_seed", "qliphoth_pome", "monolith_fragment", "monolith_imbued_cloth", "memory_of_vesper",
			"orb_of_perspective", "fungal_spine", "fruiting_lure", "morphic_tonic","mycophant_tendril",
			"silent_archon_helm", "silent_archon_chestplate", "silent_archon_leggings", "silent_archon_boots",
			"erythrocytic_dirt", "erythrocytic_mycelium", "mycelium_erythrocytic_dirt", "infected_stem",
			"hyphae_block", "infected_cap", "fruiting_infected_cap", "calcified_hyphae", "hemorrhagic_crust",
			"sporite_crystal", "conscious_mass", "hyphae", "infected_fungus", "puffball_fungus", "fungal_gill",
			"heart_node", "spawn_egg_fungling", "spawn_egg_eruptor", "spawn_egg_lump_of_thought",
			"spawn_egg_abhorrent_thought", "spawn_egg_facsimile_bat", "spawn_egg_facsimile_enderman",
			"spawn_egg_facsimile_blaze", "spawn_egg_facsimile_golem", "spawn_egg_facsimile_horse",
			"spawn_egg_facsimile_donkey", "spawn_egg_facsimile_wolf", "spawn_egg_facsimile_spider",
			"spawn_egg_antibody", "spawn_egg_hunter_fungus")),
	NETHER("nether", Set.of(
			"gore_wound_harpoon", "blood_scorched_scab", "pointed_blood_scorched_scab", "escharian_scyphus",
			"spawn_egg_phlegethosoma",
			"spawn_egg_excoriated", "spawn_egg_phlegethontic_bombardier", "spawn_egg_osteophage")),
	END("end", Set.of(
			"neural_tissue", "axonal_slate", "myelin_sheath", "ganglion_matter", "cortical_folds", "nerve_fiber",
			"nerve_bundle", "synaptic_node", "dura_membrane", "dendrite_growth","ganglion_cluster", "naeglerophaeon_ganglion", "spawn_egg_choir_keeper",
			"spawn_egg_myelin_borer")),
	MISC("misc", Set.of());

	/**
	 * The Brined Court's own ids plus the Morphling jar, the polyp and the eight house strains, which the user files
	 * with the Court (they are also listed in {@link CreativeTabGroup#MORPHLINGS}; the section wins).
	 */
	private static Set<String> withMorphlings(String... paths) {
		var all = new LinkedHashSet<>(List.of(paths));
		all.add("morphling_jar");
		all.add("morphling_polyp");
		all.addAll(MorphlingStrains.itemPaths());
		return Set.copyOf(all);
	}

	/** Entries shown straight after another entry of their section instead of in registry order. */
	private static final Map<String, String> PLACED_AFTER = Map.of("hemorath_rib", "velorum_mask");

	private static final Map<ResourceLocation, CreativeTabSection> BY_ID = new HashMap<>();

	static {
		for (CreativeTabSection section : values()) {

			for (String path : section.paths) {
				CreativeTabSection previous = BY_ID.put(Hemomancy.rloc(path), section);
				if (previous != null) {
					throw new IllegalStateException(path + " is filed under both " + previous + " and " + section);
				}
			}
		}
	}

	private final String id;
	private final Set<String> paths;

	CreativeTabSection(String id, Set<String> paths) {
		this.id = id;
		this.paths = paths;
	}

	public String id() {
		return id;
	}

	/**
	 * The item ids filed under this section by hand; empty for {@link #HEMATIC}, which reads
	 * {@link CreativeTabGroup}, and for {@link #MISC}, which takes the rest.
	 */
	public Set<String> paths() {
		return paths;
	}

	public Component title() {
		return Component.translatable("item_group." + Hemomancy.MOD_ID + ".hemomancytab.section." + id);
	}

	public static CreativeTabSection of(Item item) {
		CreativeTabSection filed = BY_ID.get(BuiltInRegistries.ITEM.getKey(item));
		if (filed != null) {
			return filed;
		}
		return CreativeTabGroup.of(item) != null ? HEMATIC : MISC;
	}

	public static CreativeTabSection of(ItemStack stack) {
		return of(stack.getItem());
	}

	/** Entries sharing a run start on one row and keep together: a Hematic group, or one section's spawn eggs. */
	public static int run(ItemStack stack) {
		if (stack.getItem() instanceof SpawnEggItem) {
			return -1;
		}
		return of(stack) == HEMATIC ? CreativeTabGroup.of(stack.getItem()).ordinal() : 0;
	}

	/**
	 * Collects the main tab's entries in registry order, then hands them on grouped by section, each section's
	 * spawn eggs after its items and blocks.
	 */
	public static final class Sorter implements CreativeModeTab.Output {
		private record Entry(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
		}

		private final Map<CreativeTabSection, List<Entry>> items = new EnumMap<>(CreativeTabSection.class);
		private final Map<CreativeTabSection, List<Entry>> eggs = new EnumMap<>(CreativeTabSection.class);

		@Override
		public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
			var target = stack.getItem() instanceof SpawnEggItem ? eggs : items;
			target.computeIfAbsent(of(stack), section -> new ArrayList<>()).add(new Entry(stack, visibility));
		}

		public void emitTo(CreativeModeTab.Output output) {
			// Stable: ties keep registry order.
			Comparator<Entry> hematicOrder = Comparator
					.comparingInt((Entry entry) -> CreativeTabGroup.of(entry.stack.getItem()).ordinal())
					.thenComparingInt(entry -> CreativeTabGroup.orderWithinGroup(entry.stack.getItem()));
			items.getOrDefault(HEMATIC, new ArrayList<>()).sort(hematicOrder);
			eggs.getOrDefault(HEMATIC, new ArrayList<>()).sort(hematicOrder);
			items.values().forEach(Sorter::placeAfterAnchors);
			for (CreativeTabSection section : values()) {
				items.getOrDefault(section, List.of()).forEach(entry -> output.accept(entry.stack, entry.visibility));
				eggs.getOrDefault(section, List.of()).forEach(entry -> output.accept(entry.stack, entry.visibility));
			}
		}

		private static void placeAfterAnchors(List<Entry> entries) {
			PLACED_AFTER.forEach((path, anchorPath) -> {
				int from = indexOf(entries, path);
				int anchor = indexOf(entries, anchorPath);
				if (from < 0 || anchor < 0) {
					return;
				}
				Entry moved = entries.remove(from);
				entries.add(indexOf(entries, anchorPath) + 1, moved);
			});
		}

		private static int indexOf(List<Entry> entries, String path) {
			ResourceLocation id = Hemomancy.rloc(path);
			for (int i = 0; i < entries.size(); i++) {
				if (BuiltInRegistries.ITEM.getKey(entries.get(i).stack.getItem()).equals(id)) {
					return i;
				}
			}
			return -1;
		}
	}
}
