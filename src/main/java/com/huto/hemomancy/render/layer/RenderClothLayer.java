package com.huto.hemomancy.render.layer;

import com.huto.hemomancy.model.entity.armor.ModelLivingBladeHip;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;

public class RenderClothLayer<T extends PlayerEntity, M extends PlayerModel<T>> extends LayerRenderer<T, M> {
	ModelLivingBladeHip model = new ModelLivingBladeHip();

	public RenderClothLayer(IEntityRenderer<T, M> entityRendererIn) {
		super(entityRendererIn);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer iRenderTypeBuffer, int packedLight,
			PlayerEntity player, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {

			
		
	
	}
}