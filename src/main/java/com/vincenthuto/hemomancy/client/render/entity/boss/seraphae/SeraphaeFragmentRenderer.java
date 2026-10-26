package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * The shared Lux pass draws radiant shards and feather frays instead of the old opaque placeholder.
 */
public class SeraphaeFragmentRenderer extends EntityRenderer<SeraphaeFragmentEntity> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/boss/seraphae/seraphae_fragment.png");

	public SeraphaeFragmentRenderer(Context context) {
		super(context);
		this.shadowRadius = .2F;
	}

	@Override
	public ResourceLocation getTextureLocation(SeraphaeFragmentEntity entity) {
		return TEXTURE;
	}
}
