package com.vincenthuto.hemomancy.render.entity.mob;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.entity.mob.ChthonianEntity;
import com.vincenthuto.hemomancy.model.entity.mob.ChthonianModel;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChthonianRenderer extends MobRenderer<ChthonianEntity, ChthonianModel<ChthonianEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/chthonian/model_chthonian.png");

	public ChthonianRenderer(Context renderManagerIn) {
		super(renderManagerIn, new ChthonianModel<ChthonianEntity>(renderManagerIn.bakeLayer(ChthonianModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(ChthonianEntity entity) {
		return TEXTURE;

	}

}
