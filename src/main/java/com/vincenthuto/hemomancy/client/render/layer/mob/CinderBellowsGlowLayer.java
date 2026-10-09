package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.summon.CinderBellowsModel;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** Emissive eyes, lung folds, throat, coil cracks and torch, banked at rest and flaring with the breath. */
public class CinderBellowsGlowLayer extends RenderLayer<CinderBellowsEntity, CinderBellowsModel> {
	private static final RenderType GLOW =
			RenderType.eyes(Hemomancy.rloc("textures/entity/puppeteer_summon/cinder_bellows_glow.png"));

	public CinderBellowsGlowLayer(RenderLayerParent<CinderBellowsEntity, CinderBellowsModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CinderBellowsEntity entity,
					   float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
					   float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		int cycle = entity.getBreathCycle();
		float heat = cycle == 0 ? 0 : CinderBellowsModel.emberHeat(Math.min(100, cycle + partialTicks));
		int level = (int) (255 * Mth.clamp(.35F + .65F * heat + Mth.sin(ageInTicks * .3F) * .04F, 0, 1));
		getParentModel().renderToBuffer(poseStack, buffer.getBuffer(GLOW), LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, FastColor.ARGB32.color(255, level, level, level));
	}
}
