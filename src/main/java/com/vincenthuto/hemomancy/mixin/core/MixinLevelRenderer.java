package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.systems.RenderSystem;

import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.client.Minecraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.LevelRenderer;

/**
 * Client-only mixin that tints the vanilla overworld moon red when
 * a Blood Moon is active.
 *
 * <p>Injection point: immediately after {@code ClientLevel.getMoonPhase()}
 * is called inside {@code LevelRenderer.renderSky}. At that moment the moon
 * UV coordinates are being computed and the shader colour set here becomes the
 * active multiplier when the moon quad is uploaded, producing a blood-red tint.
 *
 * <p>{@code require = 0} is used so that a Forge/Mixin patch change that
 * renames or removes the injection target does not crash the game — the moon
 * simply stays white instead.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

	/** Red channel for the blood-red moon: full red. */
	private static final float BLOOD_MOON_RED   = 1.00F;
	/** Green channel: near-zero to suppress green, giving a vivid crimson. */
	private static final float BLOOD_MOON_GREEN = 0.05F;
	/** Blue channel: near-zero to suppress blue, giving a vivid crimson. */
	private static final float BLOOD_MOON_BLUE  = 0.05F;

	/**
	 * Set the shader colour to blood-red right before the moon quad is rendered.
	 * The vanilla {@code renderSky} method ends with
	 * {@code RenderSystem.setShaderColor(1, 1, 1, 1)} which naturally resets
	 * the colour; subsequent star/void geometry uses the {@code POSITION} shader
	 * which ignores the colour modulator, so no explicit reset is required.
	 */
	@Inject(
		method = "renderSky",
		at = @At(
			value = "INVOKE",
			target = "net/minecraft/client/multiplayer/ClientLevel.getMoonPhase()I",
			shift = At.Shift.AFTER
		),
		require = 0
	)
	private void hemomancy$applyBloodMoonTint(CallbackInfo ci) {
		if (!BloodMoonClientState.isActive()) return;
		Minecraft mc = Minecraft.getInstance();
		float rainLevel = (mc.level != null) ? mc.level.getRainLevel(mc.getFrameTime()) : 0.0F;
		float alpha = 1.0F - rainLevel;
		// Suppress green and blue channels to produce a vivid blood-red moon
		RenderSystem.setShaderColor(BLOOD_MOON_RED, BLOOD_MOON_GREEN, BLOOD_MOON_BLUE, alpha);
	}
}
