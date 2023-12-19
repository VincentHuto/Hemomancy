package com.vincenthuto.hemomancy.common.init;

import java.io.IOException;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.tile.RadiantPortalRendertype;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class ShaderInit {

	@SubscribeEvent
	public static void register(RegisterShadersEvent event) throws IOException {
		event.registerShader(new ShaderInstance(event.getResourceProvider(),
				Hemomancy.rloc("water"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
				(instance) -> {
					RadiantPortalRendertype.WATER_SHADER = instance;
				});
		event.registerShader(new ShaderInstance(event.getResourceProvider(),Hemomancy.rloc("ray"),
				DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), (instance) -> {
					RadiantPortalRendertype.RAY_SHADER = instance;
				});
	}

}
