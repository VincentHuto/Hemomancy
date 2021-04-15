package com.huto.hemomancy.render.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.projectile.EntityBloodCloudCarrier;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBloodCloudCarrier extends EntityRenderer<EntityBloodCloudCarrier> {

	public RenderBloodCloudCarrier(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Nonnull
	@Override
	public ResourceLocation getEntityTexture(@Nonnull EntityBloodCloudCarrier entity) {
		return new ResourceLocation(Hemomancy.MOD_ID + "textures/entity/tracker.png");
	}

}