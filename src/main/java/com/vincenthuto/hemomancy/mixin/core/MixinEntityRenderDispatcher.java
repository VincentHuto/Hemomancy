package com.vincenthuto.hemomancy.mixin.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.data.CrimsonFireClientState;
import com.vincenthuto.hemomancy.client.render.CrimsonFireRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
	@Shadow(remap = false)
	private Quaternionf cameraOrientation;

	@Inject(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;displayFireAnimation()Z"),
			remap = false)
	private void hemomancy$renderCrimsonFire(Entity entity, double x, double y, double z, float rotationYaw,
			float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
		if (entity.displayFireAnimation() && CrimsonFireClientState.isActive(entity)) {
			CrimsonFireRenderer.renderEntityFlames(poseStack, buffer, entity,
					Mth.rotationAroundAxis(Mth.Y_AXIS, this.cameraOrientation, new Quaternionf()));
		}
	}

	@Redirect(
			method = "render(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;displayFireAnimation()Z"),
			remap = false)
	private boolean hemomancy$suppressVanillaFireForCrimson(Entity entity) {
		return entity.displayFireAnimation() && !CrimsonFireClientState.isActive(entity);
	}
}
