package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueMarrowJugglerModel;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The Marrow Juggler's true self in the Dagger Thrower's Chamber (2026-11-17): its own rig, its marrow lit. */
public class TrueMarrowJugglerRenderer extends MobRenderer<MarrowSpitterEntity, TrueMarrowJugglerModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_marrow_juggler.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_marrow_juggler_glow.png"));

	public TrueMarrowJugglerRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueMarrowJugglerModel(context.bakeLayer(TrueMarrowJugglerModel.LAYER_LOCATION)), 0.4F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(MarrowSpitterEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(MarrowSpitterEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) return;
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}
}
