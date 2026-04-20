package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.LevelRenderer;

/**
 * Tints the vanilla moon texture blood-red during an active blood moon by
 * setting the shader colour right before the moon quad is uploaded.
 *
 * require = 0 so a future Forge patch that renames the injection target
 * leaves the moon white rather than crashing.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

	private static final float BLOOD_MOON_RED   = 1.00F;
	private static final float BLOOD_MOON_GREEN = 0.05F;
	private static final float BLOOD_MOON_BLUE  = 0.05F;

	@Inject(
		method = "renderSky",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/client/multiplayer/ClientLevel.getMoonPhase()I",
			shift = At.Shift.AFTER
		),
		require = 0
	)
	private void hemomancy$applyBloodMoonTint(PoseStack poseStack, Matrix4f projectionMatrix,
			float partialTick, Camera camera, boolean isFoggy, Runnable setupFog, CallbackInfo ci) {
		if (!BloodMoonClientState.isActive()) return;
		Minecraft mc = Minecraft.getInstance();
		float rainLevel = (mc.level != null) ? mc.level.getRainLevel(mc.getFrameTime()) : 0.0F;
		float alpha = 1.0F - rainLevel;
		RenderSystem.setShaderColor(BLOOD_MOON_RED, BLOOD_MOON_GREEN, BLOOD_MOON_BLUE, alpha);
	}
}
