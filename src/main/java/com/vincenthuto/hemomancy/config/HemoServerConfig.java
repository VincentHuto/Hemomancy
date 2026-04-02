package com.vincenthuto.hemomancy.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;

public class HemoServerConfig {

	public static ForgeConfigSpec.BooleanValue BLOOD_REGEN_ENABLED;
	public static ForgeConfigSpec.DoubleValue BLOOD_REGEN_RATE;
	public static ForgeConfigSpec.IntValue BLOOD_REGEN_INTERVAL;

	public static void registerServerConfig(Builder builder) {
		builder.comment("Blood Volume Settings").push("blood_volume");

		BLOOD_REGEN_ENABLED = builder
				.comment("Whether passive blood regeneration is enabled when the blood system is active.")
				.define("bloodRegenEnabled", true);

		BLOOD_REGEN_RATE = builder
				.comment("Amount of blood restored per regen tick.")
				.defineInRange("bloodRegenRate", 1.0, 0.1, 100.0);

		BLOOD_REGEN_INTERVAL = builder
				.comment("How many ticks between each passive blood regen tick. 20 ticks = 1 second.")
				.defineInRange("bloodRegenInterval", 20, 1, 1200);

		builder.pop();
	}

}
