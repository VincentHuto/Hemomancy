package com.vincenthuto.hemomancy.config;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class HemoConfig {

	public static void register() {
		registerServerConfigs();
		registerCommonConfigs();
		registerClientConfigs();
	}

	private static void registerClientConfigs() {
		ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
		HemoClientConfig.registerClientConfig(CLIENT_BUILDER);
		ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.CLIENT, CLIENT_BUILDER.build());
	}

	private static void registerCommonConfigs() {
		ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
		HemoCommonConfig.registerCommonConfig(COMMON_BUILDER);
		ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.COMMON, COMMON_BUILDER.build());
	}

	private static void registerServerConfigs() {
		ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
		HemoServerConfig.registerServerConfig(SERVER_BUILDER);
		ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, SERVER_BUILDER.build());
	}
}