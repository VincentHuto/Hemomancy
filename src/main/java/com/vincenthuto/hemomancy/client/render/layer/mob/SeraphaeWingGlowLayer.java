package com.vincenthuto.hemomancy.client.render.layer.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeModel;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import com.vincenthuto.hemomancy.client.model.entity.boss.seraphae.SeraphaeAnimState;

/**
 * Emissive mask for Seraphae's eyes, vessels, selected feather shafts,
 * and halo inlays. Renders these surfaces at full brightness with a time-modulated
 * gold-to-white pulsing glow, making them visually distinct from the base model.
 */
public class SeraphaeWingGlowLayer<T extends SeraphaeEntity> extends RenderLayer<T, SeraphaeModel<T>> {

	private static final ResourceLocation GLOW_TEXTURE = Hemomancy.rloc("textures/entity/boss/seraphae/seraphae_glow.png");

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
		if (entity.isInvisible() || entity.getSeraphaeState() == SeraphaeState.DISPERSED) return;
		var motion = SeraphaeAnimState.of(entity);
		motion.update(entity, partialTicks);
		var pose = motion.pose(entity, ageInTicks);
		float beat = .5F + .5F * (float) Math.sin(ageInTicks * .24F);
		float glow = Math.min(1, pose.glow() + beat * pose.strain() * .12F);
		int color = FastColor.ARGB32.color((int) (150 + glow * 105), 255,
				(int) (190 + glow * 65), (int) (105 + glow * 135));
		VertexConsumer vertices = buffer.getBuffer(GLOW);
		getParentModel().renderToBuffer(poseStack, vertices, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, color);
	}
}
