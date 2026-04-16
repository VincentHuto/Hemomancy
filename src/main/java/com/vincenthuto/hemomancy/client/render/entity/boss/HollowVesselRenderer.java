package com.vincenthuto.hemomancy.client.render.entity.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerVicarModel;
import com.vincenthuto.hemomancy.common.entity.boss.HollowVesselEntity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Reuses the Harbinger Vicar mesh and texture — visually, the Hollow Vessel
 * is a hollowed-out saintly corpus in the same ceremonial vestments.
 */
public class HollowVesselRenderer extends MobRenderer<HollowVesselEntity, HumanoidModel<HollowVesselEntity>> {

	protected static final ResourceLocation TEXTURE = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/harbinger_vicar/harbinger_vicar.png");

	public HollowVesselRenderer(Context context) {
		super(context, new HumanoidModel<>(context.bakeLayer(HarbingerVicarModel.LAYER_LOCATION)), 0.7F);
	}

	@Override
	public ResourceLocation getTextureLocation(HollowVesselEntity entity) {
		return TEXTURE;
	}
}
