package com.huto.hemomancy.render.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.projectile.EntityBloodOrbDirected;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBloodOrbDirected extends EntityRenderer<EntityBloodOrbDirected> {

	public RenderBloodOrbDirected(Context renderManager) {
		super(renderManager);
	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull EntityBloodOrbDirected entity) {
		return new ResourceLocation(Hemomancy.MOD_ID + "textures/entity/tracker.png");
	}

}