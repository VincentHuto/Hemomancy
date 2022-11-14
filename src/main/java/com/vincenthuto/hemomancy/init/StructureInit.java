package com.vincenthuto.hemomancy.init;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.world.structure.BloodTempleStructure;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
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

	public static final RegistryObject<StructureType<BloodTempleStructure>> blood_temple = STRUCTURES
			.register("blood_temple", () -> explicitStructureTypeTyping(BloodTempleStructure.CODEC));

	private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
		return () -> structureCodec;
	}

//	public static TagKey<Structure> BLOOD_TEMPLE = create("blood_temple_located");

	private static TagKey<Structure> create(String pName) {
		return TagKey.create(Registry.STRUCTURE_REGISTRY, new ResourceLocation(Hemomancy.MOD_ID, pName));
	}
}
