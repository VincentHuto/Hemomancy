package com.vincenthuto.hemomancy.render.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.entity.mob.ModelBloodDrunkPuppeteer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LayerPuppteerGlow<T extends LivingEntity> extends EyesLayer<T, ModelBloodDrunkPuppeteer<T>> {
	private static final RenderType GLOW = RenderType.eyes(new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/blood_drunk_puppeteer/model_blood_drunk_puppeteer_glow.png"));

	public LayerPuppteerGlow(RenderLayerParent<T, ModelBloodDrunkPuppeteer<T>> p_116981_) {
		super(p_116981_);
	}

	@Override
	public void render(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity,
			float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw,
			float pHeadPitch) {
		super.render(pMatrixStack, pBuffer, pPackedLight, pLivingEntity, pLimbSwing, pLimbSwingAmount, pPartialTicks,
				pAgeInTicks, pNetHeadYaw, pHeadPitch);
	}

	public RenderType renderType() {
		return GLOW;
	}
}