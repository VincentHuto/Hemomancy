package com.vincenthuto.hemomancy.init;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.world.structure.BloodTempleStructure;

import net.minecraft.core.Registry;
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
	public static final DeferredRegister<StructureType<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister
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

	/**
	 * Registers the base structure itself and sets what its path is. In this case,
	 * this base structure will have the resourcelocation of
	 * hemomancy:sky_structures.
	 */

	public static final RegistryObject<StructureType<BloodTempleStructure>> blood_temple = DEFERRED_REGISTRY_STRUCTURE
			.register("blood_temple", () -> () -> BloodTempleStructure.CODEC);

}
