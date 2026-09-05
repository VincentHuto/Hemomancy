package com.vincenthuto.hemomancy.client.render.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.animal.VampireBatModel;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class VampireBatRenderer extends MobRenderer<VampireBatEntity, VampireBatModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/vampire_bat/model_vampire_bat.png");

	public VampireBatRenderer(EntityRendererProvider.Context context) {
		super(context, new VampireBatModel(context.bakeLayer(VampireBatModel.LAYER_LOCATION)), 0.25F);
	}

	@Override
	public ResourceLocation getTextureLocation(VampireBatEntity entity) {
		return TEXTURE;
	}
}
