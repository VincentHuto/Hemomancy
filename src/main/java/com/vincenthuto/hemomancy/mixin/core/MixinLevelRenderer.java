package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.world.BloodMoonVeinSkyRenderer;
import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.LevelRenderer;

/**
 * Hooks into renderSky at the moon-draw point to:
 *   1. Draw blood-vein tendrils radiating from the moon (BEFORE getMoonPhase)
 *   2. Tint the moon texture blood-red (AFTER getMoonPhase)
 *
 * require = 0 on both so a future Forge patch to the injection target
 * degrades gracefully instead of crashing.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

	private static final float hemomancy$bloodMoonRed = 1.00F;
	private static final float hemomancy$bloodMoonGreen = 0.05F;
	private static final float hemomancy$bloodMoonBlue = 0.05F;

	@Inject(method = "renderSky", at = @At("TAIL"), require = 0, remap = false)
	private void hemomancy$renderBloodMoonVeins(Matrix4f frustumMatrix, Matrix4f projectionMatrix,
			float partialTick, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
		if (!BloodMoonClientState.isActive()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null) return;
		BloodMoonVeinSkyRenderer.renderInSky(new PoseStack(), level, mc.getTimer().getGameTimeDeltaPartialTick(false));
	}

	@Inject(method = "renderSky", at = @At("TAIL"), require = 0, remap = false)
	private void hemomancy$applyBloodMoonTint(Matrix4f frustumMatrix, Matrix4f projectionMatrix,
			float partialTick, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
		if (!BloodMoonClientState.isActive()) return;
		// TAIL injections happen after sky draw; ensure we do not leak tint/alpha into GUI render passes.
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
	}
}
