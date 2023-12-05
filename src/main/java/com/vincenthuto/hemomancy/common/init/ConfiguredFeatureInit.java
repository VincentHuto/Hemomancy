package com.vincenthuto.hemomancy.common.init;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ConfiguredFeatureInit {
	public static final ResourceKey<ConfiguredFeature<?, ?>> FLESH_TENDON = createKey("flesh_tendon");
	public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_FUNGUS = createKey("huge_fungus");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_INFECTED_FUNGUS = FeatureUtils
			.createKey("small_infected_fungus");

	public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_HYPHAE = createKey("patch_hyphae");

	public static final ResourceKey<ConfiguredFeature<?, ?>> BROWN_CANOPY_MUSHROOM_TREE = createKey(
			"mushroom/brown_canopy_mushroom");
	public static final ResourceKey<ConfiguredFeature<?, ?>> RED_CANOPY_MUSHROOM_TREE = createKey(
			"mushroom/red_canopy_mushroom");

	// super funky tree placement lists
	public static final ResourceKey<ConfiguredFeature<?, ?>> DUMMY_TREE = createKey("tree/dummy");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CANOPY_MUSHROOMS_SPARSE = createKey(
			"mushroom/canopy_mushrooms_sparse");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CANOPY_MUSHROOMS_DENSE = createKey(
			"mushroom/canopy_mushrooms_dense");

	public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Hemomancy.MOD_ID, name));
	}

	public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
		HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

		register(context, PATCH_HYPHAE, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.stinkhorn_fungus.get())))),
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.infected_fungus.get())))),
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.infected_stem.get())))),
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.infected_stem.get())))))));

		context.register(BROWN_CANOPY_MUSHROOM_TREE,
				new ConfiguredFeature<>(BaseFeatureInit.CANOPY_BROWN_MUSHROOM,
						new HugeMushroomFeatureConfiguration(
								BlockStateProvider.simple(BlockInit.infected_cap.get().defaultBlockState()
										.setValue(HugeMushroomBlock.UP, Boolean.TRUE)
										.setValue(HugeMushroomBlock.DOWN, Boolean.FALSE)),
								BlockStateProvider.simple(BlockInit.infected_stem.get().defaultBlockState()
										.setValue(HugeMushroomBlock.UP, Boolean.FALSE)
										.setValue(HugeMushroomBlock.DOWN, Boolean.FALSE)),
								3)));

		context.register(RED_CANOPY_MUSHROOM_TREE,
				new ConfiguredFeature<>(BaseFeatureInit.CANOPY_RED_MUSHROOM,
						new HugeMushroomFeatureConfiguration(
								BlockStateProvider.simple(BlockInit.infected_cap.get().defaultBlockState()
										.setValue(HugeMushroomBlock.UP, Boolean.TRUE)
										.setValue(HugeMushroomBlock.DOWN, Boolean.FALSE)),
								BlockStateProvider.simple(BlockInit.infected_stem.get().defaultBlockState()
										.setValue(HugeMushroomBlock.UP, Boolean.FALSE)
										.setValue(HugeMushroomBlock.DOWN, Boolean.FALSE)),
								3)));

		context.register(DUMMY_TREE, new ConfiguredFeature<>(Feature.NO_OP, NoneFeatureConfiguration.INSTANCE));

		context.register(CANOPY_MUSHROOMS_SPARSE,
				new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
						new WeightedPlacedFeature(
								PlacementUtils.inlinePlaced(features.getOrThrow(BROWN_CANOPY_MUSHROOM_TREE)), 0.15f),
						new WeightedPlacedFeature(
								PlacementUtils.inlinePlaced(features.getOrThrow(RED_CANOPY_MUSHROOM_TREE)), 0.05f)),
						PlacementUtils.inlinePlaced(features.getOrThrow(DUMMY_TREE)))));

		context.register(CANOPY_MUSHROOMS_DENSE,
				new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(
						new WeightedPlacedFeature(
								PlacementUtils.inlinePlaced(features.getOrThrow(BROWN_CANOPY_MUSHROOM_TREE)), 0.675f),
						new WeightedPlacedFeature(
								PlacementUtils.inlinePlaced(features.getOrThrow(RED_CANOPY_MUSHROOM_TREE)), 0.225f)),
						PlacementUtils.inlinePlaced(features.getOrThrow(DUMMY_TREE)))));

		register(context, FLESH_TENDON, BaseFeatureInit.FLESH_TENDON, NoneFeatureConfiguration.INSTANCE);
		register(context, HUGE_FUNGUS, BaseFeatureInit.HUGE_FUNGUS, NoneFeatureConfiguration.INSTANCE);

		register(context, SMALL_INFECTED_FUNGUS, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.infected_fungus.get())))),
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.bleeding_heart.get())))),
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.befouling_ash_trail.get())))),
						PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
								FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK,
										new SimpleBlockConfiguration(
												BlockStateProvider.simple(BlockInit.smouldering_ash_trail.get())))))));

	}

	private static RandomPatchConfiguration grassPatch(BlockStateProvider p_195203_, int pTries) {
		return FeatureUtils.simpleRandomPatchConfiguration(pTries,
				PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(p_195203_)));
	}

	private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
			BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey,
			F feature, FC configuration) {
		context.register(configuredFeatureKey, new ConfiguredFeature<>(feature, configuration));
	}
}
