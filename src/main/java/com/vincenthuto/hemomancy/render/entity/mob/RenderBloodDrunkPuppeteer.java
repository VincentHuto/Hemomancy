package com.vincenthuto.hemomancy.render.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityBloodDrunkPuppeteer;
import com.vincenthuto.hemomancy.model.entity.mob.ModelBloodDrunkPuppeteer;
import com.vincenthuto.hemomancy.render.layer.LayerPuppteerGlow;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderBloodDrunkPuppeteer
		extends MobRenderer<EntityBloodDrunkPuppeteer, ModelBloodDrunkPuppeteer<EntityBloodDrunkPuppeteer>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer.png");

	public RenderBloodDrunkPuppeteer(Context renderManagerIn) {
		super(renderManagerIn,
				new ModelBloodDrunkPuppeteer(renderManagerIn.bakeLayer(ModelBloodDrunkPuppeteer.LAYER_LOCATION)), 0.5F);
		this.addLayer(new LayerPuppteerGlow<>(this));

	}

	@Override
	public void render(EntityBloodDrunkPuppeteer pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityBloodDrunkPuppeteer entity) {
		return TEXTURE;

	}

}
