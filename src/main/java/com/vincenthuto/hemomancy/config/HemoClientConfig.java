package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HemoClientConfig {

	public static ModConfigSpec.IntValue HUD_LOCATION;

	public static void registerClientConfig(ModConfigSpec.Builder CLIENT_BUILDER) {
		CLIENT_BUILDER.comment("Client settings for the power generator").push("powergen");

        HUD_LOCATION = CLIENT_BUILDER
                .comment("Location of Blood Volume Hud(Top Left =0,Top Right =1,Bottom Left =2,Bottom Right =3")
                .defineInRange("location", 0, 0, 3);

        CLIENT_BUILDER.pop();
	}

}
