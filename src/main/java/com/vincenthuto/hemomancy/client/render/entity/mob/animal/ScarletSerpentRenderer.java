package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.ScarletSerpentModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.ScarletSerpentVariant;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ScarletSerpentRenderer extends MobRenderer<ScarletSerpentEntity, ScarletSerpentModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/scarlet_serpent/model_scarlet_serpent.png");
	private static final ResourceLocation DESERT_TEXTURE = Hemomancy.rloc(
			"textures/entity/scarlet_serpent/model_scarlet_serpent_desert.png");
	private static final ResourceLocation SWAMP_TEXTURE = Hemomancy.rloc(
			"textures/entity/scarlet_serpent/model_scarlet_serpent_swamp.png");

	public ScarletSerpentRenderer(Context context) {
		super(context, new ScarletSerpentModel(), 0.35F);
	}

	@Override
	protected void scale(ScarletSerpentEntity entity, PoseStack poseStack, float partialTick) {
		poseStack.scale(.72F, .72F, .72F);
	}

	@Override
	public boolean shouldRender(ScarletSerpentEntity entity, Frustum frustum, double x, double y, double z) {
		return super.shouldRender(entity, frustum, x, y, z)
				|| (entity.shouldRender(x, y, z)
					&& frustum.isVisible(entity.getBoundingBoxForCulling().inflate(3.2, .85, 3.2)));
	}

	@Override
	public ResourceLocation getTextureLocation(ScarletSerpentEntity entity) {
		ScarletSerpentVariant variant = entity.getVariant();
		if (variant == ScarletSerpentVariant.DESERT_RED) {
			return DESERT_TEXTURE;
		}
		if (variant == ScarletSerpentVariant.SWAMP_BROWN) {
			return SWAMP_TEXTURE;
		}
		return TEXTURE;
	}
}
