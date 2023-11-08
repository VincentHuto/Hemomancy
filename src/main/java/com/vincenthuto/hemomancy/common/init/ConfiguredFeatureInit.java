package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class ConfiguredFeatureInit {
	public static final ResourceKey<ConfiguredFeature<?, ?>> FLESH_TENDON = createKey("flesh_tendon");
	public static final ResourceKey<ConfiguredFeature<?, ?>> HUGE_FUNGUS = createKey("huge_fungus");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SMALL_INFECTED_FUNGUS = FeatureUtils.createKey("small_infected_fungus");

	public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Hemomancy.MOD_ID, name));
	}

	public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
		register(context, FLESH_TENDON, BaseFeatureInit.FLESH_TENDON, NoneFeatureConfiguration.INSTANCE);
		register(context, HUGE_FUNGUS, BaseFeatureInit.HUGE_FUNGUS, NoneFeatureConfiguration.INSTANCE);
		register(context, SMALL_INFECTED_FUNGUS, Feature.SIMPLE_RANDOM_SELECTOR,
						new SimpleRandomFeatureConfiguration(
								HolderSet.direct(
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
														new SimpleBlockConfiguration(BlockStateProvider
																.simple(BlockInit.smouldering_ash_trail.get())))))));

	}

	private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
			BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey,
			F feature, FC configuration) {
		context.register(configuredFeatureKey, new ConfiguredFeature<>(feature, configuration));
	}
}
