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
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class PlacedFeatureInit {
	public static final ResourceKey<PlacedFeature> HYPHAE_TENDRIL = createKey("hyphae_tendril");
	public static final ResourceKey<PlacedFeature> HUGE_FUNGUS = createKey("huge_fungus");
	public static final ResourceKey<PlacedFeature> SMALL_INFECTED_FUNGUS = createKey("small_infected_fungus");
	public static final ResourceKey<PlacedFeature> PATCH_HYPHAE = createKey("patch_hyphae");
	public static final ResourceKey<PlacedFeature> PLACED_MYCELIUM_BLOB = createKey("mycelium_blob");
	public static final ResourceKey<PlacedFeature> PLACED_INFESTED_VENOUS_STONE_BLOB = createKey("infested_venous_stone_blob");

	public static final ResourceKey<PlacedFeature> PLACED_CANOPY_MUSHROOMS_SPARSE = createKey(
			"mushroom/canopy_mushrooms_sparse");
	public static final ResourceKey<PlacedFeature> PLACED_CANOPY_MUSHROOMS_DENSE = createKey(
			"mushroom/canopy_mushrooms_dense");

	public static void bootstrap(BootstapContext<PlacedFeature> context) {

		HolderGetter<ConfiguredFeature<?, ?>> configuredFeatureGetter = context.lookup(Registries.CONFIGURED_FEATURE);

		final Holder<ConfiguredFeature<?, ?>> HYPHAE_TENDRIL = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.HYPHAE_TENDRIL);

		final Holder<ConfiguredFeature<?, ?>> HUGE_FUNGUS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.HUGE_FUNGUS);

		final Holder<ConfiguredFeature<?, ?>> SMALL_INFECTED_FUNGUS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.SMALL_INFECTED_FUNGUS);

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
