package com.vincenthuto.hemomancy.client.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.BloodDrunkPuppeteerModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.PuppteerGlowLayer;
import com.vincenthuto.hemomancy.common.entity.mob.BloodDrunkPuppeteerEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BloodDrunkPuppeteerRenderer
		extends MobRenderer<BloodDrunkPuppeteerEntity, BloodDrunkPuppeteerModel<BloodDrunkPuppeteerEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer.png");

	public BloodDrunkPuppeteerRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new BloodDrunkPuppeteerModel(renderManagerIn.bakeLayer(BloodDrunkPuppeteerModel.LAYER_LOCATION)), 0.5F);
		this.addLayer(new PuppteerGlowLayer<>(this));

	}

	@Override
	public ResourceLocation getTextureLocation(BloodDrunkPuppeteerEntity entity) {
		return TEXTURE;

	}

	@Override
	public void render(BloodDrunkPuppeteerEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
	}

}