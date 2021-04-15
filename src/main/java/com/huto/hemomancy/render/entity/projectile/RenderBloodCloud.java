package com.huto.hemomancy.render.entity.projectile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.blood.EntityBloodCloud;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderBloodCloud extends EntityRenderer<EntityBloodCloud> {

	public RenderBloodCloud(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Nonnull
	@Override
	public ResourceLocation getEntityTexture(@Nonnull EntityBloodCloud entity) {
		return new ResourceLocation(Hemomancy.MOD_ID + "textures/entity/tracker.png");
	}

}