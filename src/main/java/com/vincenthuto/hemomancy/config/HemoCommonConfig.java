package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class HemoCommonConfig {
	public static final boolean DEFAULT_ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION = false;

	public static BooleanValue ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION;

	public static void registerCommonConfig(Builder commonBuilder) {
		commonBuilder.push("worldgen");
		ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION = commonBuilder
				.comment("Enables the optional Overworld Fungal Gardens TerraBlender region. Disabled by default so erythrocoral reef debugging stays ocean-only.")
				.define("enableOverworldFungalGardensRegion", DEFAULT_ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION);
		commonBuilder.pop();
	}

	public static boolean enableOverworldFungalGardensRegion() {
		return ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION != null && ENABLE_OVERWORLD_FUNGAL_GARDENS_REGION.get();
	}

}
