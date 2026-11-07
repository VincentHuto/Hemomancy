package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeFragmentEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

/**
 * A fragment of her light draws nothing itself: the radiance geometry draws it as a knot of nerve light in the shared
 * Lux and Ductilis batch, with how far a challenger's absorption has drawn it in.
 */
public class SeraphaeFragmentRenderer extends EntityRenderer<SeraphaeFragmentEntity> {
	public SeraphaeFragmentRenderer(Context context) {
		super(context);
		this.shadowRadius = 0.2F;
	}

	@Override
	public ResourceLocation getTextureLocation(SeraphaeFragmentEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}
