package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class HemoCommonConfig {
	public static final boolean DEFAULT_ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION = true;

	public static BooleanValue ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION;
	public static BooleanValue ENABLE_PHLEGETHONTIC_NETHER_REGION;
	public static net.neoforged.neoforge.common.ModConfigSpec.IntValue PHLEGETHONTIC_NETHER_REGION_WEIGHT;
	public static BooleanValue ENABLE_CORTICAL_DRIFT_END_REGION;
	public static net.neoforged.neoforge.common.ModConfigSpec.IntValue CORTICAL_DRIFT_END_REGION_WEIGHT;

	public static void registerCommonConfig(Builder commonBuilder) {
		commonBuilder.push("worldgen");
		ENABLE_PHLEGETHONTIC_NETHER_REGION = commonBuilder.comment("Generate the Phlegethontic Basin in new Nether chunks. Requires restart.")
				.worldRestart().define("enablePhlegethonticNetherRegion",true);
		PHLEGETHONTIC_NETHER_REGION_WEIGHT = commonBuilder.comment("TerraBlender Nether region weight. Requires restart.")
				.worldRestart().defineInRange("phlegethonticNetherRegionWeight",1,1,100);
		ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION = commonBuilder
				.comment("Enables the optional Overworld Fungal Gardens TerraBlender region. Enabled by default.")
				.define("enableOverworldFungalGardensRegion", DEFAULT_ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION);
		ENABLE_CORTICAL_DRIFT_END_REGION = commonBuilder
				.comment("Generate the Cortical Drift biome in new End chunks. Requires restart.")
				.worldRestart().define("enableCorticalDriftEndRegion", true);
		CORTICAL_DRIFT_END_REGION_WEIGHT = commonBuilder
				.comment("TerraBlender End biome weight for Cortical Drift. Requires restart.")
				.worldRestart().defineInRange("corticalDriftEndRegionWeight", 2, 1, 100);
		commonBuilder.pop();
	}

	public static boolean enableOverworldFungalGardensRegion() {
		return ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION != null && ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION.get();
	}

}
