package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.layer.MonolithicDislocationShellLayer;
import com.vincenthuto.hemomancy.client.render.armor.SilentArchonPlayerRenderHelper;
import com.vincenthuto.hemomancy.client.player.WarpChairPlayerPose;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

	@Redirect(
			method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;hasPose(Lnet/minecraft/world/entity/Pose;)Z"),
			remap = false)
	private boolean hemomancy$suppressBedTranslationForWarpChair(LivingEntity entity, Pose pose) {
		if (pose == Pose.SLEEPING && WarpChairPlayerPose.isSeated(entity)) {
			return false;
		}
		return entity.hasPose(pose);
	}

	@Inject(method = "setupRotations", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$useWarpChairSeatedRotation(LivingEntity entity, PoseStack poseStack,
			float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
		if (WarpChairPlayerPose.applyRenderRotation(entity, poseStack)) ci.cancel();
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
