package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.EnthralledDollModel;
import com.vincenthuto.hemomancy.client.render.layer.mob.EnthralledDollGlowLayer;
import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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
	protected void scale(EnthralledDollEntity entity, PoseStack poseStack, float partialTick) {
		if (entity.deathTime <= 0) return;
		float melt = Mth.clamp((entity.deathTime + partialTick) / 20.0F, 0.0F, 1.0F);
		poseStack.scale(1.0F + melt * 0.25F, 1.0F - melt * 0.92F, 1.0F + melt * 0.25F);
	}

	@Override
	protected float getFlipDegrees(EnthralledDollEntity entity) {
		return 0.0F;
	}

}
