package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.ContainmentAnchorModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Containment Anchors — static arena pylons.
 * Uses a simple EntityRenderer since ContainmentAnchorEntity extends Entity (not Mob).
 */
public class ContainmentAnchorRenderer extends EntityRenderer<ContainmentAnchorEntity> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/containment_anchor/containment_anchor.png");

	private final ContainmentAnchorModel model;

	public ContainmentAnchorRenderer(Context context) {
		super(context);
		this.model = new ContainmentAnchorModel(context.bakeLayer(ContainmentAnchorModel.LAYER_LOCATION));
	}

	@Override
	public ResourceLocation getTextureLocation(ContainmentAnchorEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(ContainmentAnchorEntity entity, float entityYaw, float partialTicks,
					   PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

		poseStack.pushPose();

		// Animate the model
		model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);

		VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		model.renderToBuffer(poseStack, vertexconsumer, packedLight,
				OverlayTexture.NO_OVERLAY, -1);

		poseStack.popPose();
	}
}

