package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.EntityLeech;
import com.huto.hemomancy.model.entity.ModelLeech;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

public class RenderLeech extends MobRenderer<EntityLeech, ModelLeech> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/denizen/model_denizen.png");

	public RenderLeech(EntityRendererManager renderManagerIn) {
		super(renderManagerIn, new ModelLeech(), 0.1f);

	}

	@Override
	public ResourceLocation getEntityTexture(EntityLeech entity) {
		return entity.getLeechTypeName();

	}

}
