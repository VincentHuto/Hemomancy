package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.item.shared.armor.PlayerLayerHidingArmor;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class MixinCapeLayer {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$hideCapeUnderOversizedArmor(PoseStack poseStack, MultiBufferSource buffer,
			int packedLight, AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
			float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		if (PlayerLayerHidingArmor.isWorn(player)) ci.cancel();
	}
}
