package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules;
import com.vincenthuto.hemomancy.client.screen.overlay.CircusPerceptionOverlay;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * The big top's house going dark as the phantom show gathers: the fog draws in toward a near-black crimson, never
 * nearer than 32 blocks so the ring and anything in it stays readable, crossfaded by the player's darkness-effect
 * setting. Only inside a pavilion with its ring in view, past Unaware. The pattern of {@link UmbralFieldFog}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class CircusShowFog {
	/** The house colour: crimson-black, lifted enough to read as air. */
	private static final float RED = 0.04F, GREEN = 0.012F, BLUE = 0.024F;

	private CircusShowFog() {
	}

	@SubscribeEvent
	public static void fog(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN || !CircusPerceptionOverlay.showing()) return;
		float house = CircusPerceptionOverlay.look().house();
		double scale = Minecraft.getInstance().options.darknessEffectScale().get();
		float far = PhantomShowRules.fogFar(event.getFarPlaneDistance(), house, scale);
		if (far >= event.getFarPlaneDistance()) return;
		event.setFarPlaneDistance(far);
		event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), far * 0.25F));
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void colour(ViewportEvent.ComputeFogColor event) {
		if (!CircusPerceptionOverlay.showing()) return;
		float weight = HemoEasing.clamp01(CircusPerceptionOverlay.look().house()
				* Minecraft.getInstance().options.darknessEffectScale().get().floatValue());
		if (weight <= 0.004F) return;
		event.setRed(HemoEasing.crossfade(event.getRed(), RED, weight));
		event.setGreen(HemoEasing.crossfade(event.getGreen(), GREEN, weight));
		event.setBlue(HemoEasing.crossfade(event.getBlue(), BLUE, weight));
	}
}
