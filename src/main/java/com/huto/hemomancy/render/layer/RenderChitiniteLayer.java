package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityChitinite;
import com.huto.hemomancy.model.entity.mob.ModelChitinite;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChitiniteLayer extends RenderLayer<EntityChitinite, ModelChitinite> {

	private static final RenderType RENDER_TYPE = RenderType
			.eyes(new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/chitinite/model_chitinite_warn.png"));

	public RenderChitiniteLayer(RenderLayerParent<EntityChitinite, ModelChitinite> entityRendererIn) {
		super(entityRendererIn);
	}

	public RenderType getRenderType() {
		return RENDER_TYPE;
	}

	@Override
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn,
			EntityChitinite entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch) {
		if (!entitylivingbaseIn.noActiveAnimation()) {
			if (entitylivingbaseIn.getAnimation() == EntityChitinite.ROLLUP_ANIMATION) {
				IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RENDER_TYPE);
				this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, 15728640, OverlayTexture.NO_OVERLAY,
						1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
	}
}
