package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.HunterFungusModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.HunterFungusEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HunterFungusRenderer extends MobRenderer<HunterFungusEntity, HunterFungusModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/hunter_fungus/hunter_fungus.png");

	public HunterFungusRenderer(EntityRendererProvider.Context context) {
		super(context, new HunterFungusModel(context.bakeLayer(HunterFungusModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HunterFungusEntity entity) {
		return TEXTURE;
	}
}
