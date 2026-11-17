package com.vincenthuto.hemomancy.mixin.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.vincenthuto.hemomancy.client.player.LashCamera;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hands each frame's mouse turn to {@link LashCamera}, which gives the whole of it to a live lash's whip aim and a
 * capped share to the view. NeoForge's turn event only scales sensitivity; the split needs the turn itself. The turn is
 * wrapped, not redirected, so another mod's wrapper of the same call still runs.
 */
@Mixin(MouseHandler.class)
public abstract class MixinMouseHandler {
	@Inject(method = "turnPlayer", at = @At("HEAD"), remap = false)
	private void hemomancy$lashFrame(double movementTime, CallbackInfo ci) {
		LashCamera.frame(movementTime);
	}

	@WrapOperation(method = "turnPlayer",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"), remap = false)
	private void hemomancy$lashTurn(LocalPlayer player, double yaw, double pitch, Operation<Void> original) {
		double[] view = LashCamera.split(yaw, pitch);
		original.call(player, view[0], view[1]);
	}
}
