package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.layer.MonolithicDislocationShellLayer;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonPlayerRenderHelper;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
	@Shadow(remap = false)
	public abstract EntityModel<LivingEntity> getModel();

	@Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$useSilentArchonMonolithPlayerCore(LivingEntity entity, boolean bodyVisible,
			boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
		if (entity instanceof AbstractClientPlayer player && bodyVisible && !translucent
				&& SilentArchonPlayerRenderHelper.hasFullSilentArchonSet(player)) {
			cir.setReturnValue(SilentArchonPlayerRenderHelper.playerCoreRenderType(player));
		}
	}

	@Inject(
			method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"),
			remap = false)
	private void hemomancy$renderMonolithicDislocationShellFallback(LivingEntity entity, float entityYaw,
			float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		MonolithicDislocationShellLayer.renderMixinFallback(entity, this.getModel(), poseStack, buffer);
	}
}
