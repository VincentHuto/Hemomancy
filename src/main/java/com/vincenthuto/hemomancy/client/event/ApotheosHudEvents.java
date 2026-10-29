package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** An Apotheos does not hunger: the spore bar takes the food bar's place. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ApotheosHudEvents {
	private ApotheosHudEvents() {}

	@SubscribeEvent
	public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
		if (!event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) return;
		var player = Minecraft.getInstance().player;
		if (player != null && ApotheosBodyEvents.isApotheos(player)) event.setCanceled(true);
	}
}
