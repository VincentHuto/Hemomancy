package com.vincenthuto.hemomancy.client.render.entity.projectile;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.TrackingSerpentEntity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrackingSerpentRenderer extends EntityRenderer<TrackingSerpentEntity> {

	public TrackingSerpentRenderer(Context renderManager) {
		super(renderManager);
	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull TrackingSerpentEntity entity) {
		return Hemomancy.rloc("textures/entity/tracker.png");
	}

}