package com.vincenthuto.hemomancy.client.render.entity.mob.arthropod;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.arthropod.VenomRibCentipedeModel;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.VenomRibCentipedeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class VenomRibCentipedeRenderer extends MobRenderer<VenomRibCentipedeEntity, VenomRibCentipedeModel> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/venom_rib_centipede/model_venom_rib_centipede.png");

	public VenomRibCentipedeRenderer(Context context) {
		super(context, new VenomRibCentipedeModel(context.bakeLayer(VenomRibCentipedeModel.LAYER_LOCATION)), 0.55F);
	}

	@Override
	public ResourceLocation getTextureLocation(VenomRibCentipedeEntity entity) {
		return TEXTURE;
	}
}
