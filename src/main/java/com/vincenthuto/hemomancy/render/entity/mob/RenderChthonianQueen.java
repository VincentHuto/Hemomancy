package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.EntityChthonianQueen;
import com.vincenthuto.hemomancy.model.entity.mob.ModelChthonianQueen;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class RenderChthonianQueen extends MobRenderer<EntityChthonianQueen, ModelChthonianQueen<EntityChthonianQueen>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chthonian_queen/model_chthonian_queen.png");

	public RenderChthonianQueen(Context renderManagerIn) {
		super(renderManagerIn, new ModelChthonianQueen<EntityChthonianQueen>(renderManagerIn.bakeLayer(ModelChthonianQueen.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(EntityChthonianQueen entity) {
		return TEXTURE;

	}

}
