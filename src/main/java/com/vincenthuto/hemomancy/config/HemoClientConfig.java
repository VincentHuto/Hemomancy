package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class HemoClientConfig {

	public static ModConfigSpec.IntValue HUD_LOCATION;
	public static ModConfigSpec.BooleanValue USE_COOLDOWN_VIGNETTE;
	public static ModConfigSpec.BooleanValue RENDER_CROWN_POMES_AS_ITEMS;
	public static ModConfigSpec.BooleanValue RENDER_BLOOD_GOURD_LAYER;
	public static ModConfigSpec.BooleanValue RENDER_VASCULARIUM_CHARM_LAYER;
	public static ModConfigSpec.BooleanValue RENDER_MORPHLING_JAR_LAYER;
	public static ModConfigSpec.BooleanValue RENDER_EQUIPPED_MORPHLING_LAYER;
	public static ModConfigSpec.BooleanValue RENDER_EQUIPPED_MORPHLING_HAND_LAYER;
	public static ModConfigSpec.BooleanValue RENDER_MORPHLING_MUTATION_LAYER;
	public static ModConfigSpec.BooleanValue RENDER_FANE_BOUNDARY;

	public static void registerClientConfig(ModConfigSpec.Builder CLIENT_BUILDER) {
		CLIENT_BUILDER.comment("Client settings for the power generator").push("powergen");

        HUD_LOCATION = CLIENT_BUILDER
                .comment("Location of Blood Volume Hud(Top Left =0,Top Right =1,Bottom Left =2,Bottom Right =3")
                .defineInRange("location", 0, 0, 3);

	USE_COOLDOWN_VIGNETTE = CLIENT_BUILDER
		.comment("Render cooldowns as full-screen vignette overlays. If false, cooldowns render as compact numerical timers beside the relevant HUD gauge.")
		.define("useCooldownVignette", true);

	RENDER_CROWN_POMES_AS_ITEMS = CLIENT_BUILDER
		.comment("Render Qliphoth Communion crown sockets using the real Qliphoth Pome item icon instead of filled HUD pips.")
		.define("renderCrownPomesAsItems", false);

        CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment("Client-side toggles for Hemomancy player render layers").push("render_layers");

		RENDER_BLOOD_GOURD_LAYER = CLIENT_BUILDER
				.comment("Render blood gourds and curved horns equipped in the gourd slot.")
				.define("renderBloodGourdLayer", true);

		RENDER_VASCULARIUM_CHARM_LAYER = CLIENT_BUILDER
				.comment("Render the Charm of Vascularium equipped on the player model.")
				.define("renderVasculariumCharmLayer", true);

		RENDER_MORPHLING_JAR_LAYER = CLIENT_BUILDER
				.comment("Render morphling jars equipped on the player model.")
				.define("renderMorphlingJarLayer", true);

		RENDER_EQUIPPED_MORPHLING_LAYER = CLIENT_BUILDER
				.comment("Render the equipped morphling on the player arm in third person.")
				.define("renderEquippedMorphlingLayer", true);

		RENDER_EQUIPPED_MORPHLING_HAND_LAYER = CLIENT_BUILDER
				.comment("Render the equipped morphling on the player hand in first person.")
				.define("renderEquippedMorphlingHandLayer", true);

		RENDER_MORPHLING_MUTATION_LAYER = CLIENT_BUILDER
				.comment("Render morphling mutation overlays and attachments on the player model.")
				.define("renderMorphlingMutationLayer", true);

		CLIENT_BUILDER.pop();

		CLIENT_BUILDER.comment("Client-side toggles for Hemomancy world rendering effects").push("world_rendering");

		RENDER_FANE_BOUNDARY = CLIENT_BUILDER
				.comment("Render Founding Fane boundary domes and screen distortion.")
				.define("renderFaneBoundary", true);

		CLIENT_BUILDER.pop();
	}

}
