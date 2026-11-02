package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.ErythromyceliumEruptusModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ErythromyceliumEruptusEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ErythromyceliumEruptusRenderer
		extends MobRenderer<ErythromyceliumEruptusEntity, ErythromyceliumEruptusModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/erythromycelium_eruptus/model_erythromycelium_eruptus.png");

	public ErythromyceliumEruptusRenderer(Context renderManagerIn) {
		super(renderManagerIn,
				new ErythromyceliumEruptusModel(renderManagerIn.bakeLayer(ErythromyceliumEruptusModel.LAYER_LOCATION)),
				0.5F);
	}

	/** Bloats as it swells, the way a creeper does; the swelling drains away as the eruption starts. */
	@Override
	protected void scale(ErythromyceliumEruptusEntity entity, PoseStack pose, float partialTick) {
		float swelling = entity.getSwelling(partialTick);
		float pulse = 1.0F + Mth.sin(swelling * 100.0F) * swelling * 0.01F;
		float bloat = swelling * swelling * swelling * swelling;
		pose.scale((1.0F + bloat * 0.4F) * pulse, (1.0F + bloat * 0.1F) / pulse, (1.0F + bloat * 0.4F) * pulse);
	}

	@Override
	protected float getWhiteOverlayProgress(ErythromyceliumEruptusEntity entity, float partialTick) {
		if (entity.isErupted()) return 0.0F;
		float swelling = entity.getSwelling(partialTick);
		return (int) (swelling * 10.0F) % 2 == 0 ? 0.0F : Mth.clamp(swelling, 0.5F, 1.0F);
	}

	/** It never tips over: a corpse is already down, and the standing growth curls up in its model. */
	@Override
	protected float getFlipDegrees(ErythromyceliumEruptusEntity entity) {
		return 0.0F;
	}

	@Override
	public ResourceLocation getTextureLocation(ErythromyceliumEruptusEntity entity) {
		return TEXTURE;
	}
}
