package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.seraphae.SeraphaeState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Special emissive render layer for Saint Seraphae's wing-like radiance protrusions
 * and halo crown. Renders these parts at full brightness with a time-modulated
 * gold-to-white pulsing glow, making them visually distinct from the base model.
 */
public class SeraphaeWingGlowLayer<T extends SeraphaeEntity> extends RenderLayer<T, SeraphaeModel<T>> {

	private static final ResourceLocation GLOW_TEXTURE = Hemomancy.rloc("textures/entity/seraphae/seraphae_glow.png");

	private static final RenderType GLOW = RenderType.eyes(GLOW_TEXTURE);

	/** Maximum light value — ensures wings always render at full brightness. */
	private static final int FULL_BRIGHT = 0x00F000F0;

	public SeraphaeWingGlowLayer(RenderLayerParent<T, SeraphaeModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
					   T entity, float limbSwing, float limbSwingAmount,
					   float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		// Don't render glow when dispersed (entity is invisible)
		if (entity.getSeraphaeState() == SeraphaeState.DISPERSED) {
			return;
		}

		// Pulsing gold → white → gold color cycle
		float time = ageInTicks * 0.08F;
		float pulse = ((float) Math.sin(time) + 1.0F) * 0.5F; // 0..1 oscillation

		// Base gold (1.0, 0.85, 0.4) → bright white (1.0, 1.0, 1.0)
		float r = 1.0F;
		float g = 0.85F + 0.15F * pulse;
		float b = 0.4F + 0.6F * pulse;

		// Intensify glow during FRACTURING state (more unstable = brighter)
		if (entity.getSeraphaeState() == SeraphaeState.FRACTURING) {
			float flicker = ((float) Math.sin(ageInTicks * 0.5F) + 1.0F) * 0.5F;
			g = Math.min(1.0F, g + 0.1F * flicker);
			b = Math.min(1.0F, b + 0.2F * flicker);
		}

		VertexConsumer vertexConsumer = buffer.getBuffer(GLOW);
		this.getParentModel().renderToBuffer(poseStack, vertexConsumer, FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
	}
}
