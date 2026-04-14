package com.vincenthuto.hemomancy.common.init;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class PlacedFeatureInit {
	public static final ResourceKey<PlacedFeature> HYPHAE_TENDRIL = createKey("hyphae_tendril");
	public static final ResourceKey<PlacedFeature> HUGE_FUNGUS = createKey("huge_fungus");
	public static final ResourceKey<PlacedFeature> SMALL_INFECTED_FUNGUS = createKey("small_infected_fungus");
	public static final ResourceKey<PlacedFeature> PATCH_HYPHAE = createKey("patch_hyphae");
	public static final ResourceKey<PlacedFeature> PLACED_MYCELIUM_BLOB = createKey("mycelium_blob");
	public static final ResourceKey<PlacedFeature> PLACED_INFESTED_VENOUS_STONE_BLOB = createKey(
			"infested_venous_stone_blob");

	public static final ResourceKey<PlacedFeature> PLACED_CANOPY_MUSHROOMS_SPARSE = createKey(
			"mushroom/canopy_mushrooms_sparse");
	public static final ResourceKey<PlacedFeature> PLACED_CANOPY_MUSHROOMS_DENSE = createKey(
			"mushroom/canopy_mushrooms_dense");

	// Plants
	public static final ResourceKey<PlacedFeature> BLEEDING_HEARTS = createKey("bleeding_hearts");

	public static final ResourceKey<PlacedFeature> STINK_HORNS = createKey("stink_horns");

	public static final ResourceKey<PlacedFeature> LETHEAN_POPPIES = createKey("lethean_poppies");

	public static final ResourceKey<PlacedFeature> GHOST_PIPES = createKey("ghost_pipes");

	public static final ResourceKey<PlacedFeature> SARCODES = createKey("sarcodes");

	public static final ResourceKey<PlacedFeature> RAFFLESIA = createKey("rafflesia");

	public static final ResourceKey<PlacedFeature> BOG_BODY = createKey("bog_body");

	public static final ResourceKey<PlacedFeature> TERMITE_MOUND = createKey("termite_mound");

	// Conscious mass blob
	public static final ResourceKey<PlacedFeature> PLACED_CONSCIOUS_MASS_BLOB = createKey("conscious_mass_blob");

	// Fungal dimension ores
	public static final ResourceKey<PlacedFeature> ORE_HEMATIC_IRON = createKey("ore_hematic_iron");

	// Spore Nexus Tower - rare mega-structure
	public static final ResourceKey<PlacedFeature> SPORE_NEXUS_TOWER = createKey("spore_nexus_tower");

	// Sporite Crystal cluster patches (for Mycelial Depths)
	public static final ResourceKey<PlacedFeature> SPORITE_CRYSTAL_CLUSTER = createKey("sporite_crystal_cluster");

	public static void bootstrap(BootstapContext<PlacedFeature> context) {

		HolderGetter<ConfiguredFeature<?, ?>> configuredFeatureGetter = context.lookup(Registries.CONFIGURED_FEATURE);

		final Holder<ConfiguredFeature<?, ?>> HYPHAE_TENDRIL = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.HYPHAE_TENDRIL);

		final Holder<ConfiguredFeature<?, ?>> HUGE_FUNGUS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.HUGE_FUNGUS);

		final Holder<ConfiguredFeature<?, ?>> SMALL_INFECTED_FUNGUS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.SMALL_INFECTED_FUNGUS);

		//Plants
		final Holder<ConfiguredFeature<?, ?>> BLEEDING_HEARTS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.BLEEDING_HEARTS);

		register(context, PlacedFeatureInit.BLEEDING_HEARTS, BLEEDING_HEARTS, RarityFilter.onAverageOnceEvery(7),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

		final Holder<ConfiguredFeature<?, ?>> STINK_HORNS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.STINK_HORNS);

		register(context, PlacedFeatureInit.STINK_HORNS, STINK_HORNS, RarityFilter.onAverageOnceEvery(7),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

		final Holder<ConfiguredFeature<?, ?>> LETHEAN_POPPIES = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.LETHEAN_POPPIES);

		register(context, PlacedFeatureInit.LETHEAN_POPPIES, LETHEAN_POPPIES, RarityFilter.onAverageOnceEvery(8),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, CountPlacement.of(3), BiomeFilter.biome());

		// Myco-heterotrophic plants — spawn under trees / in the dark
		final Holder<ConfiguredFeature<?, ?>> GHOST_PIPES = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.GHOST_PIPES);

		register(context, PlacedFeatureInit.GHOST_PIPES, GHOST_PIPES, RarityFilter.onAverageOnceEvery(10),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

		final Holder<ConfiguredFeature<?, ?>> SARCODES = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.SARCODES);

		register(context, PlacedFeatureInit.SARCODES, SARCODES, RarityFilter.onAverageOnceEvery(12),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

		final Holder<ConfiguredFeature<?, ?>> RAFFLESIA = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.RAFFLESIA);

		register(context, PlacedFeatureInit.RAFFLESIA, RAFFLESIA, RarityFilter.onAverageOnceEvery(50),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());

		// Bog body — underwater on the ocean floor, rare
		final Holder<ConfiguredFeature<?, ?>> BOG_BODY = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.BOG_BODY);

		register(context, PlacedFeatureInit.BOG_BODY, BOG_BODY, RarityFilter.onAverageOnceEvery(14),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());

		// Termite Mound — rare surface structure in savannahs
		final Holder<ConfiguredFeature<?, ?>> TERMITE_MOUND = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.TERMITE_MOUND);

		register(context, PlacedFeatureInit.TERMITE_MOUND, TERMITE_MOUND, RarityFilter.onAverageOnceEvery(60),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());


		
		//Blobs
		context.register(PLACED_MYCELIUM_BLOB,
				new PlacedFeature(configuredFeatureGetter.getOrThrow(ConfiguredFeatureInit.MYCELIUM_BLOB),
						ImmutableList.<PlacementModifier>builder().add(RarityFilter.onAverageOnceEvery(3),
								InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())
								.build()));

		context.register(PLACED_INFESTED_VENOUS_STONE_BLOB,
				new PlacedFeature(configuredFeatureGetter.getOrThrow(ConfiguredFeatureInit.INFESTED_VENOUS_STONE_BLOB),
						ImmutableList.<PlacementModifier>builder().add(RarityFilter.onAverageOnceEvery(3),
								InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())
								.build()));

		register(context, PlacedFeatureInit.HYPHAE_TENDRIL, HYPHAE_TENDRIL, List.of(CountPlacement.of(50),
				InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()));
		register(context, PlacedFeatureInit.HUGE_FUNGUS, HUGE_FUNGUS, List.of(CountPlacement.of(50),
				InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()));

		register(context, PlacedFeatureInit.SMALL_INFECTED_FUNGUS, SMALL_INFECTED_FUNGUS,
				RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

		register(context, PlacedFeatureInit.PATCH_HYPHAE, SMALL_INFECTED_FUNGUS, RarityFilter.onAverageOnceEvery(7),
				InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

		context.register(PLACED_CANOPY_MUSHROOMS_SPARSE,
				new PlacedFeature(configuredFeatureGetter.getOrThrow(ConfiguredFeatureInit.CANOPY_MUSHROOMS_SPARSE),
						tfTreeCheckArea(PlacementUtils.countExtra(3, 0.1F, 1),
								BlockInit.infected_fungus.get().defaultBlockState())));

		context.register(PLACED_CANOPY_MUSHROOMS_DENSE,
				new PlacedFeature(configuredFeatureGetter.getOrThrow(ConfiguredFeatureInit.CANOPY_MUSHROOMS_DENSE),
						tfTreeCheckArea(PlacementUtils.countExtra(5, 0.1F, 1),
								BlockInit.infected_fungus.get().defaultBlockState())));

		// Conscious mass blob - surface decoration for fungal dimension
		context.register(PLACED_CONSCIOUS_MASS_BLOB,
				new PlacedFeature(configuredFeatureGetter.getOrThrow(ConfiguredFeatureInit.CONSCIOUS_MASS_BLOB),
						ImmutableList.<PlacementModifier>builder().add(RarityFilter.onAverageOnceEvery(4),
								InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())
								.build()));

		// Fungal dimension ores
		final Holder<ConfiguredFeature<?, ?>> ORE_HEMATIC_IRON = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.ORE_HEMATIC_IRON);
		register(context, PlacedFeatureInit.ORE_HEMATIC_IRON, ORE_HEMATIC_IRON, List.of(
				CountPlacement.of(10), InSquarePlacement.spread(),
				HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(96)),
				BiomeFilter.biome()));

		// Spore Nexus Tower - very rare mega-structure, once every ~150 chunks
		final Holder<ConfiguredFeature<?, ?>> SPORE_NEXUS_TOWER = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.SPORE_NEXUS_TOWER);
		register(context, PlacedFeatureInit.SPORE_NEXUS_TOWER, SPORE_NEXUS_TOWER, List.of(
				RarityFilter.onAverageOnceEvery(150), InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome()));

		// Sporite Crystal patches - occasional glowing clusters
		final Holder<ConfiguredFeature<?, ?>> SPORITE_CRYSTAL_CLUSTER = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.SPORITE_CRYSTAL_CLUSTER);
		register(context, PlacedFeatureInit.SPORITE_CRYSTAL_CLUSTER, SPORITE_CRYSTAL_CLUSTER,
				RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-2, 2), 0, 2)), BiomeFilter.biome());

	}

	public static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
			BootstapContext<ConfiguredFeature<?, ?>> pContext, ResourceKey<ConfiguredFeature<?, ?>> pKey, F pFeature,
			FC pConfig) {
		pContext.register(pKey, new ConfiguredFeature(pFeature, pConfig));
	}

	private static List<PlacementModifier> tfTreeCheckArea(PlacementModifier count, BlockState sapling) {
		return ImmutableList.of(count, InSquarePlacement.spread(), SurfaceWaterDepthFilter.forMaxDepth(0),
				PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome());
	}

	protected static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> placedFeatureKey,
			Holder<ConfiguredFeature<?, ?>> configuredFeature, PlacementModifier... modifiers) {
		register(context, placedFeatureKey, configuredFeature, List.of(modifiers));
	}

	protected static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> placedFeatureKey,
			Holder<ConfiguredFeature<?, ?>> configuredFeature, List<PlacementModifier> modifiers) {
		context.register(placedFeatureKey, new PlacedFeature(configuredFeature, modifiers));
	}

	public static ResourceKey<PlacedFeature> createKey(String name) {
		return ResourceKey.create(Registries.PLACED_FEATURE, Hemomancy.rloc(name));
	}

}
