package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperMountModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWoundRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * While a wound is open Vesper takes no ordinary damage, so the whole body says so: mount and rider wear a glowing
 * blood skin whose rim lights the silhouette and whose vessels beat toward the wound, quickening as it is drained.
 */
public final class VesperWoundDrawLayer extends RenderLayer<VesperTheCrownedRefusalEntity, VesperMountModel> {
	private static final ResourceLocation MOUNT = Hemomancy.rloc("textures/entity/boss/endgame/vesper_mount.png");

	private final VesperRiderLayer rider;

	public VesperWoundDrawLayer(RenderLayerParent<VesperTheCrownedRefusalEntity, VesperMountModel> parent,
			VesperRiderLayer rider) {
		super(parent);
		this.rider = rider;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		int anchor = entity.getActiveAnchor();
		if (anchor < 0 || entity.getTransitionTick() > 0 || entity.isInvisible()) return;
		float progress = Mth.clamp(entity.getWoundDrained() / VesperWoundRules.DRAIN_REQUIRED, 0.0F, 1.0F);
		float flash = VesperCombatRules.anchorFlashStrength(entity.getAnchorFlashTicks());

		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		Vec3 socket = entity.woundCenter(anchor).subtract(entity.position());
		float woundX = (float) (Mth.lerp(partialTick, entity.xOld, entity.getX()) + socket.x - camera.x);
		float woundY = (float) (Mth.lerp(partialTick, entity.yOld, entity.getY()) + socket.y - camera.y);
		float woundZ = (float) (Mth.lerp(partialTick, entity.zOld, entity.getZ()) + socket.z - camera.z);
		float yaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot) * Mth.DEG_TO_RAD;
		float seed = (entity.getId() & 255) * 0.731F;

		getParentModel().renderMountAssembly(poseStack, buffer.getBuffer(HemoRenderTypes.vesperWoundDraw(MOUNT,
				ageInTicks, woundX, woundY, woundZ, yaw, progress, flash, seed)),
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);

		// The rider layer has already posed the rider model for this frame.
		poseStack.pushPose();
		rider.applyRiderTransform(poseStack);
		rider.rider().renderToBuffer(poseStack, buffer.getBuffer(HemoRenderTypes.vesperWoundDraw(
				VesperRiderLayer.TEXTURE, ageInTicks, woundX, woundY, woundZ, yaw, progress, flash, seed)),
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		poseStack.popPose();
	}
}
