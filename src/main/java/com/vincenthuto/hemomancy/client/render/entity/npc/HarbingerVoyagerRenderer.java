package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerVoyagerModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerVoyagerRenderer
		extends MobRenderer<HarbingerVoyagerEntity, HarbingerVoyagerModel<HarbingerVoyagerEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy
			.rloc("textures/entity/harbinger_voyager/harbinger_voyager.png");

	public HarbingerVoyagerRenderer(Context context) {
		super(context, new HarbingerVoyagerModel<>(context.bakeLayer(HarbingerVoyagerModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HarbingerVoyagerEntity entity) {
		return TEXTURE;
	}
}
