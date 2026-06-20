package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.EnthralledDollModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.EnthralledDollGlowLayer;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class EnthralledDollRenderer
		extends MobRenderer<EnthralledDollEntity, EnthralledDollModel<EnthralledDollEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/enthralled_doll/model_enthralled_doll.png");

	public EnthralledDollRenderer(Context renderManagerIn) {
		super(renderManagerIn, new EnthralledDollModel<EnthralledDollEntity>(
				renderManagerIn.bakeLayer(EnthralledDollModel.LAYER_LOCATION)), 0.1F);
		this.addLayer(new EnthralledDollGlowLayer<>(this));

	}

	@Override
	public ResourceLocation getTextureLocation(EnthralledDollEntity entity) {
		return TEXTURE;

	}

	@Override
	public void render(EnthralledDollEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
			MultiBufferSource pBuffer, int pPackedLight) {
		super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
	}

}
