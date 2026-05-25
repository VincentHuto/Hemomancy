package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingPolypLayerModel;
import com.vincenthuto.hemomancy.client.model.entity.summon.MorphlingPolypModel;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class MorphlingPolypAppendageLayer
		extends RenderLayer<MorphlingPolypEntity, MorphlingPolypModel<MorphlingPolypEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/morphling_polyp/model_morphling_polyp_layers.png");

	private final MorphlingPolypLayerModel model;

	public MorphlingPolypAppendageLayer(
			RenderLayerParent<MorphlingPolypEntity, MorphlingPolypModel<MorphlingPolypEntity>> parent,
			EntityModelSet modelSet) {
		super(parent);
		this.model = new MorphlingPolypLayerModel(modelSet.bakeLayer(MorphlingPolypLayerModel.LAYER_LOCATION));
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			MorphlingPolypEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.getLayers().isEmpty() || entity.isInvisible()) {
			return;
		}
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		int packedOverlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
		for (MorphlingPolypLayer layer : entity.getLayers()) {
			this.model.renderLayer(layer, poseStack, vertexConsumer, packedLight, packedOverlay, layer.packedColor());
		}
	}
}
