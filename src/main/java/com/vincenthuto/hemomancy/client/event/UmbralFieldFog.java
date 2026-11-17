package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.ManipulationVisualRenderer;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Inside a field of dark (Blood Eclipse's light taken, core-loop round two P8) a violet-black fog draws in: near 3 and
 * far 14 blocks at full depth, thinning across the field's soft edge, crossfaded by the player's darkness-effect
 * setting. Never black and never a blackout; where some other dark (a Saint's gloam, a rite's air) already holds the far
 * plane closer, it is left alone. The pattern of {@link VelorumGloamFog}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class UmbralFieldFog {
	public static final float NEAR = 3.0F, FAR = 14.0F;
	/** The fog's colour: Tenebris's violet-black lifted, so it reads as dark air, never as black. */
	private static final float RED = 0.075F, GREEN = 0.05F, BLUE = 0.115F;
	/** How fast the dark closes and lifts, in ticks. */
	private static final float EASE = 5.0F;

	private static float eased;
	private static float easedAt = Float.NaN;
	/** Whether the last terrain fog was this field's: the colour follows it, so another dark's colour is left alone too. */
	private static boolean drawn;

	private UmbralFieldFog() {
	}

	/** How deep the camera stands in a field of dark, 0 to 1, eased over a few ticks. */
	public static float strength(Entity camera, float partial) {
		if (camera == null) return 0.0F;
		float now = AnimationTime.gameTime(camera.level(), partial);
		float target = ManipulationVisualRenderer.umbralDepth(camera.getEyePosition(partial), now);
		if (Float.isNaN(easedAt) || now < easedAt || now - easedAt > 40.0F) {
			eased = target;
		} else {
			float dt = now - easedAt;
			eased += (target - eased) * (1.0F - (float) Math.exp(-dt / EASE));
		}
		easedAt = now;
		return HemoEasing.clamp01(eased);
	}

	@SubscribeEvent
	public static void fog(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
		Entity camera = event.getCamera().getEntity();
		if (camera == null) return;
		float depth = strength(camera, (float) event.getPartialTick());
		float weight = (float) (depth * Minecraft.getInstance().options.darknessEffectScale().get());
		drawn = weight > 0.004F && event.getFarPlaneDistance() > FAR;
		if (!drawn) return;
		event.setNearPlaneDistance(HemoEasing.crossfade(event.getNearPlaneDistance(), NEAR, weight));
		event.setFarPlaneDistance(HemoEasing.crossfade(event.getFarPlaneDistance(), FAR, weight));
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void colour(ViewportEvent.ComputeFogColor event) {
		if (!drawn || event.getCamera().getEntity() == null) return;
		float weight = HemoEasing.clamp01(eased) * Minecraft.getInstance().options.darknessEffectScale().get().floatValue();
		if (weight <= 0.004F) return;
		event.setRed(HemoEasing.crossfade(event.getRed(), RED, weight));
		event.setGreen(HemoEasing.crossfade(event.getGreen(), GREEN, weight));
		event.setBlue(HemoEasing.crossfade(event.getBlue(), BLUE, weight));
	}
}
