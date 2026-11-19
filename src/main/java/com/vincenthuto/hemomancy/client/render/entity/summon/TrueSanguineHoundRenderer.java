package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueSanguineHoundModel;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The Sanguine Hound's true self in the Beast Tamer's Chamber (2026-11-17): its own rig, its eyes and blood seams lit. */
public class TrueSanguineHoundRenderer extends MobRenderer<SanguineHoundEntity, TrueSanguineHoundModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_sanguine_hound.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_sanguine_hound_glow.png"));

	public TrueSanguineHoundRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueSanguineHoundModel(context.bakeLayer(TrueSanguineHoundModel.LAYER_LOCATION)), 0.6F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(SanguineHoundEntity entity) {
		return TEXTURE;
	}
}
