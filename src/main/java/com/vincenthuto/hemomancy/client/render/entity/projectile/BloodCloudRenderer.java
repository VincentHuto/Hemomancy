package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.CloudEntityBlood;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class BloodCloudRenderer extends EntityRenderer<CloudEntityBlood> {

	public BloodCloudRenderer(Context renderManager) {
		super(renderManager);
	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull CloudEntityBlood entity) {
		return Hemomancy.rloc("textures/entity/tracker.png");
	}

}