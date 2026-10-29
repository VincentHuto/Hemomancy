package com.vincenthuto.hemomancy.client.render.entity.mob.facsimile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

/** Draws the facsimile's model again in its fungal growth texture, over the borrowed skin. */
public class FacsimileGrowthLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	private final RenderType growth;

	public FacsimileGrowthLayer(RenderLayerParent<T, M> parent, RenderType growth) {
		super(parent);
		this.growth = growth;
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing,
			float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		getParentModel().renderToBuffer(poseStack, buffer.getBuffer(growth), packedLight,
				LivingEntityRenderer.getOverlayCoords(entity, 0.0F), -1);
	}
}
