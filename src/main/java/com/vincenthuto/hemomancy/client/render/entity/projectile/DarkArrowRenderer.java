package com.vincenthuto.hemomancy.client.render.entity.projectile;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.projectile.DarkArrowEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;

public class DarkArrowRenderer extends EntityRenderer<DarkArrowEntity> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/block/end_portal.png");

	public DarkArrowRenderer(Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	public ResourceLocation getTextureLocation(DarkArrowEntity entity) {
		return TEXTURE;
	}

}

