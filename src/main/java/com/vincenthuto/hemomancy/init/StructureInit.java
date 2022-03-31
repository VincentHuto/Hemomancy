package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.world.structure.BloodTempleStructure;

import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StructureInit {

	public static final DeferredRegister<StructureFeature<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.STRUCTURE_FEATURES, Hemomancy.MOD_ID);

	public static final RegistryObject<StructureFeature<JigsawConfiguration>> blood_temple = STRUCTURES
			.register("blood_temple", BloodTempleStructure::new);
	// public static final TagKey<StructureSet> MYSTERIOUS_DIMENSION_STRUCTURE_SET =
	// TagKey.create(Registry.STRUCTURE_SET_REGISTRY, RL_MYSTERIOUS_DIMENSION_SET);

}
