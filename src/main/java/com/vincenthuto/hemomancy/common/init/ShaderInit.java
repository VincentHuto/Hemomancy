package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.RadiantPortalRendertype;
import com.vincenthuto.hemomancy.client.screen.codex.ExtendedShaderInstance;
import com.vincenthuto.hemomancy.client.screen.codex.ShaderHolder;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class ShaderInit {

	public static ShaderHolder DISTORTED_TEXTURE = new ShaderHolder(Hemomancy.rloc("screen/distorted_texture"),
			DefaultVertexFormat.POSITION_COLOR_TEX, "Speed", "TimeOffset", "Intensity", "XFrequency", "YFrequency",
			"UVCoordinates");

	@SubscribeEvent
	public static void register(RegisterShadersEvent event) throws IOException {
        ResourceProvider provider = event.getResourceProvider();

		event.registerShader(new ShaderInstance(event.getResourceProvider(), Hemomancy.rloc("water"),
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (instance) -> {
					RadiantPortalRendertype.WATER_SHADER = instance;
				});
		event.registerShader(new ShaderInstance(event.getResourceProvider(), Hemomancy.rloc("ray"),
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (instance) -> {
					RadiantPortalRendertype.RAY_SHADER = instance;
				});
        registerShader(event, DISTORTED_TEXTURE.createInstance(provider));

	}
	   public static void registerShader(RegisterShadersEvent event, ExtendedShaderInstance extendedShaderInstance) {
	        event.registerShader(extendedShaderInstance, s -> {
	        });
	    }

}
