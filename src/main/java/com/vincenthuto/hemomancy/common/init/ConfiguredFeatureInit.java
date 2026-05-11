package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;

import java.util.List;

public class ConfiguredFeatureInit {
	public static final ResourceKey<ConfiguredFeature<?, ?>> HYPHAE_TENDRIL = createKey("hyphae_tendril");
	public static final ResourceKey<ConfiguredFeature<?, ?>> VENOUS_RIDGE = createKey("venous_ridge");
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
	public static final ResourceKey<ConfiguredFeature<?, ?>> MYCELIUM_BLOB = createKey("mycelium_blob");
	public static final ResourceKey<ConfiguredFeature<?, ?>> INFESTED_VENOUS_STONE_BLOB = createKey(
			"infested_venous_stone_blob");
	
	//Plants
	public static final ResourceKey<ConfiguredFeature<?, ?>> BLEEDING_HEARTS = FeatureUtils
			.createKey("bleeding_hearts");
	
	public static final ResourceKey<ConfiguredFeature<?, ?>> STINK_HORNS = FeatureUtils
			.createKey("stink_horns");

	public static final ResourceKey<ConfiguredFeature<?, ?>> LETHEAN_POPPIES = createKey("lethean_poppies");

	public static final ResourceKey<ConfiguredFeature<?, ?>> GHOST_PIPES = createKey("ghost_pipes");

	public static final ResourceKey<ConfiguredFeature<?, ?>> SARCODES = createKey("sarcodes");

	public static final ResourceKey<ConfiguredFeature<?, ?>> RAFFLESIA = createKey("rafflesia");

	public static final ResourceKey<ConfiguredFeature<?, ?>> BOG_BODY = createKey("bog_body");

	public static final ResourceKey<ConfiguredFeature<?, ?>> EARTHEN_VEIN = createKey("earthen_vein");

	public static final ResourceKey<ConfiguredFeature<?, ?>> TERMITE_MOUND = createKey("termite_mound");

	public static final ResourceKey<ConfiguredFeature<?, ?>> DEEP_OCEAN_VENT = createKey("deep_ocean_vent");

	public static final ResourceKey<ConfiguredFeature<?, ?>> ERYTHROCORAL_REEF = createKey("erythrocoral_reef");

	public static final ResourceKey<ConfiguredFeature<?, ?>> TOOTH_GEODE = createKey("tooth_geode");

	// Spore Nexus Tower - massive rare fungal spire for the fungal dimension
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPORE_NEXUS_TOWER = createKey("spore_nexus_tower");
	public static final ResourceKey<ConfiguredFeature<?, ?>> SILVER_BELLS_TOWER = createKey("silver_bells_tower");

	// Sporite Crystal patch feature
	public static final ResourceKey<ConfiguredFeature<?, ?>> SPORITE_CRYSTAL_CLUSTER = createKey("sporite_crystal_cluster");

	// Conscious mass blob (for fungal dimension surface decoration)
	public static final ResourceKey<ConfiguredFeature<?, ?>> CONSCIOUS_MASS_BLOB = createKey("conscious_mass_blob");

	// Fungal dimension ores
	public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_HEMATIC_IRON = createKey("ore_hematic_iron");

	public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, Hemomancy.rloc(name));
	}

	public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
		HolderGetter<ConfiguredFeature<?, ?>> features = context.lookup(Registries.CONFIGURED_FEATURE);

		context.register(MYCELIUM_BLOB,
				new ConfiguredFeature<>(BaseFeatureInit.MYCELIUM_BLOB,
						new DiskConfiguration(RuleBasedBlockStateProvider.simple(Blocks.MYCELIUM),
								BlockPredicate.matchesBlocks(BlockInit.erythrocytic_mycelium.get()),
								UniformInt.of(4, 6), 3)));

		context.register(INFESTED_VENOUS_STONE_BLOB,
				new ConfiguredFeature<>(BaseFeatureInit.INFESTED_VENOUS_STONE_BLOB,
						new DiskConfiguration(RuleBasedBlockStateProvider.simple(BlockInit.infested_venous_stone.get()),
								BlockPredicate.matchesBlocks(BlockInit.erythrocytic_mycelium.get()),
								UniformInt.of(4, 6), 3)));

		context.register(CONSCIOUS_MASS_BLOB,
				new ConfiguredFeature<>(BaseFeatureInit.MYCELIUM_BLOB,
						new DiskConfiguration(RuleBasedBlockStateProvider.simple(BlockInit.conscious_mass.get()),
								BlockPredicate.matchesBlocks(BlockInit.erythrocytic_mycelium.get()),
								UniformInt.of(3, 5), 3)));

		// Fungal dimension ores - veins embedded in venous stone
		BlockMatchTest venousStoneTest = new BlockMatchTest(BlockInit.venous_stone.get());

		context.register(ORE_HEMATIC_IRON,
				new ConfiguredFeature<>(Feature.ORE,
						new OreConfiguration(venousStoneTest,
								BlockInit.hematic_iron_ore.get().defaultBlockState(), 9)));

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

		register(context, HYPHAE_TENDRIL, BaseFeatureInit.HYPHAE_TENDRIL, NoneFeatureConfiguration.INSTANCE);
		register(context, VENOUS_RIDGE, BaseFeatureInit.VENOUS_RIDGE, NoneFeatureConfiguration.INSTANCE);
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

		register(context, BLEEDING_HEARTS, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
						FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
								BlockStateProvider.simple(BlockInit.bleeding_heart.get())))))));

		register(context,STINK_HORNS, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
						FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
								BlockStateProvider.simple(BlockInit.stinkhorn_fungus.get())))))));

		register(context, LETHEAN_POPPIES, Feature.FLOWER,
				grassPatch(BlockStateProvider.simple(BlockInit.lethean_poppy.get()), 6));

		register(context, GHOST_PIPES, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
						FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
								BlockStateProvider.simple(BlockInit.ghost_pipe.get())))))));

		register(context, SARCODES, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
						FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
								BlockStateProvider.simple(BlockInit.sarcodes.get())))))));

		register(context, RAFFLESIA, Feature.FLOWER,
				grassPatch(BlockStateProvider.simple(BlockInit.rafflesia.get()), 1));

		register(context, BOG_BODY, BaseFeatureInit.BOG_BODY, NoneFeatureConfiguration.INSTANCE);

		register(context, EARTHEN_VEIN, BaseFeatureInit.EARTHEN_VEIN, NoneFeatureConfiguration.INSTANCE);

		register(context, TERMITE_MOUND, BaseFeatureInit.TERMITE_MOUND, NoneFeatureConfiguration.INSTANCE);

		register(context, DEEP_OCEAN_VENT, BaseFeatureInit.DEEP_OCEAN_VENT, NoneFeatureConfiguration.INSTANCE);

		register(context, ERYTHROCORAL_REEF, BaseFeatureInit.ERYTHROCORAL_REEF, NoneFeatureConfiguration.INSTANCE);

		register(context, TOOTH_GEODE, BaseFeatureInit.TOOTH_GEODE, NoneFeatureConfiguration.INSTANCE);

		register(context, SPORE_NEXUS_TOWER, BaseFeatureInit.SPORE_NEXUS_TOWER, NoneFeatureConfiguration.INSTANCE);
		register(context, SILVER_BELLS_TOWER, BaseFeatureInit.SILVER_BELLS_TOWER, NoneFeatureConfiguration.INSTANCE);

		register(context, SPORITE_CRYSTAL_CLUSTER, Feature.SIMPLE_RANDOM_SELECTOR,
				new SimpleRandomFeatureConfiguration(HolderSet.direct(PlacementUtils.inlinePlaced(Feature.RANDOM_PATCH,
						FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(
								BlockStateProvider.simple(BlockInit.sporite_crystal.get())))))));

	}

	private static RandomPatchConfiguration grassPatch(BlockStateProvider p_195203_, int pTries) {
		return FeatureUtils.simpleRandomPatchConfiguration(pTries,
				PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(p_195203_)));
	}

	private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
			BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey,
			F feature, FC configuration) {
		context.register(configuredFeatureKey, new ConfiguredFeature<>(feature, configuration));
	}
}
