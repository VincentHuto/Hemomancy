package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.world.structure.BloodTempleStructure;

import net.minecraft.core.registries.Registries;
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
			.create(Registries.STRUCTURE_TYPE, Hemomancy.MOD_ID);

	public static DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister
			.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Hemomancy.MOD_ID);

	public static final RegistryObject<StructureType<BloodTempleStructure>> blood_temple = STRUCTURES
			.register("blood_temple", () -> explicitStructureTypeTyping(BloodTempleStructure.CODEC));

	private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(Codec<T> structureCodec) {
		return () -> structureCodec;
	}

}
