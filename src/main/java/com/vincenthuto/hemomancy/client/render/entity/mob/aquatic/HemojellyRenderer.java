package com.vincenthuto.hemomancy.client.render.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.HemojellyModel;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.HemojellyEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class HemojellyRenderer extends MobRenderer<HemojellyEntity, HemojellyModel> {

	protected static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/hemojelly/hemojelly_man_o_war.png");
	private static final RenderType GLOW = RenderType.eyes(Hemomancy.rloc("textures/entity/hemojelly/hemojelly_man_o_war_glow.png"));

	public HemojellyRenderer(Context renderManagerIn) {
		super(renderManagerIn, new HemojellyModel(renderManagerIn.bakeLayer(HemojellyModel.LAYER_LOCATION)), 0.4F);
		addLayer(new GlowLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(HemojellyEntity entity) {
		return TEXTURE;
	}

	/** The stinging beads pulse as the colony drifts and brighten while it holds prey. */
	private static final class GlowLayer extends RenderLayer<HemojellyEntity, HemojellyModel> {
		GlowLayer(RenderLayerParent<HemojellyEntity, HemojellyModel> parent) {
			super(parent);
		}

		@Override
		public void render(PoseStack pose, MultiBufferSource buffers, int light, HemojellyEntity entity, float limbSwing,
				float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			float beat = Math.max(0.0F, Mth.sin(ageInTicks * 0.15F + entity.getId()));
			float level = entity.captureTicks() > 0 ? 1.0F : 0.45F + 0.4F * beat;
			int channel = (int) (255 * level);
			getParentModel().renderToBuffer(pose, buffers.getBuffer(GLOW), light, OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(255, channel, channel, channel));
		}
	}
}
