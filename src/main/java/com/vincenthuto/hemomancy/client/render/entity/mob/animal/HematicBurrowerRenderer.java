package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.HematicBurrowerModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.HematicBurrowerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HematicBurrowerRenderer extends MobRenderer<HematicBurrowerEntity, HematicBurrowerModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/hematic_burrower/model_hematic_burrower.png");

	public HematicBurrowerRenderer(Context context) {
		super(context, new HematicBurrowerModel(context.bakeLayer(HematicBurrowerModel.LAYER_LOCATION)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(HematicBurrowerEntity entity) {
		return TEXTURE;
	}
}
