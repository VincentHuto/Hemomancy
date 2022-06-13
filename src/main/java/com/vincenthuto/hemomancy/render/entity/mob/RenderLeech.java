package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityLeech;
import com.vincenthuto.hemomancy.model.entity.mob.ModelLeech;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderLeech extends MobRenderer<EntityLeech, ModelLeech<EntityLeech>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/denizen/model_denizen.png");

	public RenderLeech(Context renderManagerIn) {
		super(renderManagerIn, new ModelLeech<EntityLeech>(renderManagerIn.bakeLayer(ModelLeech.LAYER_LOCATION)), 0.5F);

	}

	@Override
	public ResourceLocation getTextureLocation(EntityLeech entity) {
		return entity.getLeechTypeName();

	}

}
