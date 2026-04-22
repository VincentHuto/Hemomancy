package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeFragmentModel;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeFragmentEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for Seraphae fragments — small radiant shards.
 */
public class SeraphaeFragmentRenderer extends MobRenderer<SeraphaeFragmentEntity, SeraphaeFragmentModel<SeraphaeFragmentEntity>> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/seraphae_fragment/seraphae_fragment.png");

	public SeraphaeFragmentRenderer(Context context) {
		super(context, new SeraphaeFragmentModel<>(context.bakeLayer(SeraphaeFragmentModel.LAYER_LOCATION)), 0.3F);
	}

	@Override
	public ResourceLocation getTextureLocation(SeraphaeFragmentEntity entity) {
		return TEXTURE;
	}
}
