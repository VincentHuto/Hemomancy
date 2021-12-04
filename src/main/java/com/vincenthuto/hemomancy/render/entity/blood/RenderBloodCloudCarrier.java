package com.vincenthuto.hemomancy.render.entity.blood;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.blood.EntityBloodCloudCarrier;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBloodCloudCarrier extends EntityRenderer<EntityBloodCloudCarrier> {

	public RenderBloodCloudCarrier(Context renderManager) {
		super(renderManager);
	}

	@Nonnull
	@Override
	public ResourceLocation getTextureLocation(@Nonnull EntityBloodCloudCarrier entity) {
		return new ResourceLocation(Hemomancy.MOD_ID + "textures/entity/tracker.png");
	}

}