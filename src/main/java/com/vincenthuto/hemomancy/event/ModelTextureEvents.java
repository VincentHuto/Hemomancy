package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelTextureEvents {

	@SubscribeEvent
	public static void onStitch(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
			event.addSprite(new ResourceLocation(Hemomancy.MOD_ID, "entity/royal_guard_shield_base"));
			event.addSprite(new ResourceLocation(Hemomancy.MOD_ID, "entity/spiked_shield/model_spiked_shield"));

		}
	}

}