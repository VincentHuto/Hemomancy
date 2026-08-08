package com.vincenthuto.hemomancy.client.render.layer.mob.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.VesperTheCrownedRefusalModel;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Blood-crafting-style shimmer over the throne and mount while Vesper absorbs them. */
public final class VesperMountAbsorptionLayer
		extends RenderLayer<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> {
	private static final int BLOOD_RED = 0xE6FF1208;

	public VesperMountAbsorptionLayer(
			RenderLayerParent<VesperTheCrownedRefusalEntity, VesperTheCrownedRefusalModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		float transitionFrame = entity.getTransitionTick() + partialTick;
		float progress = VesperPhaseTransitionRules.absorptionProgress(transitionFrame);
		if (progress <= 0.0F || progress >= 1.0F || entity.isInvisible()) return;

		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		float centerX = (float) (Mth.lerp(partialTick, entity.xOld, entity.getX()) - camera.x);
		float groundY = (float) (Mth.lerp(partialTick, entity.yOld, entity.getY()) - camera.y);
		float centerY = groundY + entity.getBbHeight() * 0.68F;
		float centerZ = (float) (Mth.lerp(partialTick, entity.zOld, entity.getZ()) - camera.z);
		RenderType melt = HemoRenderTypes.cardinalStaffBloodMelt(
				ageInTicks, entity.getId() * 0.137F, 0.075F + progress * 0.045F,
				centerX, centerY, centerZ, progress, groundY, entity.getBbHeight());
		VertexConsumer consumer = buffer.getBuffer(melt);
		getParentModel().renderMountAssembly(poseStack, consumer, LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, BLOOD_RED);
	}
}
