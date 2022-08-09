package com.vincenthuto.hemomancy.entity;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.EntityInit;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.FORGE)
public class WorldGenEvents {

	public static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> BLEEDING_HEART_FEATURE;
	public static Holder<PlacedFeature> BLEEDING_HEART_PLACEMENT;
    public static Holder<PlacedFeature> VENOUS_PLACEMENT;
    public static Holder<ConfiguredFeature<OreConfiguration, ?>> VENOUS_FEATURE;

	private static <T extends FeatureConfiguration> Holder<ConfiguredFeature<T, ?>> feature(String name,
			Feature<T> feature, T configuration) {
		return FeatureUtils.register(Hemomancy.MOD_ID + ":" + name, feature, configuration);
	}

	private static Holder<PlacedFeature> placement(String name, Holder<? extends ConfiguredFeature<?, ?>> feature,
			PlacementModifier... modifiers) {
		return PlacementUtils.register(Hemomancy.MOD_ID + ":" + name, feature, modifiers);
	}

	/**
	 * @param name   The name of the feature.
	 * @param tries  How many tries for this feature will be performed.
	 * @param flower The flower block to use.
	 * @return A configured flower feature based on the given parameters.
	 */
	public static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> flower(String name, int tries,
			Supplier<? extends Block> flower) {
		return feature(name, Feature.RANDOM_PATCH,
				FeatureUtils.simpleRandomPatchConfiguration(tries, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
						new SimpleBlockConfiguration(BlockStateProvider.simple(flower.get())))));
	}

	/**
	 * @param name    The name of the feature.
	 * @param feature The configured feature to use.
	 * @param rarity  The rarity of this feature.
	 * @return A placed feature based on the given parameters.
	 */
	public static Holder<PlacedFeature> flowerPlacement(String name, Holder<? extends ConfiguredFeature<?, ?>> feature,
			int rarity) {
		return placement(name, feature, RarityFilter.onAverageOnceEvery(rarity), InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
	}
	  /**
     * @param name                     The name of the feature.
     * @param ore                      The ore block to use.
     * @param deepslateOre             The deepslate ore block to use.
     * @param veinSize                 The vein size of this ore.
     * @param airExposureDiscardChance A 0..1 float that represents the probability of the feature being discarded if the vein touches air.
     * @return A configured ore feature based on the given parameters.
     */
    public static Holder<ConfiguredFeature<OreConfiguration, ?>> netherOre(String name, Supplier<? extends Block> ore, Supplier<? extends Block> deepslateOre, int veinSize, float airExposureDiscardChance) {
        return feature(name, Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(OreFeatures.NETHER_ORE_REPLACEABLES, ore.get().defaultBlockState()), OreConfiguration.target(OreFeatures.NETHER_ORE_REPLACEABLES, deepslateOre.get().defaultBlockState())), veinSize, airExposureDiscardChance));
    }

    /**
     * @param name                 The name of the feature.
     * @param feature              The configured feature to use.
     * @param veinCount            The amount of veins for this feature.
     * @param heightRangePlacement The height distribution of this feature.
     * @return A placed feature based on the given parameters.
     */
    public static Holder<PlacedFeature> orePlacement(String name, Holder<? extends ConfiguredFeature<?, ?>> feature, int veinCount, HeightRangePlacement heightRangePlacement) {
        return placement(name, feature, CountPlacement.of(veinCount), InSquarePlacement.spread(), heightRangePlacement, BiomeFilter.biome());
    }
	@SubscribeEvent
	public static void biomeLoading(BiomeLoadingEvent event) {
		BiomeGenerationSettingsBuilder builder = event.getGeneration();
		MobSpawnSettingsBuilder spawn = event.getSpawns();
		ResourceLocation biome = event.getName();
		Biome.BiomeCategory category = event.getCategory();
        if (category == Biome.BiomeCategory.NETHER ) {
            builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, VENOUS_PLACEMENT);
        }

		
        if (category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND) {

		if (category == Biome.BiomeCategory.FOREST) {
			builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, BLEEDING_HEART_PLACEMENT);
		}

		if (category == Biome.BiomeCategory.UNDERGROUND) {
			spawn.addSpawn(MobCategory.AMBIENT,
					new MobSpawnSettings.SpawnerData(EntityInit.chitinite.get(), 2, 15, 25));
		}
        }

	}



}
