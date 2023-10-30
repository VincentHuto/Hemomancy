package com.vincenthuto.hemomancy.common.worldgen;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacedFeatureInit {
	public static final ResourceKey<PlacedFeature> FLESH_TENDON = createKey("flesh_tendon");

	public static void bootstrap(BootstapContext<PlacedFeature> context) {
		HolderGetter<ConfiguredFeature<?, ?>> configuredFeatureGetter = context.lookup(Registries.CONFIGURED_FEATURE);

		final Holder<ConfiguredFeature<?, ?>> FLESH_TENDON = configuredFeatureGetter
				.getOrThrow(ConfiguredFeatureInit.FLESH_TENDON);
		
        register(context, PlacedFeatureInit.FLESH_TENDON, FLESH_TENDON, List.of(CountPlacement.of(50), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome()));

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
		return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Hemomancy.MOD_ID, name));
	}
}
