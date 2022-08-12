package com.vincenthuto.hemomancy.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.model.entity.mob.EnthralledDollModel;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class EnthralledDollGlowLayer<T extends LivingEntity> extends EyesLayer<T, EnthralledDollModel<T>> {

	private static final RenderType GLOW = RenderType.eyes(
			new ResourceLocation(Hemomancy.MOD_ID, "textures/entity/enthralled_doll/model_enthralled_doll_glow.png"));

	public EnthralledDollGlowLayer(RenderLayerParent<T, EnthralledDollModel<T>> p_116981_) {
		super(p_116981_);
		// TODO Auto-generated constructor stub
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