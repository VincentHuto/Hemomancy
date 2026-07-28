package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * The entity emits its point-cloud particles server-side; this renderer keeps
 * the entity registered with the dispatcher without adding a flat backing
 * sprite or model.
 */
public final class HumanitySpriteRenderer extends EntityRenderer<HumanitySpriteEntity> {
	private static final ResourceLocation BLANK = Hemomancy.rloc("textures/entity/blank.png");

	public HumanitySpriteRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public ResourceLocation getTextureLocation(HumanitySpriteEntity entity) {
		return BLANK;
	}
}
