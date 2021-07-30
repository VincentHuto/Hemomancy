package com.huto.hemomancy.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import LivingEntity;

public class RenderLivingBladeAttack<T extends LivingEntity> extends RenderLayer<T, PlayerModel<T>> {
	public static final ResourceLocation field_204836_a = new ResourceLocation("textures/entity/trident_riptide.png");
	private final ModelPart field_229143_b_ = new ModelPart(64, 64, 0, 0);

	public RenderLivingBladeAttack(RenderLayerParent<T, PlayerModel<T>> p_i50920_1_) {
		super(p_i50920_1_);
		this.field_229143_b_.addBox(-8.0F, -16.0F, -8.0F, 16.0F, 32.0F, 16.0F);
	}

	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entitylivingbaseIn,
			float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch) {
		if (entitylivingbaseIn.isSpinAttacking()) {
			IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutoutNoCull(field_204836_a));

			for (int i = 0; i < 3; ++i) {
				matrixStackIn.push();
				float f = ageInTicks * (float) (-(45 + i * 5));
				matrixStackIn.rotate(Vector3f.YP.rotationDegrees(f));
				float f1 = 0.75F * (float) i;
				matrixStackIn.scale(f1, f1, f1);
				matrixStackIn.translate(0.0D, (double) (-0.2F + 0.6F * (float) i), 0.0D);
				this.field_229143_b_.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY);
				matrixStackIn.pop();
			}

		}
	}
}
