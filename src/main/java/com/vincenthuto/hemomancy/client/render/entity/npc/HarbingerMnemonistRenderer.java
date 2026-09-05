package com.vincenthuto.hemomancy.client.render.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.npc.HarbingerMnemonistModel;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HarbingerMnemonistRenderer extends MobRenderer<HarbingerMnemonistEntity, HarbingerMnemonistModel<HarbingerMnemonistEntity>> {
	protected static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/npc/harbinger/harbinger_mnemonist/harbinger_mnemonist.png");

	public HarbingerMnemonistRenderer(Context context) {
		super(context, new HarbingerMnemonistModel<>(context.bakeLayer(HarbingerMnemonistModel.LAYER_LOCATION)), 0.5F);
	}

	@Override
	public ResourceLocation getTextureLocation(HarbingerMnemonistEntity entity) {
		return TEXTURE;
	}
}
