package com.vincenthuto.hemomancy.client.render.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.monster.SynapseHoundModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.SynapseHoundEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SynapseHoundRenderer extends MobRenderer<SynapseHoundEntity, SynapseHoundModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/synapse_hound/model_synapse_hound.png");

	public SynapseHoundRenderer(Context renderManagerIn) {
		super(renderManagerIn, new SynapseHoundModel(renderManagerIn.bakeLayer(SynapseHoundModel.LAYER_LOCATION)), 0.4F);
	}

	@Override
	public ResourceLocation getTextureLocation(SynapseHoundEntity entity) {
		return TEXTURE;
	}
}
