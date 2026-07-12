package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.FungalWhisperVignetteOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class FungalProjectionClientEvents {
	private FungalProjectionClientEvents() {}

	@SubscribeEvent
	public static void onRenderHand(RenderHandEvent event) {
		if (FungalWhisperVignetteOverlay.isProjectionActive()) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
		if (FungalWhisperVignetteOverlay.isProjectionActive()
				&& event.getEntity() == net.minecraft.client.Minecraft.getInstance().player) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onScreenOpening(ScreenEvent.Opening event) {
		if (FungalWhisperVignetteOverlay.isProjectionActive()
				&& event.getNewScreen() instanceof InventoryScreen) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
		if (FungalWhisperVignetteOverlay.isProjectionActive()
				&& event.getName().equals(VanillaGuiLayers.HOTBAR)) {
			event.setCanceled(true);
		}
	}
}
