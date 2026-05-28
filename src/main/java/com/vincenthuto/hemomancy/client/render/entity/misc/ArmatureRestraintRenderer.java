package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class ArmatureRestraintRenderer extends EntityRenderer<ArmatureRestraintEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

	public ArmatureRestraintRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(ArmatureRestraintEntity entity) {
		return TEXTURE;
	}
}
