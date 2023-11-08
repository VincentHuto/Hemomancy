package com.vincenthuto.hemomancy.common.init;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class PlacedFeatureInit {
	public static final ResourceKey<PlacedFeature> FLESH_TENDON = createKey("flesh_tendon");
	public static final ResourceKey<PlacedFeature> HUGE_FUNGUS = createKey("huge_fungus");
	public static final ResourceKey<PlacedFeature> SMALL_INFECTED_FUNGUS = createKey("small_infected_fungus");

	public static void bootstrap(BootstapContext<PlacedFeature> context) {
		HolderGetter<ConfiguredFeature<?, ?>> configuredFeatureGetter = context.lookup(Registries.CONFIGURED_FEATURE);

		final Holder<ConfiguredFeature<?, ?>> FLESH_TENDON = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.FLESH_TENDON);

		final Holder<ConfiguredFeature<?, ?>> HUGE_FUNGUS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.HUGE_FUNGUS);

		final Holder<ConfiguredFeature<?, ?>> SMALL_INFECTED_FUNGUS = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.SMALL_INFECTED_FUNGUS);

		register(context, PlacedFeatureInit.FLESH_TENDON, FLESH_TENDON, List.of(CountPlacement.of(50),
				InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()));
		register(context, PlacedFeatureInit.HUGE_FUNGUS, HUGE_FUNGUS, List.of(CountPlacement.of(50),
				InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()));

		register(context, PlacedFeatureInit.SMALL_INFECTED_FUNGUS, SMALL_INFECTED_FUNGUS,
				RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)), BiomeFilter.biome());

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
