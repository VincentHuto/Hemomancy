package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.VelorumDarkSight;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Saint Velorum's gloam (revision 2026-11-06, design R.4): his sensory dark is his own, never the school's obscuring state. While the
 * viewer stands in his liquid dark (a pool outside his lantern's light, or anywhere in the Eclipse's hold, never within
 * an awake ally's warmth or while Searing), or carries the 60-tick gloam a Long Night, a Gloam Cut or the Shadow Step's
 * arrival put on them, a violet-black fog draws in: near 6 and far 18 blocks (4 and 14 in the Eclipse), crossfaded by
 * the player's darkness-effect setting. Never black, never a blackout: the far plane stays past the floor's width. The
 * pattern of {@link SchoolDarknessPresentation}. The screen vignette and the muffle are his meter's
 * ({@code VelorumMeterStyle}), from {@link #strength}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class VelorumGloamFog {
	public static final float NEAR = 6.0F, FAR = 18.0F, ECLIPSE_NEAR = 4.0F, ECLIPSE_FAR = 14.0F;
	/** The fog's colour: the liquid dark's violet-black (0x07040C) lifted a little, so it is never black. */
	private static final float RED = 0.045F, GREEN = 0.030F, BLUE = 0.070F;
	/** How fast the gloam closes and lifts, in ticks: it draws in, it does not snap. */
	private static final float EASE = 5.0F;

	private static float eased;
	private static float easedAt = Float.NaN;

	private VelorumGloamFog() {
	}

	/** How far the viewer is in his dark or gloamed, 0 to 1, eased over a few ticks. */
	public static float strength(Player viewer, float partial) {
		if (viewer == null) return 0.0F;
		float target = Math.max(VelorumDarkSight.dark(viewer, partial), VelorumDarkSight.gloam(viewer, partial));
		float now = AnimationTime.gameTime(viewer.level(), partial);
		if (Float.isNaN(easedAt) || now < easedAt || now - easedAt > 40.0F) {
			eased = target;
		} else {
			float dt = now - easedAt;
			eased += (target - eased) * (1.0F - (float) Math.exp(-dt / EASE));
		}
		easedAt = now;
		return HemoEasing.clamp01(eased);
	}

	/** The last eased strength, without stepping it (the sound muffle asks between frames). */
	public static float last() {
		return HemoEasing.clamp01(eased);
	}

	@SubscribeEvent
	public static void fog(ViewportEvent.RenderFog event) {
		if (event.getMode() != FogRenderer.FogMode.FOG_TERRAIN) return;
		if (!(event.getCamera().getEntity() instanceof Player viewer)) return;
		float partial = (float) event.getPartialTick();
		float gloam = strength(viewer, partial);
		double scale = Minecraft.getInstance().options.darknessEffectScale().get();
		float weight = (float) (gloam * scale);
		if (weight <= 0.004F) return;
		boolean eclipse = VelorumDarkSight.eclipsed(viewer, partial);
		float near = eclipse ? ECLIPSE_NEAR : NEAR, far = eclipse ? ECLIPSE_FAR : FAR;
		if (event.getFarPlaneDistance() <= far) return;
		event.setNearPlaneDistance(HemoEasing.crossfade(event.getNearPlaneDistance(), near, weight));
		event.setFarPlaneDistance(HemoEasing.crossfade(event.getFarPlaneDistance(), far, weight));
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void colour(ViewportEvent.ComputeFogColor event) {
		if (!(event.getCamera().getEntity() instanceof Player)) return;
		float weight = last() * Minecraft.getInstance().options.darknessEffectScale().get().floatValue();
		if (weight <= 0.004F) return;
		event.setRed(HemoEasing.crossfade(event.getRed(), RED, weight));
		event.setGreen(HemoEasing.crossfade(event.getGreen(), GREEN, weight));
		event.setBlue(HemoEasing.crossfade(event.getBlue(), BLUE, weight));
	}
}
