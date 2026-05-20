package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.utility.CovenantThroneSeatEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CovenantThroneSeatRenderer extends EntityRenderer<CovenantThroneSeatEntity> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/blank.png");

	public CovenantThroneSeatRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(CovenantThroneSeatEntity entity) {
		return TEXTURE;
	}
}
