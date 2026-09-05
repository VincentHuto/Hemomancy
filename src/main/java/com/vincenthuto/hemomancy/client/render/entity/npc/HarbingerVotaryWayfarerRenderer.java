package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerVotaryWayfarerModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVotaryWayfarerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerVotaryWayfarerRenderer extends
		MobRenderer<HarbingerVotaryWayfarerEntity, HarbingerVotaryWayfarerModel<HarbingerVotaryWayfarerEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy
			.rloc("textures/entity/npc/harbinger/harbinger_votary_wayfarer/harbinger_votary_wayfarer.png");

	public HarbingerVotaryWayfarerRenderer(Context context) {
		super(context,
				new HarbingerVotaryWayfarerModel<>(context.bakeLayer(HarbingerVotaryWayfarerModel.LAYER_LOCATION)),
				0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HarbingerVotaryWayfarerEntity entity) {
		return TEXTURE;
	}
}
