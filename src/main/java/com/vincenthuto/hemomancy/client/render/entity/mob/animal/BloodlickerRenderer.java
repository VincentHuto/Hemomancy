package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.BloodlickerModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.BloodlickerEntity;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class BloodlickerRenderer
		extends MobRenderer<BloodlickerEntity, BloodlickerModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/bloodlicker/model_bloodlicker.png");

	public BloodlickerRenderer(EntityRendererProvider.Context context) {
		super(context, new BloodlickerModel(), 0.7F);
	}

	@Override
	public boolean shouldRender(BloodlickerEntity entity, Frustum frustum, double x, double y, double z) {
		return super.shouldRender(entity, frustum, x, y, z)
				|| (entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBoxForCulling().inflate(1.2D)));
	}

	@Override
	public ResourceLocation getTextureLocation(BloodlickerEntity entity) {
		return TEXTURE;
	}
}
