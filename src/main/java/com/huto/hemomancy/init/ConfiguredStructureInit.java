package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public class ConfiguredStructureInit {
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_RUN_DOWN_HOUSE = StructureInit.RUN_DOWN_HOUSE.get()
			.configured(FeatureConfiguration.NONE);
	public static ConfiguredStructureFeature<?, ?> CONFIGURED_BLOOD_TEMPLE = StructureInit.blood_temple.get()
			.configured(FeatureConfiguration.NONE);

	/**
	 * Registers the configured structure which is what gets added to the biomes.
	 * Noticed we are not using a forge registry because there is none for
	 * configured structures.
	 */
	public static void registerConfiguredStructures() {
		Registry<ConfiguredStructureFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE;

		Registry.register(registry, new ResourceLocation(Hemomancy.MOD_ID, "configured_run_down_house"),
				CONFIGURED_RUN_DOWN_HOUSE);
		FlatLevelGeneratorSettings.STRUCTURE_FEATURES.put(StructureInit.RUN_DOWN_HOUSE.get(),
				CONFIGURED_RUN_DOWN_HOUSE);

		Registry.register(registry, new ResourceLocation(Hemomancy.MOD_ID, "configured_blood_temple"),
				CONFIGURED_BLOOD_TEMPLE);
		FlatGenerationSettings.STRUCTURE_FEATURES.put(StructureInit.blood_temple.get(), CONFIGURED_BLOOD_TEMPLE);

	}
}
