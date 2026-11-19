package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.TrueCinderBellowsModel;
import com.vincenthuto.hemomancy.client.render.world.CinderBreathFlames;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * The Cinder Bellows' true self in the Fire Breather's Chamber (2026-11-17): its own rig, the fire in its hood folds,
 * spine vents, lungs and torch burning. Its ordinary breath still draws its flames down the same cone.
 */
public class TrueCinderBellowsRenderer extends MobRenderer<CinderBellowsEntity, TrueCinderBellowsModel> {
	private static final ResourceLocation TEXTURE =
			Hemomancy.rloc("textures/entity/puppeteer_summon/true_cinder_bellows.png");
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/true_cinder_bellows_glow.png"));

	public TrueCinderBellowsRenderer(EntityRendererProvider.Context context) {
		super(context, new TrueCinderBellowsModel(context.bakeLayer(TrueCinderBellowsModel.LAYER_LOCATION)), 0.5F);
		addLayer(new EyesLayer<>(this) {
			@Override
			public RenderType renderType() {
				return GLOW;
			}
		});
	}

	@Override
	public ResourceLocation getTextureLocation(CinderBellowsEntity entity) {
		return TEXTURE;
	}

	@Override
	public void render(CinderBellowsEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight) {
		if (PuppeteerSummonRenderHelper.shouldSkipRender(entity)) return;
		CinderBreathFlames.emit(entity, partialTicks);
		super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
	}
}
