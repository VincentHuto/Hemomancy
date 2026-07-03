package com.vincenthuto.hemomancy.client.render.entity.mob.will;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.will.WillModel;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillOrigin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class WillRenderer extends MobRenderer<WillEntity, WillModel> {
	private static final ResourceLocation BROKEN_TEXTURE =
			Hemomancy.rloc("textures/entity/wretched_will/modelwretchedwill.png");
	private static final ResourceLocation SENT_TEXTURE =
			Hemomancy.rloc("textures/entity/wretched_will/modelwretchedwill.png");

	public WillRenderer(EntityRendererProvider.Context context) {
		super(context, new WillModel(context.bakeLayer(WillModel.LAYER_LOCATION)), 0.35F);
	}

	@Override
	public ResourceLocation getTextureLocation(WillEntity entity) {
		return entity.getOrigin() == WillOrigin.SENT ? SENT_TEXTURE : BROKEN_TEXTURE;
	}
}
