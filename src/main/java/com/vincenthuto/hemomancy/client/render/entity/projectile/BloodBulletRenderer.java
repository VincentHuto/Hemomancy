package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodBulletEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class BloodBulletRenderer extends EntityRenderer<BloodBulletEntity> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/wretched_will/modelwretchedwill.png");

	public BloodBulletRenderer(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(BloodBulletEntity entity) {
		return TEXTURE;
	}

}
