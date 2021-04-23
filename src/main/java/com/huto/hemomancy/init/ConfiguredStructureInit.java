package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

public class ConfiguredStructureInit {
	public static StructureFeature<?, ?> CONFIGURED_RUN_DOWN_HOUSE = StructureInit.RUN_DOWN_HOUSE.get()
			.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);
	public static StructureFeature<?, ?> CONFIGURED_BLOOD_TEMPLE = StructureInit.blood_temple.get()
			.withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG);

	/**
	 * Registers the configured structure which is what gets added to the biomes.
	 * Noticed we are not using a forge registry because there is none for
	 * configured structures.
	 */
	public static void registerConfiguredStructures() {
		Registry<StructureFeature<?, ?>> registry = WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE;

		Registry.register(registry, new ResourceLocation(Hemomancy.MOD_ID, "configured_run_down_house"),
				CONFIGURED_RUN_DOWN_HOUSE);
		FlatGenerationSettings.STRUCTURES.put(StructureInit.RUN_DOWN_HOUSE.get(), CONFIGURED_RUN_DOWN_HOUSE);

		Registry.register(registry, new ResourceLocation(Hemomancy.MOD_ID, "configured_blood_temple"),
				CONFIGURED_BLOOD_TEMPLE);
		FlatGenerationSettings.STRUCTURES.put(StructureInit.blood_temple.get(), CONFIGURED_BLOOD_TEMPLE);

	}
}
