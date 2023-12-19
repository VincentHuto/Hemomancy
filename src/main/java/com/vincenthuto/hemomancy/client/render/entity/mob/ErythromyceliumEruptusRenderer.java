package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.ErythromyceliumEruptusModel;
import com.vincenthuto.hemomancy.common.entity.mob.ErythromyceliumEruptusEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ErythromyceliumEruptusRenderer
		extends MobRenderer<ErythromyceliumEruptusEntity, ErythromyceliumEruptusModel> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png");

	public ErythromyceliumEruptusRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new ErythromyceliumEruptusModel(renderManagerIn.bakeLayer(ErythromyceliumEruptusModel.LAYER_LOCATION)),
				0.5F);
		
	}
 @Override
public void render(ErythromyceliumEruptusEntity pEntity, float pEntityYaw, float pPartialTicks,
		PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

	 	if(pEntity.isErupted()) {
	 		pPoseStack.translate(0, 1, 0);
	 	}
	 super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);

 
 }
	@Override
	protected void scale(ErythromyceliumEruptusEntity pLivingEntity, PoseStack pPoseStack, float pPartialTickTime) {
		float f = pLivingEntity.getSwelling(pPartialTickTime);
		float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
		f = Mth.clamp(f, 0.0F, 1.0F);
		f *= f;
		f *= f;
		float f2 = (1.0F + f * 0.4F) * f1;
		float f3 = (1.0F + f * 0.1F) / f1;
		pPoseStack.scale(f2, f3, f2);
	}

	@Override
	protected float getWhiteOverlayProgress(ErythromyceliumEruptusEntity pLivingEntity, float pPartialTicks) {
		float f = pLivingEntity.getSwelling(pPartialTicks);
		return (int) (f * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0.5F, 1.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(ErythromyceliumEruptusEntity entity) {
		return TEXTURE;

	}

}
