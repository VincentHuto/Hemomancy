package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.BrinedVotaryModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.BrinedVotaryEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BrinedVotaryRenderer extends MobRenderer<BrinedVotaryEntity, BrinedVotaryModel<BrinedVotaryEntity>> {
	private static final ResourceLocation TEXTURE = Hemomancy.rloc(
			"textures/entity/brined_votary/brined_votary.png");

	public BrinedVotaryRenderer(Context context) {
		super(context, new BrinedVotaryModel<>(context.bakeLayer(BrinedVotaryModel.LAYER_LOCATION)), 0.45F);
	}

	@Override
	public ResourceLocation getTextureLocation(BrinedVotaryEntity entity) {
		return TEXTURE;
	}
}
