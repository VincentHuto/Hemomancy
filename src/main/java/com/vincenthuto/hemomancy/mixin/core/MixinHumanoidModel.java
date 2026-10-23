package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.player.PlayerAnimationRegistry;
import com.vincenthuto.hemomancy.client.player.WarpChairPlayerPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class MixinHumanoidModel {
	@Inject(method = "setupAnim", at = @At("HEAD"), remap = false)
	private void hemomancy$resetWarpChairHeadRoll(
			LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch, CallbackInfo callback) {
		WarpChairPlayerPose.resetHeadRoll((HumanoidModel<?>) (Object) this);
	}

	@Inject(method = "setupAnim", at = @At("TAIL"), remap = false)
	private void hemomancy$applyPlayerAnimationPoses(
			LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch, CallbackInfo callback) {
		PlayerAnimationRegistry.applyThirdPersonPose(entity, (HumanoidModel<?>) (Object) this,
				Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false));
	}
}
