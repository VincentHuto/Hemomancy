package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.ChitiniteModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.ChitiniteEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChitiniteRenderer extends MobRenderer<ChitiniteEntity, ChitiniteModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/chitinite/model_chitinite.png");

	public ChitiniteRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new ChitiniteModel(renderManagerIn.bakeLayer(ChitiniteModel.LAYER_LOCATION)), 0.5F);
	//	this.addLayer(new RenderChitiniteLayer(this));

	}

	@Override
	public ResourceLocation getTextureLocation(ChitiniteEntity entity) {
		return TEXTURE;

	}

	@Override
	public void render(ChitiniteEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
	}

}
