package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerCicatrixAnchoriteModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerCicatrixAnchoriteRenderer extends MobRenderer<HarbingerCicatrixAnchoriteEntity,
		HarbingerCicatrixAnchoriteModel<HarbingerCicatrixAnchoriteEntity>> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/npc/harbinger/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite.png");

	public HarbingerCicatrixAnchoriteRenderer(Context context) {
		super(context, new HarbingerCicatrixAnchoriteModel<>(
				context.bakeLayer(HarbingerCicatrixAnchoriteModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HarbingerCicatrixAnchoriteEntity entity) {
		return TEXTURE;
	}
}
