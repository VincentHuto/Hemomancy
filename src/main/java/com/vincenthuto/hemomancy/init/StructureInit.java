package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureInit {

	public static final DeferredRegister<Feature<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.FEATURES, Hemomancy.MOD_ID);
	/*
	 * public static final RegistryObject<Feature<JigsawConfiguration>> blood_temple
	 * = STRUCTURES .register("blood_temple", BloodTempleStructure::new);
	 */

}
