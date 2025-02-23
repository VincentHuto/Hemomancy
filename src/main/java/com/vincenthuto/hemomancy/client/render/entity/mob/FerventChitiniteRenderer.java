package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.FerventChitiniteModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.FerventChitiniteCrystalLayer;
import com.vincenthuto.hemomancy.common.entity.mob.FerventChitiniteEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class FerventChitiniteRenderer extends MobRenderer<FerventChitiniteEntity, FerventChitiniteModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/fervent_chitinite/model_fervent_chitinite.png");

	public FerventChitiniteRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new FerventChitiniteModel(renderManagerIn.bakeLayer(FerventChitiniteModel.LAYER_LOCATION)), 0.5F);
	      this.addLayer(new FerventChitiniteCrystalLayer(this, renderManagerIn.getModelSet()));

	}
	
	@Override
	public void render(FerventChitiniteEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
	
	}

	

	@Override
	public ResourceLocation getTextureLocation(FerventChitiniteEntity entity) {
		return TEXTURE;

	}


	
}
