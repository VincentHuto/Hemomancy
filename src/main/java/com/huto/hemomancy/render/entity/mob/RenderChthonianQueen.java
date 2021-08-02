package com.huto.hemomancy.render.entity.mob;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.entity.mob.EntityChthonianQueen;
import com.huto.hemomancy.model.entity.mob.ModelChthonianQueen;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderChthonianQueen extends MobRenderer<EntityChthonianQueen, ModelChthonianQueen> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chthonian_queen/model_chthonian_queen.png");

	public RenderChthonianQueen(EntityRenderDispatcher renderManagerIn) {
		super(renderManagerIn, new ModelChthonianQueen(), 0.45f);
	}

	@Override
	public ResourceLocation getEntityTexture(EntityChthonianQueen entity) {
		return TEXTURE;

	}

}
