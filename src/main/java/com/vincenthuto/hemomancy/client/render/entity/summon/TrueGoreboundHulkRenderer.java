package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueGoreboundHulkModel;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The Gorebound Hulk's true self in the Strongman's Chamber (2026-11-17): its own rig, its blood-vents glowing. */
public class TrueGoreboundHulkRenderer extends MobRenderer<GoreboundHulkEntity, TrueGoreboundHulkModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_gorebound_hulk.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_gorebound_hulk_glow.png"));

	public TrueGoreboundHulkRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueGoreboundHulkModel(context.bakeLayer(TrueGoreboundHulkModel.LAYER_LOCATION)), 0.8F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(GoreboundHulkEntity entity) {
		return TEXTURE;
	}
}
