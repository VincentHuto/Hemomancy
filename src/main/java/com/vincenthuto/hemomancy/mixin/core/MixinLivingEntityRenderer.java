package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.render.armor.SilentArchonPlayerRenderHelper;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class MixinLivingEntityRenderer {
	@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$useSilentArchonMonolithPlayerCore(LivingEntity entity, boolean bodyVisible,
			boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
		System.out.println("Checking if we should use Silent Archon Player Core Render Type for player: " + entity.getName().getString());
		if (entity instanceof AbstractClientPlayer player && bodyVisible && !translucent
				&& SilentArchonPlayerRenderHelper.hasFullSilentArchonSet(player)) {
			System.out.println("Using Silent Archon Player Core Render Type for player: " + player.getName().getString());
			cir.setReturnValue(SilentArchonPlayerRenderHelper.playerCoreRenderType(player));
		}
	}
}
