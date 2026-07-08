package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerArtificerModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerArtificerRenderer
		extends MobRenderer<HarbingerArtificerEntity, HarbingerArtificerModel<HarbingerArtificerEntity>> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/harbinger_artificer/harbinger_artificer.png");

	public HarbingerArtificerRenderer(Context context) {
		super(context, new HarbingerArtificerModel<>(context.bakeLayer(HarbingerArtificerModel.LAYER_LOCATION)),
				0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HarbingerArtificerEntity entity) {
		return TEXTURE;
	}
}
