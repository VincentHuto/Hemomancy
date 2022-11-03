package com.vincenthuto.hemomancy.init;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.world.structure.BloodTempleStructure;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StructureInit {
	/**
	 * We are using the Deferred Registry system to register our structure as this
	 * is the preferred way on Forge. This will handle registering the base
	 * structure for us at the correct time so we don't have to handle it ourselves.
	 */
	public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister
			.create(Registry.STRUCTURE_TYPE_REGISTRY, Hemomancy.MOD_ID);

	public static DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Hemomancy.MOD_ID);

//	public static RegistryObject<Codec<ExampleBiomeModifier>> EXAMPLE_CODEC = BIOME_MODIFIER_SERIALIZERS
//			.register("example", () -> RecordCodecBuilder.create(builder -> builder.group(
//					// declare fields
//					Biome.LIST_CODEC.fieldOf("biomes").forGetter(ExampleBiomeModifier::biomes),
//					PlacedFeature.CODEC.fieldOf("feature").forGetter(ExampleBiomeModifier::feature)
//			// declare constructor
//			).apply(builder, ExampleBiomeModifier::new)));
	
	public static final RegistryObject<StructureType<BloodTempleStructure>> blood_temple = STRUCTURES
			.register("blood_temple", () -> explicitStructureTypeTyping(BloodTempleStructure.CODEC));

	private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
		return () -> structureCodec;
	}

//	private static void registerFeature(RegistryEvent.Register<Feature<?>> event) {
//		WorldGenEvents.BLEEDING_HEART_FEATURE = WorldGenEvents.flower("bleeding_heart", 64, BlockInit.bleeding_heart);
//		WorldGenEvents.BLEEDING_HEART_PLACEMENT = WorldGenEvents.flowerPlacement("aum",
//				WorldGenEvents.BLEEDING_HEART_FEATURE, 32);
//		// Ores
//		WorldGenEvents.VENOUS_FEATURE = WorldGenEvents.netherOre("venous_ore", BlockInit.venous_stone,
//				BlockInit.gilded_venous_stone, 27, 0.25F);
//
//		WorldGenEvents.VENOUS_PLACEMENT = WorldGenEvents.orePlacement("venous_ore", WorldGenEvents.VENOUS_FEATURE, 56,
//				HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(36)));
//
//		// Trees
//		WorldGenEvents.WITCHWOOD_TREE_FEATURE = WorldGenEvents.tree("infected_fungus", BlockInit.infected_stem,
//				new DarkOakTrunkPlacer(9, 3, 1), BlockInit.infected_cap,
//				new DarkOakFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1)),
//				new ThreeLayersFeatureSize(1, 2, 1, 1, 2, OptionalInt.empty()));
//		WorldGenEvents.WITCHWOOD_TREE_PLACEMENT = WorldGenEvents.treePlacement("infected_fungus",
//				WorldGenEvents.WITCHWOOD_TREE_FEATURE, BlockInit.infected_fungus);
//		WorldGenEvents.WITCHWOOD_TREE_VEGETATION = WorldGenEvents.treeVegetation("infected_fungus",
//				WorldGenEvents.WITCHWOOD_TREE_FEATURE, PlacementUtils.countExtra(1, 0.1F, 0), 8,
//				BlockInit.infected_fungus);
//	}
}
